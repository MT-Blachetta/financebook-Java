package com.financebook.service;

import com.financebook.dto.request.CategoryTypeCreateRequest;
import com.financebook.dto.response.CategoryTypeReadResponse;
import com.financebook.entity.CategoryType;
import com.financebook.entity.User;
import com.financebook.exception.ResourceNotFoundException;
import com.financebook.repository.CategoryTypeRepository;
import com.financebook.repository.UserRepository;
import com.financebook.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Category type service for managing classification dimensions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryTypeService {
    
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new category type.
     */
    @Transactional
    public CategoryTypeReadResponse createCategoryType(
            CategoryTypeCreateRequest request, 
            String username) {
        
        User user = getUserByUsername(username);
        
        CategoryType categoryType = CategoryType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .build();
        
        categoryType = categoryTypeRepository.save(categoryType);
        
        return DtoMapper.toCategoryTypeReadResponse(categoryType);
    }
    
    /**
     * Get all category types for the current user.
     */
    @Transactional(readOnly = true)
    public List<CategoryTypeReadResponse> getAllCategoryTypes(String username) {
        User user = getUserByUsername(username);
        List<CategoryType> categoryTypes = categoryTypeRepository.findByUserId(user.getId());
        return DtoMapper.toCategoryTypeReadResponseList(categoryTypes);
    }
    
    /**
     * Get user by username.
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
