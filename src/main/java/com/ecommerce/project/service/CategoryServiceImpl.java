package com.ecommerce.project.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

	
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Override
	public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		
		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		
		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
		
		List<Category> categories = categoryPage.getContent();
		if(categories.isEmpty())
			throw new APIException("No category created till now!");
		List<CategoryDTO> categoryDTOs = categories.stream().map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
		
		CategoryResponse categoryResponse = new CategoryResponse();
		categoryResponse.setContent(categoryDTOs);
		categoryResponse.setPageNumber(categoryPage.getNumber());
		categoryResponse.setPageSize(categoryPage.getSize());
		categoryResponse.setTotalElements(categoryPage.getTotalElements());
		categoryResponse.setTotalPages(categoryPage.getTotalPages());
		categoryResponse.setLastPage(categoryPage.isLast());
		return categoryResponse;
	}

	@Override
	public CategoryDTO createCategory(CategoryDTO categoryDTO) {
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category categoryfromDb = categoryRepository.findByCategoryName(category.getCategoryName());
		if(categoryfromDb != null)
			throw new APIException("Category with the name " +category.getCategoryName() + " already exists!!!");
		Category savedCategory = categoryRepository.save(category);
		return modelMapper.map(savedCategory, CategoryDTO.class);
		
	}

	@Override
	public CategoryDTO deleteCategory(Long categoryId) {
		//List<Category> categories = categoryRepository.findAll();
		
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
		
		//Category category = categories.stream().filter(c -> c.getCategoryId().equals(categoryId)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
		
		categoryRepository.delete(category);
		return modelMapper.map(category, CategoryDTO.class);
	}

	@Override
	public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
		//Optional<Category> savedCategoryOptional = categoryRepository.findById(categoryId);
		
		Category savedCategory = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
		
		Category category = modelMapper.map(categoryDTO, Category.class);
		category.setCategoryId(categoryId);
		savedCategory = categoryRepository.save(category);
		
		return modelMapper.map(savedCategory, CategoryDTO.class);
		
		/*
		Optional<Category> optionalCategory = categories.stream().filter(c -> c.getCategoryId().equals(categoryId)).findFirst();
		
		if(optionalCategory.isPresent()) {
			Category existingCategory = optionalCategory.get();
			existingCategory.setCategoryName(category.getCategoryName());
			Category savedCategory = categoryRepository.save(existingCategory);
			return savedCategory;
		}else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
		}
		*/
		
	}


}
