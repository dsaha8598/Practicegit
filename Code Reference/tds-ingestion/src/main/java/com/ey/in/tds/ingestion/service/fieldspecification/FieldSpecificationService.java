package com.ey.in.tds.ingestion.service.fieldspecification;

import org.springframework.stereotype.Service;

@Service
public class FieldSpecificationService {
	//TODO NEED TO DELETE AFTER 1  WEEK

/*	@Autowired
	FieldSpecificationRepository fieldSpecificationRepository;

	public FieldSpecification create(FieldSpecification FieldSpecification) {
		FieldSpecification.getKey().setId(UUID.randomUUID());
		return fieldSpecificationRepository.insert(FieldSpecification);
	}

	public FieldSpecification get(FieldSpecification.Key key) {
		Optional<FieldSpecification> response = fieldSpecificationRepository.findById(key);
		return response.orElseThrow(() -> new RuntimeException(
				"Did not find an FieldSpecification with the passed in criteria : " + key.toString()));
	}   */
}