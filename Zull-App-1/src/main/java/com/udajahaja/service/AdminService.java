package com.udajahaja.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.udajahaja.dto.AeroplaneSaveDTO;
import com.udajahaja.dto.AirlineSaveDTO;
import com.udajahaja.dto.BookingDTO;
import com.udajahaja.dto.SearchDTO;
import com.udajahaja.dto.SearchedFlightDetails;
import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.entity.Coupons;
import com.udajahaja.entity.Scheduler;
import com.udajahaja.repository.AeroplaneRepository;
import com.udajahaja.repository.AirlineRepository;
import com.udajahaja.repository.CouponRepository;
import com.udajahaja.repository.SchedulerRepository;

@Service
public class AdminService {

	@Autowired
	private AirlineRepository repo;
	@Autowired
	private AeroplaneRepository aeroplaneRepository;
	@Autowired
	private SchedulerRepository schedulerRepository;
	@Autowired
	private CouponRepository ccouponRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private RestTemplate restTemplate;
	
	public void save(AirlineSaveDTO dto) {
		AirlineEntity entity=new AirlineEntity();
		entity.setActive(true);
		entity.setAirlineName(dto.getAirlineName());
		entity.setContactAddress(dto.getContactAddress());
		entity.setContactNumber(dto.getContactNumber());
		entity=repo.save(entity);
		for(AeroplaneSaveDTO aeroplaneSaveDTO:dto.getAeroplanes()) {
			AeroplaneEntity aeroplaneEntity=new AeroplaneEntity();
			aeroplaneEntity.setActive(1);
			aeroplaneEntity.setAeroplaneNumber(aeroplaneSaveDTO.getAeroplaneNumber());
			aeroplaneEntity.setAirlineId((long)entity.getId());
			aeroplaneEntity.setBusinessClassCount(aeroplaneSaveDTO.getBusinessClassCount());
			aeroplaneEntity.setEconomyClassCount(aeroplaneSaveDTO.getEconomyClassCount());
			aeroplaneEntity.setStartDate(aeroplaneSaveDTO.getStartDate());
			aeroplaneEntity.setEndDate(aeroplaneSaveDTO.getEndDate());
			aeroplaneRepository.save(aeroplaneEntity);
		}
		System.out.println("Saved id--->"+entity.getId());
	}
	public List<AirlineEntity> findAllAirlines() {
		return repo.findAll();
	}
	public AirlineSaveDTO findAllAirlinesById(Long id) {
		 AirlineEntity entity=repo.findByAirlineId(id);
		 AirlineSaveDTO dto=new AirlineSaveDTO();
		 dto.setAirlineLogo(null);
		 dto.setAirlineName(entity.getAirlineName());
		 dto.setContactAddress(entity.getContactAddress());
		 dto.setContactNumber(entity.getContactNumber());
		 dto.setId(entity.getId());
		 //dto.set
		 
		 List<AeroplaneSaveDTO> list=new ArrayList<>();
		 List<AeroplaneEntity> listAeroplaneEntity=aeroplaneRepository.getAeroplaneByAirlineId(id);
		 for(AeroplaneEntity aeroplaneEntity:listAeroplaneEntity) {
			 AeroplaneSaveDTO aeroplaneSaveDTO=new AeroplaneSaveDTO();
			 aeroplaneSaveDTO.setAeroplaneNumber(aeroplaneEntity.getAeroplaneNumber());
			 aeroplaneSaveDTO.setBusinessClassCount(aeroplaneEntity.getBusinessClassCount());
			 aeroplaneSaveDTO.setEconomyClassCount(aeroplaneEntity.getEconomyClassCount());
			 aeroplaneSaveDTO.setEndDate(aeroplaneEntity.getEndDate());
			 aeroplaneSaveDTO.setStartDate(aeroplaneEntity.getStartDate());
			 aeroplaneSaveDTO.setId(aeroplaneEntity.getId());
			 list.add(aeroplaneSaveDTO);
		 }
		 dto.setAeroplanes(list);
		 return dto;
	}
	public List<String> getallAirlineNames(){
		return repo.findAllAirlineName();
	}
	
	public List<AeroplaneSaveDTO> aeroplanesByairlineId(long id) {
		List<AeroplaneSaveDTO> list=new ArrayList<>();
		 List<AeroplaneEntity> listAeroplaneEntity=aeroplaneRepository.getAeroplaneByAirlineId(id);
		 for(AeroplaneEntity aeroplaneEntity:listAeroplaneEntity) {
			 AeroplaneSaveDTO aeroplaneSaveDTO=new AeroplaneSaveDTO();
			 aeroplaneSaveDTO.setAeroplaneNumber(aeroplaneEntity.getAeroplaneNumber());
			 aeroplaneSaveDTO.setBusinessClassCount(aeroplaneEntity.getBusinessClassCount());
			 aeroplaneSaveDTO.setEconomyClassCount(aeroplaneEntity.getEconomyClassCount());
			 aeroplaneSaveDTO.setEndDate(aeroplaneEntity.getEndDate());
			 aeroplaneSaveDTO.setStartDate(aeroplaneEntity.getStartDate());
			 aeroplaneSaveDTO.setId(aeroplaneEntity.getId());
			 list.add(aeroplaneSaveDTO);
		 }
		 return list;
	}
	
	public void saveScheduler(Scheduler entity) {
		entity=schedulerRepository.save(entity);
	}
	
	public List<Scheduler> aeroplanesByairlineId(int id) {
		return schedulerRepository.getSchedulesByAirlineId(id);
	}
	
	public Scheduler schedulerById(int id) {
		return schedulerRepository.findById(id).get();
	}
	
	public void saveCoupons(Coupons coupon) {
		coupon=ccouponRepository.save(coupon);
		System.out.println("coupn saved with id=" +coupon.getId());
	}
	public List<Coupons> getallCoupons(){
		return ccouponRepository.findAll();
	}
	public Coupons couponById(int id) {
		return ccouponRepository.findById(id).get();
	}
	
	public List<SearchedFlightDetails> getSearchedFlights(SearchDTO dto) {
		List<Scheduler> schedulerList= schedulerRepository.getSchedulesByFromAndToPlace(dto.getFrom(), dto.getTo());
		List<SearchedFlightDetails> searchedFlightList=new ArrayList<>();
		List<Scheduler> returnFlightList=new ArrayList<>();
		if(dto.getToDate()!=null) {
			returnFlightList=schedulerRepository.getSchedulesByFromAndToPlace( dto.getTo(),dto.getFrom());
		}
		String name="";
		if(!schedulerList.isEmpty()) {
			name=repo.findById(schedulerList.get(0).getId()).get().getAirlineName();
		}
		for(Scheduler scheduler:schedulerList) {
			SearchedFlightDetails searchedFlightDetails=new SearchedFlightDetails();
			searchedFlightDetails.setAirlineId(scheduler.getAirlineId());
			searchedFlightDetails.setAirlineName(name);
			searchedFlightDetails.setArrivalTime(scheduler.getArrivalTime());
			searchedFlightDetails.setBusinessTicketCost(scheduler.getBusinessTicketCost());
			searchedFlightDetails.setDepartureTime(scheduler.getDepartureTime());
			searchedFlightDetails.setEconomyTicketCost(scheduler.getEconomyTicketCost());
			searchedFlightDetails.setFlightId(scheduler.getFlightId());
			//searchedFlightDetails.setFlightName(scheduler.get);
			searchedFlightDetails.setFromPlace(scheduler.getFromPlace());
			searchedFlightDetails.setId(scheduler.getId());
			searchedFlightDetails.setToPlace(scheduler.getToPlace());
			searchedFlightDetails.setIsReturn("No");
			searchedFlightDetails.setStartDate(dto.getFromDate());
			searchedFlightList.add(searchedFlightDetails);
		}
		for(Scheduler scheduler:returnFlightList) {
			SearchedFlightDetails searchedFlightDetails=new SearchedFlightDetails();
			searchedFlightDetails.setAirlineId(scheduler.getAirlineId());
			searchedFlightDetails.setAirlineName(name);
			searchedFlightDetails.setArrivalTime(scheduler.getArrivalTime());
			searchedFlightDetails.setBusinessTicketCost(scheduler.getBusinessTicketCost());
			searchedFlightDetails.setDepartureTime(scheduler.getDepartureTime());
			searchedFlightDetails.setEconomyTicketCost(scheduler.getEconomyTicketCost());
			searchedFlightDetails.setFlightId(scheduler.getFlightId());
			//searchedFlightDetails.setFlightName(scheduler.get);
			searchedFlightDetails.setFromPlace(scheduler.getFromPlace());
			searchedFlightDetails.setId(scheduler.getId());
			searchedFlightDetails.setToPlace(scheduler.getToPlace());
			searchedFlightDetails.setIsReturn("Yes");
			searchedFlightDetails.setReturnDate(dto.getToDate());
			searchedFlightList.add(searchedFlightDetails);
		}
		return searchedFlightList;
	}
	
	public void unblockAirline(Integer id) {
		AirlineEntity airlineEntity = repo.findById(id).get();
		airlineEntity.setActive(true);
		repo.save(airlineEntity);
	}
	
	public File generateReport() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:templates/" + "ticket_report.xlsx");
		InputStream input = resource.getInputStream();
		List<BookingDTO> bookingList=null;
		
		URI uri = UriComponentsBuilder.fromUriString("http://localhost:8002/api/user/findAll/bookings")
				.buildAndExpand().toUri();
		uri = UriComponentsBuilder.fromUri(uri).build()
				.toUri();

		ResponseEntity<List<BookingDTO>> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<BookingDTO>>() {
				});
		bookingList = responseEntity.getBody();
		
		AtomicInteger integer=new AtomicInteger(1);
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet worksheet = workbook.getSheetAt(0);
			bookingList.stream().forEach(record->{
				SXSSFRow row = worksheet.createRow(integer.addAndGet(1));
				aceptTicketData(record, row);
			});
			File file = new File("Ticket_Summery" + System.currentTimeMillis() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.write(out);
			return file;
		}
		
		

	}
	
	private void aceptTicketData(BookingDTO bookingDTO, SXSSFRow row) {

		row.createCell(0).setCellValue("");
		row.createCell(1)
				.setCellValue("");
		row.createCell(2)
		.setCellValue(bookingDTO.getJourneydate());
		row.createCell(3)
		.setCellValue(bookingDTO.isActive()?"Booked":"Cancelled");
	}
	
}
