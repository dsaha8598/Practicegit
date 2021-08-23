package com.udajahaja.controller;

import java.awt.print.Book;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.udajahaja.dto.AeroplaneSaveDTO;
import com.udajahaja.dto.AirlineSaveDTO;
import com.udajahaja.dto.SearchDTO;
import com.udajahaja.dto.SearchedFlightDetails;
import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.entity.Coupons;
import com.udajahaja.entity.Scheduler;
import com.udajahaja.repository.AeroplaneRepository;
import com.udajahaja.repository.AirlineRepository;
import com.udajahaja.repository.SchedulerRepository;
import com.udajahaja.service.AdminService;

@RestController()
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	private AdminService service;
	
	@Autowired
	private SchedulerRepository schedulerRepository;
	
	@Autowired
	private AeroplaneRepository aeroplaneRepository;
	
	@Autowired
	private AirlineRepository repo;
	
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
	
	private static final String TOPIC = "udajahajaTopic";
	
	@PostMapping(path = "/save", consumes = "application/json")
	public ResponseEntity<AirlineSaveDTO> saveAirline(@RequestBody AirlineSaveDTO dto){
		service.save(dto);
		return null;
	}
	
	@GetMapping(path = "/findAirlines")
	public ResponseEntity<List<AirlineEntity>> findAairlines(){
		
		return new ResponseEntity<List<AirlineEntity>>(service.findAllAirlines(),HttpStatus.OK);

	}
	@GetMapping(path = "/findAirlinesById/{id}")
      public ResponseEntity<AirlineSaveDTO> findAairlinesById(@PathVariable("id") Long id){
		
		return new ResponseEntity<AirlineSaveDTO>(service.findAllAirlinesById(id),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findAirlineNames")
    public ResponseEntity<List<String>> findAairlineNames(){
		
		return new ResponseEntity<List<String>>(service.getallAirlineNames(),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findAeroplanesById/{id}")
    public ResponseEntity<List<AeroplaneSaveDTO>> findAeroplanesById(@PathVariable("id") Long id){
		
		return new ResponseEntity<List<AeroplaneSaveDTO>>(service.aeroplanesByairlineId(id),HttpStatus.OK);

	}
	
	@PostMapping(path = "/saveScheduler", consumes = "application/json")
	public ResponseEntity<String> saveAirline(@RequestBody Scheduler dto){
		service.saveScheduler(dto);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	
	@GetMapping(path = "/findScheduler/byAirlineId/{id}")
    public ResponseEntity<List<Scheduler>> getSchedulerByAirlineId(@PathVariable("id") int id){
		
		return new ResponseEntity<List<Scheduler>>(service.aeroplanesByairlineId(id),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findScheduler/byId/{id}")
    public ResponseEntity<Scheduler> getSchedulerById(@PathVariable("id") int id){
		
		return new ResponseEntity<Scheduler>(service.schedulerById(id),HttpStatus.OK);

	}
	
	@PostMapping(path = "/saveCoupon", consumes = "application/json")
	public ResponseEntity<String> saveCoupon(@RequestBody Coupons dto){
		service.saveCoupons(dto);
		return new ResponseEntity<String>("sucess",HttpStatus.OK);
	}
	
	@GetMapping(path = "/getAllCoupons")
    public ResponseEntity<List<Coupons>> findAllCoupons(){
		
		return new ResponseEntity<List<Coupons>>(service.getallCoupons(),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findCoupon/byId/{id}")
    public ResponseEntity<Coupons> getCouponById(@PathVariable("id") int id){
		
		return new ResponseEntity<Coupons>(service.couponById(id),HttpStatus.OK);

	}
	
	@PostMapping(path = "/find/flights")
	@Cacheable(value="flights")
    public ResponseEntity<List<SearchedFlightDetails>> findFlights(@RequestBody SearchDTO dto){
		return new ResponseEntity<List<SearchedFlightDetails>>(service.getSearchedFlights(dto),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findScheduler/byRoute")
    public ResponseEntity<List<Scheduler>> getSchedulerByPlaces(@RequestParam("from")String from,
    		@RequestParam("to")String to){
		
		return new ResponseEntity<List<Scheduler>>(schedulerRepository.getSchedulesByFromAndToPlace(from,to),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findAirlinesNameById")
    public ResponseEntity<String> getAirlineName(@RequestParam("id")int id
    		){
		
		return new ResponseEntity<String>(repo.findById(id).get().getAirlineName(),HttpStatus.OK);

	}
	
	@GetMapping(path = "/findAeroplaneNumberById/{id}")
    public ResponseEntity<String> getFlightNumber(@PathVariable("id")int id
    		){
		
		return new ResponseEntity<String>(aeroplaneRepository.findById(id).get().getAeroplaneNumber(),HttpStatus.OK);

	}
	
	@GetMapping("/test")
	public @ResponseBody ResponseEntity<String> test() {
		 return new ResponseEntity<String>("Hurray...admin....TEST WORKING",HttpStatus.OK);
	}
	
	@GetMapping("/blockAirline/{id}")
    public ResponseEntity<String> blockAirline(@PathVariable("id")Integer id) {

		System.out.println("Producing kafka topic");
        kafkaTemplate.send(TOPIC, id+"");

        return  new ResponseEntity<String>("Published successfully",HttpStatus.OK);
    }
	
	@GetMapping("/unblockAirline/{id}")
    public ResponseEntity<String> unBlockAirline(@PathVariable("id")Integer id) {
		service.unblockAirline(id);
        return  new ResponseEntity<String>("Published successfully",HttpStatus.OK);
    }
	
	
	
}
