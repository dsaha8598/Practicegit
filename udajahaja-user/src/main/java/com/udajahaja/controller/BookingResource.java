package com.udajahaja.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.udajahaja.dto.BookingDTO;
import com.udajahaja.dto.SearchDTO;
import com.udajahaja.dto.SearchedFlightDetails;
import com.udajahaja.dto.TicketDTO;
import com.udajahaja.exception.CustomException;
import com.udajahaja.service.BookingService;

@RestController()
@RequestMapping("/api/user/")
public class BookingResource {
	
	@Autowired
	private BookingService bookingService;
	
	@PostMapping(path = "find/flights")
	@Cacheable(value="flights")
    public ResponseEntity<List<SearchedFlightDetails>> findFlights(@RequestBody SearchDTO dto){
		return new ResponseEntity<List<SearchedFlightDetails>>(bookingService.getSearchedFlights(dto),HttpStatus.OK);

	}
	
	@PostMapping("save/bookings")
	public ResponseEntity<String> saveBookings(@RequestBody BookingDTO booking){
		bookingService.saveBooking(booking);
		return null;
	}
	
	
	@GetMapping("findAll/bookings/{email}")
	public ResponseEntity<List<TicketDTO>> findAllBookings(@PathVariable("email")String email) throws CustomException{
		 return new ResponseEntity<List<TicketDTO>>(bookingService.findAllBookings(email),HttpStatus.OK);
	}
	
	@PostMapping("cancel/ticket/{id}")
	public ResponseEntity<String> cancelTickets(@PathVariable("id")Integer id) throws CustomException{
		bookingService.cancelTicket(id);
		 return new ResponseEntity<String>("SUCCESS",HttpStatus.OK);
	}
	
	@GetMapping("findAll/bookings/history")
	public ResponseEntity<List<TicketDTO>> findBookingHistory() throws CustomException{
		 return new ResponseEntity<List<TicketDTO>>(bookingService.findBookingHistory(),HttpStatus.OK);
	}
	
	@GetMapping("/test")
	public ResponseEntity<String> test() throws CustomException{
		 return new ResponseEntity<String>("test",HttpStatus.OK);
	}
	
	

}
