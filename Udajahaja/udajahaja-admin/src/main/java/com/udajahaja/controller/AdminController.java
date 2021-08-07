package com.udajahaja.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udajahaja.dto.AirlineSaveDTO;
import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.service.AdminService;

@RestController()
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService service;
	
	@PostMapping(path = "/save", consumes = "application/json")
	public ResponseEntity<AirlineSaveDTO> saveAirline(@RequestBody AirlineSaveDTO dto){
		service.save(dto);
		return null;
	}
	
	@GetMapping(path = "/findAirlines")
	public ResponseEntity<List<AirlineEntity>> findAairlines(){
		
		return new ResponseEntity<List<AirlineEntity>>(service.findAllAirlines(),HttpStatus.OK);
		 
				
		//return null;
	}
}
