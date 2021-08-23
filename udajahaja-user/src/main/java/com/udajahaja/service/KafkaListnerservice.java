package com.udajahaja.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.udajahaja.entity.Booking;
import com.udajahaja.repository.BookingRepository;

@Service
public class KafkaListnerservice {
	@Autowired
	private BookingRepository bookingRepository;

	private static final String TOPIC = "udajahajaTopic";

    @KafkaListener(topics = TOPIC, groupId="group_id", containerFactory = "userKafkaListenerFactory")
	public void cancelTickets(String id) {
        Integer identity=Integer.parseInt(id);
        List<Booking> listBooking = bookingRepository.deactivateRecordsByAirlineId(identity);
        for(Booking booking:listBooking) {
        	booking.setActive(false);
        	System.out.println("updating tickets");
        	bookingRepository.save(booking);
        }
    }
}
