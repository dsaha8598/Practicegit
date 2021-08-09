package com.ey.in.tds.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.repository.FilingDeductorCollectorRepository;

@Service
public class FilingDeductorCollectorService {

	@Autowired
	private FilingDeductorCollectorRepository filingDeductorCollectorRepository;

	/**
	 * This method for create Deductor Collector
	 * 
	 * @param deductorCollector
	 * @param userName
	 * @return
	 */
	public FilingDeductorCollector saveDeductorCollector(@Valid FilingDeductorCollector deductorCollector,
			String userName) {
		deductorCollector.setActive(true);
		deductorCollector.setCreatedBy(userName);
		deductorCollector.setCreatedDate(Instant.now());
		return filingDeductorCollectorRepository.save(deductorCollector);
	}

	/**
	 * This method for get all Deductor Collector.
	 * 
	 * @return
	 */
	public List<FilingDeductorCollector> getAllDeductorCollector() {

		return filingDeductorCollectorRepository.findAll();
	}

	/**
	 * This method for get Deductor Collector Based on Id
	 * 
	 * @param id
	 * @return
	 */
	public FilingDeductorCollector getByDeductorCollectorId(Long id) {
		Optional<FilingDeductorCollector> filingDeductorCollectorObj = filingDeductorCollectorRepository.findById(id);
		if (filingDeductorCollectorObj.isPresent()) {
			return filingDeductorCollectorObj.get();
		}
		return new FilingDeductorCollector();
	}
	
	/**
	 * to get catagory value
	 * @param catagoryDescription
	 * @return
	 */
	public String getCatagoryValue(String catagoryDescription) {
		return filingDeductorCollectorRepository.getCatagoryValueByDescriptions(catagoryDescription);
	}
	
	public String getCategoryDescription(String categoryValue) {
		return filingDeductorCollectorRepository.getCategoryDescriptionByValue(categoryValue);
	}
}
