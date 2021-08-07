package com.ey.in.tds.ingestion.web.rest.batchupload;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.ingestion.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc()
public class BatchUploadControllerTest {

	@MockBean
	BatchUploadDAO batchUploadRepository;
	
	@MockBean
	BatchUploadDAO batchUploadDAO;
	
	BatchUpload batchUpload;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void beforeEach() {
		batchUpload = TestUtil.getRandomBatchUpload();
		Mockito.when(batchUploadDAO.save(batchUpload)).thenReturn(batchUpload);
	}

	@Test
	public void createBatchUpload() throws Exception {
		this.mockMvc
				.perform(post("/api/ingestion/batchupload").headers(TestUtil.getMandatoryHeaders())
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(batchUpload)))
				.andDo(print()).andExpect(status().isOk());
		assertTrue(true);
	}

}
