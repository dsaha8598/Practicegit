package com.udajahaja.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.udajahaja.dto.BookingDTO;
import com.udajahaja.dto.Scheduler;
import com.udajahaja.dto.SearchDTO;
import com.udajahaja.dto.SearchedFlightDetails;
import com.udajahaja.dto.TicketDTO;
import com.udajahaja.entity.Booking;
import com.udajahaja.exception.CustomException;
import com.udajahaja.repository.BookingRepository;

@Service
public class BookingService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private BookingRepository bookingRepository;

	public List<SearchedFlightDetails> getSearchedFlights(SearchDTO dto) {

		List<Scheduler> schedulerList = getListOfSchedulers(dto.getFrom(), dto.getTo());
		List<SearchedFlightDetails> searchedFlightList = new ArrayList<>();
		List<Scheduler> returnFlightList = new ArrayList<>();
		if (dto.getToDate() != null) {
			returnFlightList = getListOfSchedulers(dto.getTo(), dto.getFrom());
		}
		String name = "";
		if (!schedulerList.isEmpty()) {
			URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findAirlinesNameById")
					.buildAndExpand().toUri();
			uri = UriComponentsBuilder.fromUri(uri).queryParam("id", schedulerList.get(0).getAirlineId()).build()
					.toUri();

			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
					new ParameterizedTypeReference<String>() {
					});
			name = responseEntity.getBody();
		}
		for (Scheduler scheduler : schedulerList) {
			SearchedFlightDetails searchedFlightDetails = new SearchedFlightDetails();
			searchedFlightDetails.setAirlineId(scheduler.getAirlineId());
			searchedFlightDetails.setAirlineName(name);
			searchedFlightDetails.setArrivalTime(scheduler.getArrivalTime());
			searchedFlightDetails.setBusinessTicketCost(scheduler.getBusinessTicketCost());
			searchedFlightDetails.setDepartureTime(scheduler.getDepartureTime());
			searchedFlightDetails.setEconomyTicketCost(scheduler.getEconomyTicketCost());
			searchedFlightDetails.setFlightId(scheduler.getFlightId());
			// searchedFlightDetails.setFlightName(scheduler.get);
			searchedFlightDetails.setFromPlace(scheduler.getFromPlace());
			searchedFlightDetails.setId(scheduler.getId());
			searchedFlightDetails.setToPlace(scheduler.getToPlace());
			searchedFlightDetails.setIsReturn("No");
			searchedFlightDetails.setStartDate(dto.getFromDate());
			searchedFlightList.add(searchedFlightDetails);
		}
		for (Scheduler scheduler : returnFlightList) {
			SearchedFlightDetails searchedFlightDetails = new SearchedFlightDetails();
			searchedFlightDetails.setAirlineId(scheduler.getAirlineId());
			searchedFlightDetails.setAirlineName(name);
			searchedFlightDetails.setArrivalTime(scheduler.getArrivalTime());
			searchedFlightDetails.setBusinessTicketCost(scheduler.getBusinessTicketCost());
			searchedFlightDetails.setDepartureTime(scheduler.getDepartureTime());
			searchedFlightDetails.setEconomyTicketCost(scheduler.getEconomyTicketCost());
			searchedFlightDetails.setFlightId(scheduler.getFlightId());
			// searchedFlightDetails.setFlightName(scheduler.get);
			searchedFlightDetails.setFromPlace(scheduler.getFromPlace());
			searchedFlightDetails.setId(scheduler.getId());
			searchedFlightDetails.setToPlace(scheduler.getToPlace());
			searchedFlightDetails.setIsReturn("Yes");
			searchedFlightDetails.setReturnDate(dto.getToDate());
			searchedFlightList.add(searchedFlightDetails);
		}
		return searchedFlightList;
	}

	public List<Scheduler> getListOfSchedulers(String from, String to) {

		URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findScheduler/byRoute")
				.buildAndExpand().toUri();

		uri = UriComponentsBuilder.fromUri(uri).queryParam("from", from).queryParam("to", to).build().toUri();
		ResponseEntity<List<Scheduler>> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Scheduler>>() {
				});

		return responseEntity.getBody();
	}

	public void saveBooking(BookingDTO dto) {
		Booking booking = new Booking();
		BeanUtils.copyProperties(dto, booking);
		booking.setActive(true);
		booking.setIsEconomy(dto.getIsEconomy() == null ? 0 : (dto.getIsEconomy() == true ? 1 : 0));
		booking.setIsBusinessClass(dto.getIsBusinessClass() == null ? 0 : (dto.getIsBusinessClass() == true ? 1 : 0));
		booking.setPnrNumber("PNR" + System.currentTimeMillis());
		booking = bookingRepository.save(booking);
		System.out.println("Booking saved with id -->" + booking.getId());
	}

	public List<TicketDTO> findAllBookings(String email) throws CustomException {
		Date today = new Date();
		List<TicketDTO> listOfTickets = new ArrayList<>();
		List<Booking> bookedTicketsByEmail = bookingRepository.getBookedTicketsByEmail(email);
		if (bookedTicketsByEmail.isEmpty()) {
			throw new CustomException("No tickets found");
		}
		for (Booking booking : bookedTicketsByEmail) {

			Map<String, Integer> urlParams = new HashMap<>();
			urlParams.put("id", booking.getFlightId());
			URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findAeroplaneNumberById/{id}")
					.buildAndExpand(urlParams).toUri();
			uri = UriComponentsBuilder.fromUri(uri).build().toUri();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
					new ParameterizedTypeReference<String>() {
					});

			TicketDTO dto = new TicketDTO();
			dto.setAirlineId(booking.getAirlineId());
			dto.setArrivalTime(booking.getArrivalTime());
			dto.setClassType(booking.getIsBusinessClass() == 1 ? "Business Class" : "Economy Class");
			dto.setDepartureTime(booking.getDepartureTime());
			dto.setEmail(booking.getEmail());
			dto.setFlightId(booking.getFlightId());
			dto.setFlightNumber(responseEntity.getBody());
			dto.setFrom(booking.getFrom());
			dto.setId(booking.getId());
			dto.setJourneydate(booking.getJourneydate());
			dto.setTo(booking.getTo());
			dto.setPnrNumber(booking.getPnrNumber());
			long diff = booking.getJourneydate().getTime() - today.getTime();
			long diffHours = diff / (60 * 60 * 1000);
			if (diffHours < 24) {
				dto.setIsLessThan24(true);
			} else {
				dto.setIsLessThan24(false);
			}

			listOfTickets.add(dto);
		}
		return listOfTickets;
	}

	public void cancelTicket(Integer id) {
		Booking booking = bookingRepository.findById(id).get();
		booking.setActive(false);
		bookingRepository.save(booking);
	}

	public List<TicketDTO> findBookingHistory() throws CustomException {

		List<TicketDTO> listOfTickets = new ArrayList<>();
		List<Booking> bookedTicketsByEmail = bookingRepository.findAll();
		if (bookedTicketsByEmail.isEmpty()) {
			throw new CustomException("No tickets found");
		}
		for (Booking booking : bookedTicketsByEmail) {

			Map<String, Integer> urlParams = new HashMap<>();
			urlParams.put("id", booking.getFlightId());
			URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findAeroplaneNumberById/{id}")
					.buildAndExpand(urlParams).toUri();
			uri = UriComponentsBuilder.fromUri(uri).build().toUri();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
					new ParameterizedTypeReference<String>() {
					});

			TicketDTO dto = new TicketDTO();
			dto.setAirlineId(booking.getAirlineId());
			dto.setArrivalTime(booking.getArrivalTime());
			dto.setClassType(booking.getIsBusinessClass() == 1 ? "Business Class" : "Economy Class");
			dto.setDepartureTime(booking.getDepartureTime());
			dto.setEmail(booking.getEmail());
			dto.setFlightId(booking.getFlightId());
			dto.setFlightNumber(responseEntity.getBody());
			dto.setFrom(booking.getFrom());
			dto.setId(booking.getId());
			dto.setJourneydate(booking.getJourneydate());
			dto.setTo(booking.getTo());
			dto.setPnrNumber(booking.getPnrNumber());
			dto.setStatus(booking.isActive() == true ? "Booked" : "Cancelled");

			listOfTickets.add(dto);
		}
		return listOfTickets;
	}

	public File generateTicketPDF(Integer id) throws IOException {
		
		Booking booking = bookingRepository.findById(id).get();
		Map<String, Integer> urlParams = new HashMap<>();
		urlParams.put("id", booking.getFlightId());
		URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findAeroplaneNumberById/{id}")
				.buildAndExpand(urlParams).toUri();
		uri = UriComponentsBuilder.fromUri(uri).build().toUri();
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<String>() {
				});
		
		File file=File.createTempFile("Ticket",".pdf");
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		 contentStream.beginText(); 
	       
	      //Setting the font to the Content stream
	      contentStream.setFont( PDType1Font.TIMES_ROMAN, 16 );
	       
	      //Setting the leading
	      contentStream.setLeading(14.5f);

	      //Setting the position for the line
	      contentStream.newLineAtOffset(25, 725);

	      String text1 = "Flight Ticket   :"+booking.getJourneydate();
	      String text2 = "PNR number      :"+booking.getPnrNumber();
	      String text3 = "Flight Number    :"+responseEntity.getBody();
	      String text4 = "From Place      :"+booking.getFrom();
	      String text5 = "To Place        :"+booking.getTo();
	      String text6 = "Departure Time  :"+booking.getDepartureTime();
	      String text7 = "Arrival Time    :"+booking.getArrivalTime();
	      contentStream. showText(text1);
	      contentStream.newLine();
	      contentStream. showText(text2);
	      contentStream.newLine();
	      contentStream. showText(text3);
	      contentStream.newLine();
	      contentStream. showText(text4);
	      contentStream.newLine();
	      contentStream. showText(text5);
	      contentStream.newLine();
	      contentStream. showText(text6);
	      contentStream.newLine();
	      contentStream. showText(text7);
	      contentStream.endText();
		contentStream.close();
		document.save(file);
		document.close();
		
		return file;
	}
	
	public List<Booking> getAllBookings(){
		return bookingRepository.findAll();
	}
	
	public List<TicketDTO> findBookingHistory(String email) throws CustomException {

		List<TicketDTO> listOfTickets = new ArrayList<>();
		List<Booking> bookedTicketsByEmail = bookingRepository.getTicketsHistoryByEmail(email);
		if (bookedTicketsByEmail.isEmpty()) {
			throw new CustomException("No tickets found");
		}
		for (Booking booking : bookedTicketsByEmail) {

			Map<String, Integer> urlParams = new HashMap<>();
			urlParams.put("id", booking.getFlightId());
			URI uri = UriComponentsBuilder.fromUriString("http://localhost:8001/api/admin/findAeroplaneNumberById/{id}")
					.buildAndExpand(urlParams).toUri();
			uri = UriComponentsBuilder.fromUri(uri).build().toUri();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
					new ParameterizedTypeReference<String>() {
					});

			TicketDTO dto = new TicketDTO();
			dto.setAirlineId(booking.getAirlineId());
			dto.setArrivalTime(booking.getArrivalTime());
			dto.setClassType(booking.getIsBusinessClass() == 1 ? "Business Class" : "Economy Class");
			dto.setDepartureTime(booking.getDepartureTime());
			dto.setEmail(booking.getEmail());
			dto.setFlightId(booking.getFlightId());
			dto.setFlightNumber(responseEntity.getBody());
			dto.setFrom(booking.getFrom());
			dto.setId(booking.getId());
			dto.setJourneydate(booking.getJourneydate());
			dto.setTo(booking.getTo());
			dto.setPnrNumber(booking.getPnrNumber());
			dto.setStatus(booking.isActive() == true ? "Booked" : "Cancelled");

			listOfTickets.add(dto);
		}
		return listOfTickets;
	}


}
