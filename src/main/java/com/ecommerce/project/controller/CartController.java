package com.ecommerce.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;

@RestController
@RequestMapping("/api")
public class CartController {

	@Autowired
	private CartService cartService;
	
	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	AuthUtil authUtil;
	
	@PostMapping("/carts/products/{productId}/quantity/{quantity}")
	public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity){
		CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
	}
	
	@GetMapping("/carts")
	public ResponseEntity<List<CartDTO>> getCart(){
		List<CartDTO> cartDTO = cartService.getAllCarts();
		return new ResponseEntity<List<CartDTO>>(cartDTO, HttpStatus.FOUND);
	}
	
	@GetMapping("/carts/users/cart")
	public ResponseEntity<CartDTO> getCartById(){
		String emailId = authUtil.loggedInEmail();
		Cart cart = cartRepository.findCartByEmail(emailId);
		Long cartId = cart.getCartId();
		
		CartDTO cartDTO = cartService.getCart(emailId, cartId);
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
	
	@PutMapping("/carts/products/{productId}/quantity/{operation}")
	public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable String operation){
		CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation.equalsIgnoreCase("delete") ? -1 : 1);
		return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
	}
	
	@DeleteMapping("/carts/{cartId}/products/{productId}")
	public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId, @PathVariable Long productId){
		String status = cartService.deleteProductFromCart(cartId, productId);
		return new ResponseEntity<String>(status, HttpStatus.OK);
	}
	
}
