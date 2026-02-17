package com.financebook.service;

import com.financebook.dto.request.CategoryCreateRequest;
import com.financebook.dto.request.CategoryUpdateRequest;
import com.financebook.dto.response.CategoryReadResponse;
import com.financebook.entity.Category;
import com.financebook.entity.CategoryType;
import com.financebook.entity.User;
import com.financebook.exception.ResourceNotFoundException;
import com.financebook.exception.UnauthorizedException;
import com.financebook.exception.ValidationException;
import com.financebook.repository.CategoryRepository;
import com.financebook.repository.CategoryTypeRepository;
import com.financebook.repository.UserRepository;
import com.financebook.util.Constants;
import com.financebook.util.DtoMapper;
import com.financebook.util.NameNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Category service for managing hierarchical category trees.
 * Implements tree operations: descendants, ancestors, depth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new category with normalized name.
     * Mirrors Python's create_category endpoint.
     */
    @Transactional
    public CategoryReadResponse createCategory(CategoryCreateRequest request, String username) {
        User user = getUserByUsername(username);
        
        // Normalize and validate name
        String normalizedName = NameNormalizer.normalize(request.getName());
        if (normalizedName.isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }
        if (normalizedName.length() > Constants.MAX_CATEGORY_NAME_LENGTH) {
            throw new ValidationException("Category name exceeds maximum length");
        }
        
        // Check for duplicate name (per user)
        if (categoryRepository.findByUserIdAndName(user.getId(), normalizedName).isPresent()) {
            throw new ValidationException("Category name already exists");
        }
        
        // Validate type exists
        CategoryType type = categoryTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Category type not found"));
        
        // Validate parent if provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }
        
        // Create category
        Category category = Category.builder()
                .name(normalizedName)
                .type(type)
                .parent(parent)
                .iconFile(request.getIconFile())
                .user(user)
                .build();
        
        category = categoryRepository.save(category);
        
        return DtoMapper.toCategoryReadResponse(category);
    }
    
    /**
     * Get all categories for the current user.
     */
    @Transactional(readOnly = true)
    public List<CategoryReadResponse> getAllCategories(String username) {
        User user = getUserByUsername(username);
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        return DtoMapper.toCategoryReadResponseList(categories);
    }
    
    /**
     * Get a single category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryReadResponse getCategoryById(Long categoryId, String username) {
        User user = getUserByUsername(username);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        // Verify ownership
        if (!category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to access this category");
        }
        
        return DtoMapper.toCategoryReadResponse(category);
    }
    
    /**
     * Get categories by type ID.
     */
    @Transactional(readOnly = true)
    public List<CategoryReadResponse> getCategoriesByType(Long typeId, String username) {
        User user = getUserByUsername(username);
        List<Category> categories = categoryRepository.findByUserIdAndTypeId(user.getId(), typeId);
        return DtoMapper.toCategoryReadResponseList(categories);
    }
    
    /**
     * Get all descendant categories of a given category.
     * Mirrors Python's list_category_descendants endpoint.
     */
    @Transactional(readOnly = true)
    public List<CategoryReadResponse> getDescendants(Long categoryId, String username) {
        User user = getUserByUsername(username);
        
        Category root = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        // Verify ownership
        if (!root.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to access this category");
        }
        
        // Breadth-first traversal to collect all descendants
        List<Category> descendants = new ArrayList<>();
        List<Long> queue = new ArrayList<>();
        queue.add(categoryId);
        
        while (!queue.isEmpty()) {
            Long currentId = queue.remove(0);
            List<Category> children = categoryRepository.findByParentId(currentId);
            
            for (Category child : children) {
                descendants.add(child);
                queue.add(child.getId());
            }
        }
        
        return DtoMapper.toCategoryReadResponseList(descendants);
    }
    
    /**
     * Get all descendant IDs (including the root).
     * Used for filtering payment items by category.
     * Mirrors Python's category expansion logic.
     */
    @Transactional(readOnly = true)
    public Set<Long> expandCategoryIds(Set<Long> categoryIds, Long userId) {
        Set<Long> expandedIds = new HashSet<>(categoryIds);
        
        for (Long categoryId : categoryIds) {
            gatherDescendantIds(categoryId, expandedIds);
        }
        
        return expandedIds;
    }
    
    /**
     * Recursively gather all descendant IDs.
     */
    private void gatherDescendantIds(Long rootId, Set<Long> expandedIds) {
        List<Long> queue = new ArrayList<>();
        queue.add(rootId);
        
        while (!queue.isEmpty()) {
            Long currentId = queue.remove(0);
            List<Category> children = categoryRepository.findByParentId(currentId);
            
            for (Category child : children) {
                if (!expandedIds.contains(child.getId())) {
                    expandedIds.add(child.getId());
                    queue.add(child.getId());
                }
            }
        }
    }
    
    /**
     * Update an existing category.
     * Mirrors Python's update_category endpoint.
     */
    @Transactional
    public CategoryReadResponse updateCategory(
            Long categoryId, 
            CategoryUpdateRequest request, 
            String username) {
        
        User user = getUserByUsername(username);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        // Verify ownership
        if (!category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to update this category");
        }
        
        // Update name if provided
        if (request.getName() != null) {
            String normalizedName = NameNormalizer.normalize(request.getName());
            if (normalizedName.isEmpty()) {
                throw new ValidationException("Category name cannot be empty");
            }
            
            // Check for duplicate name (excluding current category)
            categoryRepository.findByUserIdAndName(user.getId(), normalizedName)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(categoryId)) {
                            throw new ValidationException("Category name already exists");
                        }
                    });
            
            category.setName(normalizedName);
        }
        
        // Update parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        
        // Update type if provided
        if (request.getTypeId() != null) {
            CategoryType type = categoryTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category type not found"));
            category.setType(type);
        }
        
        // Update icon if provided
        if (request.getIconFile() != null) {
            category.setIconFile(request.getIconFile());
        }
        
        category = categoryRepository.save(category);
        
        return DtoMapper.toCategoryReadResponse(category);
    }
    
    /**
     * Get user by username.
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
