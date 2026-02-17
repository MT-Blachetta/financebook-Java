package com.financebook.service;

import com.financebook.dto.request.PaymentItemCreateRequest;
import com.financebook.dto.request.PaymentItemUpdateRequest;
import com.financebook.dto.response.PaymentItemReadResponse;
import com.financebook.entity.*;
import com.financebook.exception.ResourceNotFoundException;
import com.financebook.exception.UnauthorizedException;
import com.financebook.exception.ValidationException;
import com.financebook.repository.*;
import com.financebook.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Payment item service with CRUD operations.
 * Implements category validation, fee calculation, and multi-user isolation.
 * Mirrors Python's payment item endpoints from main.py.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentItemService {
    
    private final PaymentItemRepository paymentItemRepository;
    private final RecipientRepository recipientRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    private final TransactionFeeRecordRepository feeRecordRepository;
    private final FeeEngineService feeEngineService;
    private final CategoryService categoryService;
    
    /**
     * Create a new payment item with categories and fee application.
     * Mirrors Python's create_payment_item endpoint.
     */
    @Transactional
    public PaymentItemReadResponse createPaymentItem(
            PaymentItemCreateRequest request, 
            String username) {
        
        User user = getUserByUsername(username);
        
        // 1. Validate recipient if provided
        Recipient recipient = null;
        if (request.getRecipientId() != null) {
            recipient = recipientRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
            
            if (!recipient.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("Recipient does not belong to you");
            }
        }
        
        // 2. Get standard type for later use
        CategoryType standardType = categoryTypeRepository.findByUserIdAndName(user.getId(), "standard")
                .orElse(null);
        Long standardTypeId = standardType != null ? standardType.getId() : null;
        
        // 3. Validate categories if provided
        Set<Category> categories = new HashSet<>();
        Category standardCategory = null;
        
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Map<Long, Category> seenTypes = new HashMap<>();
            
            for (Long catId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category with id " + catId + " not found"));
                
                if (!category.getUser().getId().equals(user.getId())) {
                    throw new UnauthorizedException("Category " + catId + " does not belong to you");
                }
                
                // Enforce one category per type
                if (seenTypes.containsKey(category.getType().getId())) {
                    throw new ValidationException("Only one category per type is allowed");
                }
                
                seenTypes.put(category.getType().getId(), category);
                categories.add(category);
                
                // Set standard category if this is a standard type category
                if (standardTypeId != null && category.getType().getId().equals(standardTypeId)) {
                    standardCategory = category;
                }
            }
        } else {
            // Assign default UNCLASSIFIED category
            Category defaultCat = categoryRepository.findByUserIdAndName(user.getId(), "UNCLASSIFIED")
                    .orElse(null);
            if (defaultCat != null) {
                categories.add(defaultCat);
                if (standardTypeId != null && defaultCat.getType().getId().equals(standardTypeId)) {
                    standardCategory = defaultCat;
                }
            }
        }
        
        // 4. Create payment item
        PaymentItem item = PaymentItem.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .periodic(request.getPeriodic() != null ? request.getPeriodic() : false)
                .description(request.getDescription())
                .recipient(recipient)
                .standardCategory(standardCategory)
                .user(user)
                .categories(categories)
                .build();
        
        item = paymentItemRepository.save(item);
        
        // 5. Compute and apply transaction fee
        BigDecimal fee = feeEngineService.createFeeRecord(item, user.getId());
        
        // 6. Build response
        PaymentItemReadResponse response = DtoMapper.toPaymentItemReadResponse(item);
        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            response.setTransactionFee(fee);
        }
        
        return response;
    }
    
    /**
     * Get all payment items with optional filtering.
     * Mirrors Python's list_payment_items endpoint.
     */
    @Transactional(readOnly = true)
    public List<PaymentItemReadResponse> getAllPaymentItems(
            Boolean expenseOnly,
            Boolean incomeOnly,
            List<Long> categoryIds,
            String username) {
        
        User user = getUserByUsername(username);
        
        if (Boolean.TRUE.equals(expenseOnly) && Boolean.TRUE.equals(incomeOnly)) {
            throw new ValidationException("Choose only one filter: expense_only or income_only");
        }
        
        List<PaymentItem> items;
        
        // Apply category filtering with descendant expansion
        if (categoryIds != null && !categoryIds.isEmpty()) {
            log.info("Filtering with category IDs: {}", categoryIds);
            
            // Expand category list to include all descendants
            Set<Long> expandedIds = categoryService.expandCategoryIds(
                    new HashSet<>(categoryIds), user.getId());
            
            log.info("Expanded category IDs (including descendants): {}", expandedIds);
            
            items = paymentItemRepository.findByUserIdAndCategoryIdsOrderByDateDesc(
                    user.getId(), expandedIds);
        } else if (Boolean.TRUE.equals(expenseOnly)) {
            items = paymentItemRepository.findExpensesByUserIdOrderByDateDesc(user.getId());
        } else if (Boolean.TRUE.equals(incomeOnly)) {
            items = paymentItemRepository.findIncomesByUserIdOrderByDateDesc(user.getId());
        } else {
            items = paymentItemRepository.findByUserIdOrderByDateDesc(user.getId());
        }
        
        // Fetch transaction fees efficiently
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(PaymentItem::getId).collect(Collectors.toList());
            List<TransactionFeeRecord> feeRecords = feeRecordRepository.findByPaymentItemIdIn(itemIds);
            
            Map<Long, BigDecimal> feeMap = feeRecords.stream()
                    .collect(Collectors.toMap(
                            r -> r.getPaymentItem().getId(),
                            TransactionFeeRecord::getFeeAmount
                    ));
            
            // Set transaction fees on items
            for (PaymentItem item : items) {
                if (feeMap.containsKey(item.getId())) {
                    item.setTransactionFee(feeMap.get(item.getId()));
                }
            }
        }
        
        log.info("Found {} payment items matching the filters", items.size());
        
        return DtoMapper.toPaymentItemReadResponseList(items);
    }
    
    /**
     * Get a single payment item by ID.
     */
    @Transactional(readOnly = true)
    public PaymentItemReadResponse getPaymentItemById(Long itemId, String username) {
        User user = getUserByUsername(username);
        
        PaymentItem item = paymentItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to access this item");
        }
        
        // Fetch transaction fee
        TransactionFeeRecord feeRecord = feeRecordRepository.findByPaymentItemId(itemId).orElse(null);
        if (feeRecord != null) {
            item.setTransactionFee(feeRecord.getFeeAmount());
        }
        
        return DtoMapper.toPaymentItemReadResponse(item);
    }
    
    /**
     * Update an existing payment item.
     * Mirrors Python's update_payment_item endpoint.
     */
    @Transactional
    public PaymentItemReadResponse updatePaymentItem(
            Long itemId,
            PaymentItemUpdateRequest request,
            String username) {
        
        log.info("Starting update for payment item {}", itemId);
        
        User user = getUserByUsername(username);
        
        PaymentItem item = paymentItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to update this item");
        }
        
        // Update basic fields
        if (request.getAmount() != null) {
            item.setAmount(request.getAmount());
        }
        if (request.getDate() != null) {
            item.setDate(request.getDate());
        }
        if (request.getPeriodic() != null) {
            item.setPeriodic(request.getPeriodic());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        
        // Validate and update recipient if provided
        if (request.getRecipientId() != null) {
            Recipient recipient = recipientRepository.findById(request.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
            
            if (!recipient.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("Recipient does not belong to you");
            }
            
            item.setRecipient(recipient);
        }
        
        // Update categories if provided
        if (request.getCategoryIds() != null) {
            log.info("Processing categories: {}", request.getCategoryIds());
            
            // Get standard type
            CategoryType standardType = categoryTypeRepository.findByUserIdAndName(user.getId(), "standard")
                    .orElse(null);
            Long standardTypeId = standardType != null ? standardType.getId() : null;
            
            // Clear existing categories
            item.getCategories().clear();
            
            // Add new categories
            Set<Category> newCategories = new HashSet<>();
            Map<Long, Category> seenTypes = new HashMap<>();
            Category newStandardCategory = null;
            
            if (!request.getCategoryIds().isEmpty()) {
                for (Long catId : request.getCategoryIds()) {
                    Category category = categoryRepository.findById(catId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category with id " + catId + " not found"));
                    
                    if (!category.getUser().getId().equals(user.getId())) {
                        throw new UnauthorizedException("Category " + catId + " does not belong to you");
                    }
                    
                    if (seenTypes.containsKey(category.getType().getId())) {
                        throw new ValidationException("Only one category per type is allowed");
                    }
                    
                    seenTypes.put(category.getType().getId(), category);
                    newCategories.add(category);
                    
                    if (standardTypeId != null && category.getType().getId().equals(standardTypeId)) {
                        newStandardCategory = category;
                    }
                }
            } else {
                // Assign default UNCLASSIFIED
                Category defaultCat = categoryRepository.findByUserIdAndName(user.getId(), "UNCLASSIFIED")
                        .orElse(null);
                if (defaultCat != null) {
                    newCategories.add(defaultCat);
                    if (standardTypeId != null && defaultCat.getType().getId().equals(standardTypeId)) {
                        newStandardCategory = defaultCat;
                    }
                }
            }
            
            item.setCategories(newCategories);
            item.setStandardCategory(newStandardCategory);
        }
        
        // Save changes
        item = paymentItemRepository.save(item);
        
        // Recompute fee if amount changed
        if (request.getAmount() != null) {
            log.info("Amount changed, recomputing transaction fee");
            feeEngineService.recomputeFeeRecord(item, request.getAmount(), user.getId());
        }
        
        // Fetch updated fee
        TransactionFeeRecord feeRecord = feeRecordRepository.findByPaymentItemId(itemId).orElse(null);
        
        PaymentItemReadResponse response = DtoMapper.toPaymentItemReadResponse(item);
        if (feeRecord != null) {
            response.setTransactionFee(feeRecord.getFeeAmount());
        }
        
        log.info("Successfully updated payment item {}", itemId);
        return response;
    }
    
    /**
     * Delete a payment item.
     * Mirrors Python's delete_payment_item endpoint.
     */
    @Transactional
    public void deletePaymentItem(Long itemId, String username) {
        log.info("Starting deletion of payment item {}", itemId);
        
        User user = getUserByUsername(username);
        
        PaymentItem item = paymentItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to delete this item");
        }
        
        // Delete associated invoice file if it exists
        if (item.getInvoicePath() != null) {
            try {
                Path filePath = Paths.get("app/invoices", item.getInvoicePath());
                if (Files.exists(filePath)) {
                    log.info("Deleting invoice file: {}", filePath);
                    Files.delete(filePath);
                }
            } catch (Exception e) {
                log.warn("Failed to delete invoice file: {}", e.getMessage());
            }
        }
        
        // Refund transaction fee
        feeEngineService.refundFeeRecord(itemId);
        
        // Delete payment item (categories will be removed automatically via cascade)
        paymentItemRepository.delete(item);
        
        log.info("Successfully deleted payment item {}", itemId);
    }
    
    /**
     * Get user by username.
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
