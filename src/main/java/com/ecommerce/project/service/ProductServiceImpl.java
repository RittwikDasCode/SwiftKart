package com.ecommerce.project.service;

//import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CategoryRepository categoryRepository;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	FileService fileService;
	
	@Value("${project.image}")
	private String path;
	
	@Override
	public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
		
		Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
		
		boolean isProductNotPresent =true;
		List<Product> products = category.getProducts();
		for(Product value : products) {
			if(value.getProductName().equals(productDTO.getProductName())) {
				isProductNotPresent = false;
				break;
			}
		}
		if(isProductNotPresent) {
			Product product = modelMapper.map(productDTO, Product.class);
			product.setImage("default.png");
			product.setCategory(category);
			
			double specialPrice = product.getPrice()-((product.getDiscount() * 0.01) * product.getPrice());
			product.setSpecialPrice(specialPrice);
			
			Product saveProduct = productRepository.save(product);
			return modelMapper.map(saveProduct, ProductDTO.class);
		} else {
			throw new APIException("Product already exist!!!");
		}
	}

	@Override
	public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts = productRepository.findAll(pageDetails);
		
		List<Product> products = pageProducts.getContent();
		List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
		
		
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOS);
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		
		return productResponse;
	}

	@Override
	public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		//Check product is 0
		Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
		
		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
		
		List<Product> products = pageProducts.getContent();
		List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
		
		if(products.isEmpty()) {
			throw new APIException(category.getCategoryName()+ " category does not have any product.");
		}
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOS);
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		
		return productResponse;
	}

	@Override
	public ProductResponse searchProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		
		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%" +keyword+ "%", pageDetails);
		
		List<Product> products = pageProducts.getContent();
		List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
		
		if(products.isEmpty()) {
			throw new APIException("Products not found witht the keyword: "+keyword);
		}
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOS);
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		
		return productResponse;
	}

	@Override
	public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
		//Get the existing product from DB
		Product productFromDb = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
		
		//Update the product info with the one in request body
		Product product = modelMapper.map(productDTO, Product.class);
		productFromDb.setProductName(product.getProductName());
		productFromDb.setDescription(product.getDescription());
		productFromDb.setQuantity(product.getQuantity());
		productFromDb.setDiscount(product.getDiscount());
		productFromDb.setPrice(product.getPrice());
		double specialPrice = product.getPrice()-((product.getDiscount() * 0.01) * product.getPrice());
		productFromDb.setSpecialPrice(specialPrice);
		
		//Save to DB
		Product savedProduct = productRepository.save(productFromDb);
		
		List<Cart> carts = cartRepository.findCartsByProductId(productId);
		
		List<CartDTO> cartDTOs = carts.stream().map(cart -> {
			CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
			List<ProductDTO> products = cart.getCartItems().stream().map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());
			return cartDTO;
		}).collect(Collectors.toList());
		
		cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
		
		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	public ProductDTO deleteProduct(Long productId) {
		Product product = productRepository.findById(productId).orElseThrow(() ->new ResourceNotFoundException("Product", "productId", productId));
		
		List<Cart> carts = cartRepository.findCartsByProductId(productId);
		
		carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
		
		productRepository.delete(product);
		return modelMapper.map(product, ProductDTO.class);
	}

	@Override
	public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
		//Get the product from the DB
		Product productFromDb = productRepository.findById(productId).orElseThrow(() ->new ResourceNotFoundException("Product", "productId", productId));
		
		//Uploading the image to server
		//Get the file name of the uploaded image
		
		String fileName = fileService.uploadImage(path, image);
		
		//Updating the new filename to the product
		productFromDb.setImage(fileName);
		
		//Save updated product
		Product updatedProduct = productRepository.save(productFromDb);
		
		//return DTO after mapping product to DTO
		return modelMapper.map(updatedProduct, ProductDTO.class);
	}



	
}
