package com.ecommerce.project.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService{

	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CartItemRepository cartItemRepository;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	AuthUtil authUtil;
	
	@Override
	public CartDTO addProductToCart(Long productId, Integer quantity) {
		// Find existing Cart or create one
		Cart cart = createCart();
		
		// Retrieve Product Details
		Product product = productRepository.findById(productId).orElseThrow( () -> new ResourceNotFoundException("Product", "productId", productId));
		
		// Perform Validations
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
		if(cartItem != null)
			throw new APIException("The product " + product.getProductName() + " is already exists in the Cart!");
		if(product.getQuantity() == 0)
			throw new APIException("Ooops!!! it's out of stock!!! \n The product " + product.getProductName() + " is not available at this moment! Please try later!");
		if(product.getQuantity() < quantity)
			throw new APIException("Ooops!!! it's out of quantity!!! \n Please order the product " + product.getProductName() + " within the range of the product quantity " + product.getQuantity());
		
		// Create Cart Item
		CartItem newCartItem = new CartItem();
		newCartItem.setProduct(product);
		newCartItem.setCart(cart);
		newCartItem.setQuantity(quantity);
		newCartItem.setDiscount(product.getDiscount());
		newCartItem.setProductPrice(product.getSpecialPrice());
		
		// Save Cart Item
		cartItemRepository.save(newCartItem);
		product.setQuantity(product.getQuantity());
		
		cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
		cartRepository.save(cart);
		// Return update Cart
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		
		List<CartItem> cartItems = cart.getCartItems();
		Stream<ProductDTO> productStream = cartItems.stream().map(item -> { ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
																				map.setQuantity(item.getQuantity());
																				return map;});
		cartDTO.setProducts(productStream.toList());
		return cartDTO;
	}
	
	@Override
	public List<CartDTO> getAllCarts() {
		List<Cart> carts = cartRepository.findAll();
		if(carts.size() == 0)
			throw new APIException("No Cart Exist!");
		
		List<CartDTO> cartDTOs = carts.stream()
				.map(cart -> {
					CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
					
//					List<ProductDTO> products = cart.getCartItems().stream()
//							.map(p -> modelMapper.map(p.getProduct() , ProductDTO.class))
//							.collect(Collectors.toList());
					
					List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
						ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
						productDTO.setQuantity(cartItem.getQuantity());
						return productDTO;
					}).collect(Collectors.toList());;
					
					cartDTO.setProducts(products);
					return cartDTO;
				}).collect(Collectors.toList());
		
		return cartDTOs;
	}
	
	@Override
	public CartDTO getCart(String emailId, Long cartId) {
		Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
		if(cart == null)
			throw new ResourceNotFoundException("Cart", "cartId", cartId);
		
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
		
		List<ProductDTO> products = cart.getCartItems().stream().map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
		
		cartDTO.setProducts(products);
		return cartDTO;
	}
	
	@Transactional
	@Override
	public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
		String emailId = authUtil.loggedInEmail();
		Cart userCart = cartRepository.findCartByEmail(emailId);
		Long cartId = userCart.getCartId();
		
		Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
		
		// Retrieve Product Details
		Product product = productRepository.findById(productId).orElseThrow( () -> new ResourceNotFoundException("Product", "productId", productId));

		//Performing Validation
		if(product.getQuantity() == 0)
			throw new APIException("Ooops!!! it's out of stock!!! \n The product " + product.getProductName() + " is not available at this moment! Please try later!");
		if(product.getQuantity() < quantity)
			throw new APIException("Ooops!!! it's out of quantity!!! \n Please order the product " + product.getProductName() + " within the range of the product quantity " + product.getQuantity());
		
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
		if(cartItem == null)
			throw new APIException("Product " + product.getProductName() + " is not available in the cart!!!");
		
		//Calculating new quantity
		int newQuantity = cartItem.getQuantity() + quantity;
		
		//Validation to prevent negative quantity
		if(newQuantity < 0)
			throw new APIException("The resulting quantity cannnot be negative!!!");
		
		if(newQuantity == 0)
			deleteProductFromCart(cartId, productId);
		else{
			cartItem.setProductPrice(product.getSpecialPrice());
			cartItem.setQuantity(product.getQuantity() + quantity);
			cartItem.setDiscount(product.getDiscount());
			cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));///
			cartRepository.save(cart);
		}
		
		CartItem updatedItem = cartItemRepository.save(cartItem);
		if(updatedItem.getQuantity() == null)
			cartItemRepository.deleteById(updatedItem.getCardtItemId());
		
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		List<CartItem> cartItems = cart.getCartItems();
		
		Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
			ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
			prd.setQuantity(item.getQuantity());
			return prd;
		});
		
		cartDTO.setProducts(productStream.toList());
		return cartDTO;
	}

	@Transactional
	@Override
	public String deleteProductFromCart(Long cartId, Long productId) {
		Cart cart =cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
		if(cartItem == null)
			throw new ResourceNotFoundException("Product", "productId", productId);
		cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
		cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
		
		return "The product " + cartItem.getProduct().getProductName() + " has been removed from the cart!!!";
	}
	
	@Override
	public void updateProductInCarts(Long cartId, Long productId) {
		Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
		
		// Retrieve Product Details
		Product product = productRepository.findById(productId).orElseThrow( () -> new ResourceNotFoundException("Product", "productId", productId));
		
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
		
		if(cartItem == null)
			throw new APIException("The product " + product.getProductName() + " not available in the cart!!!");
		
		// 1000 - (100 * 2) = 800/-
		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
		
		// 300/-
		cartItem.setProductPrice(product.getSpecialPrice());
		
		// 800 + (300 * 2) = 1400/-
		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));
		
		cartItem = cartItemRepository.save(cartItem);
		
	}

	
	private Cart createCart() {
		Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
		if(userCart != null)
			return userCart;
		
		Cart cart = new Cart();
		cart.setTotalPrice(0.0);
		cart.setUser(authUtil.loggedInUser());
		
		Cart newCart = cartRepository.save(cart);
		return newCart;
	}







}
