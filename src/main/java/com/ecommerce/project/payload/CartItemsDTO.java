package com.ecommerce.project.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsDTO {

	private Long cartItemDTO;
	private CartDTO cart;
	private ProductDTO productDTO;
	private Integer quantity;
	private Double discount;
	private Double productPrice;
}
