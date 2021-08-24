package com.udajahaja.controller;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.udajahaja.dto.BookingDTO;
import com.udajahaja.dto.SearchDTO;
import com.udajahaja.dto.SearchedFlightDetails;
import com.udajahaja.dto.TicketDTO;
import com.udajahaja.entity.Booking;
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
	@GetMapping("findAll/bookings/history/{email}")
	public ResponseEntity<List<TicketDTO>> findBookingHistory(@PathVariable("email")String email) throws CustomException{
		 return new ResponseEntity<List<TicketDTO>>(bookingService.findBookingHistory(email),HttpStatus.OK);
	}
	
	@PostMapping(value="download/ticket/{id}")
	public ResponseEntity<Resource> downloadTicket(@PathVariable("id")Integer id) throws IOException {
		File file=bookingService.generateTicketPDF(id);

		Resource resource = new FileSystemResource(file);

		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");

		return ResponseEntity.ok().headers(header).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
	}
	
	@GetMapping("test")
	public @ResponseBody ResponseEntity<String> test() throws CustomException{
		 return new ResponseEntity<String>("Hurray.......TEST WORKING",HttpStatus.OK);
	}
	
	@RequestMapping(path = "download/{id}", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(@PathVariable("id")Integer id) throws IOException {

	    // ...

		File file=bookingService.generateTicketPDF(id);
				//new File("//home//dipak//Desktop//USER NAME AND PASSWORDS.xlsx");
	    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

	    HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(file.length())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(resource);
	}
	
	@GetMapping("findAll/bookings")
	public ResponseEntity<List<Booking>> findBookings() throws CustomException{
		 return new ResponseEntity<List<Booking>>(bookingService.getAllBookings(),HttpStatus.OK);
	}
	
	
	

}
