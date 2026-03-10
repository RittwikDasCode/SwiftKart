package com.ecommerce.project.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;

import jakarta.validation.Valid;

@RestController
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	/*
	@GetMapping("/echo")
	public ResponseEntity<String> echoMessage(@RequestParam(name= "message", defaultValue = "Hello Rittwik!") String message){
		return new ResponseEntity<>("Echoed Message: " +message, HttpStatus.OK);
	}
	*/

	@GetMapping("api/public/categories")
	public ResponseEntity<CategoryResponse> getAllCategories(
			@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false)String sortBy,
			@RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder){
		CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
		return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
	}
	
	@PostMapping("api/admin/categories")
	public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
		CategoryDTO savedCategory = categoryService.createCategory(categoryDTO);
		return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
	}
	
	@DeleteMapping("api/admin/categories/{categoryId}")
	public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
		
		CategoryDTO deleteCategory = categoryService.deleteCategory(categoryId);
		return new ResponseEntity<>(deleteCategory, HttpStatus.OK);
		
	}
	
	@PutMapping("api/admin/categories/{categoryId}")
	public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId){
		
		CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
		return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
	}
	
}
