package com.ecommerce.project.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AddressController {

	@Autowired
	AuthUtil authUtil;
	
	@Autowired
	AddressService addressService;
	
	@PostMapping("/addresses")
	public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){
		User user = authUtil.loggedInUser();
		AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);
		return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
	}
	
	@GetMapping("/addresses")
	public ResponseEntity<List<AddressDTO>> getAddresses(){
		
		List<AddressDTO> addressList = addressService.getAddresses();
		return new ResponseEntity<>(addressList, HttpStatus.OK);
	}
	
	@GetMapping("/addresses/{addressId}")
	public ResponseEntity<AddressDTO> getAddressesById(@PathVariable Long addressId){
		
		AddressDTO addressDTO = addressService.getAddressesById(addressId);
		return new ResponseEntity<>(addressDTO, HttpStatus.OK);
	}
	
	@GetMapping("/users/addresses")
	public ResponseEntity<List<AddressDTO>> getUserAddresses(){
		User user = authUtil.loggedInUser();
		List<AddressDTO> addressDTO =  addressService.getUserAddresses(user);
		return new ResponseEntity<>(addressDTO, HttpStatus.OK);
	}
	
	@PutMapping("/addresses/{addressId}")
	public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){
		
		AddressDTO updatedAddress =  addressService.updateAddress(addressId, addressDTO);
		return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
	}
	
	@DeleteMapping("/addresses/{addressId}")
	public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){
		
		String deleteAddress =  addressService.deleteAddress(addressId);
		return new ResponseEntity<>(deleteAddress, HttpStatus.OK);
	}
	
}
