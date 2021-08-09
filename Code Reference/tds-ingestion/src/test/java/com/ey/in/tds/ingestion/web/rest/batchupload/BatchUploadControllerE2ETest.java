package com.ey.in.tds.ingestion.web.rest.batchupload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.util.TestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BatchUploadControllerE2ETest {

	TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void createBatchUpload() {

		BatchUpload batchUpload = TestUtil.getRandomBatchUpload();

		createAndAssert(batchUpload);

	}

	private BatchUpload createAndAssert(BatchUpload batchUpload) {
		// While random value can be used in other tests, the REST test should not pass
		// id values.
		// Its the server that will generate it
		batchUpload.setBatchUploadID(null);
		assertNull(batchUpload.getBatchUploadID());

		HttpEntity<BatchUpload> entity = new HttpEntity<>(batchUpload, TestUtil.getMandatoryHeaders());

		ResponseEntity<BatchUpload> response = restTemplate.exchange(TestUtil.getE2EURL("/batchupload"),
				HttpMethod.POST, entity, BatchUpload.class);

		assertEquals(200, response.getStatusCodeValue());

		BatchUpload created = response.getBody();

		assertNotNull(created.getBatchUploadID());
		return created;
	}

	private BatchUpload createAndAssert() {
		BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
		return createAndAssert(batchUpload);
	}

	@Test
	public void updateBatchUpload() {

		BatchUpload batchUpload = createAndAssert();

//    batchUpload.setFailedCount(1000);

		HttpEntity<BatchUpload> putEntity = new HttpEntity<BatchUpload>(batchUpload, TestUtil.getMandatoryHeaders());

		getUriComponentBuilder(batchUpload);

		ResponseEntity<BatchUpload> putResponse = restTemplate.exchange(TestUtil.getE2EURL("/batchupload"),
				HttpMethod.PUT, putEntity, BatchUpload.class);

		assertEquals(200, putResponse.getStatusCodeValue());

		BatchUpload created = putResponse.getBody();

		assertNotNull(created.getBatchUploadID());

//    assertEquals(1000, putResponse.getBody().getFailedCount());

	}

	private ResponseEntity<BatchUpload> getBatchUploadResponseEntity(BatchUpload batchUpload) {

		UriComponentsBuilder builder = getUriComponentBuilder(batchUpload);

		return restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<>(TestUtil.getMandatoryHeaders()), BatchUpload.class);
	}

	private UriComponentsBuilder getUriComponentBuilder(BatchUpload batchUpload) {
		return UriComponentsBuilder.fromHttpUrl(TestUtil.getE2EURL("/batchupload"))
				.queryParam("assessmentYear", batchUpload.getAssessmentYear()).queryParam("month", 10)
				.queryParam("type", batchUpload.getUploadType()).queryParam("status", "TEST")
				.queryParam("sha256sum", "sha256").queryParam("id", batchUpload.getBatchUploadID());
	}

	@Test
	public void getBatchUpload() {

		BatchUpload batchUpload = createAndAssert();

		ResponseEntity<BatchUpload> getResponse = getBatchUploadResponseEntity(batchUpload);

		assertEquals(200, getResponse.getStatusCodeValue());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void paginationTest() {
		List<BatchUpload> batchUploads = new LinkedList<>();
		BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
		for (int i = 0; i < 5; i++) {
			// Pass the same batch upload and create 5 with different "id"s
			batchUploads.add(createAndAssert(batchUpload));
		}

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(TestUtil.getE2EURL("/paginationTest"))
				.queryParam("assessmentYear", batchUpload.getAssessmentYear())
				.queryParam("type", batchUpload.getUploadType());

		int pageSize = 2;

		ResponseEntity<?> firstPageResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(new Pagination(false, 2, null), TestUtil.getMandatoryHeaders()), ApiStatus.class);
		assertEquals(200, firstPageResponse.getStatusCodeValue());
		Map<?, ?> firstPagedData = (Map<?, ?>) ((ApiStatus<?>) firstPageResponse.getBody()).getData();
		List<?> firstPageRows = (List<?>) firstPagedData.get("data");
		assertEquals("First page should contain two rows", 2, firstPageRows.size());
		assertEquals("First page size should be 2", 2,
				Integer.valueOf((String) firstPagedData.get("pageSize")).intValue());
		assertEquals("First page number should be 1", 1,
				Integer.valueOf((String) firstPagedData.get("pageNumber")).intValue());
		List<String> firstPageStates = (List<String>) firstPagedData.get("pageStates");
		assertEquals("First page states should be 2", 2, firstPageStates.size());
		List<String> firstPageIds = new LinkedList<>();
		firstPageRows.stream().forEach(map -> firstPageIds
				.add((String) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) map).get("key")).get("id")));

		// Second page
		ResponseEntity<?> secondPageResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(new Pagination(true, pageSize, firstPageStates), TestUtil.getMandatoryHeaders()),
				ApiStatus.class);
		assertEquals(200, secondPageResponse.getStatusCodeValue());
		Map<?, ?> secondPagedData = (Map<?, ?>) ((ApiStatus<?>) secondPageResponse.getBody()).getData();
		List<?> secondPageRows = (List<?>) secondPagedData.get("data");
		assertEquals("Second page should contain two rows", 2, secondPageRows.size());
		assertEquals("Second page size should be 2", 2,
				Integer.valueOf((String) firstPagedData.get("pageSize")).intValue());
		assertEquals("Second page number should be 2", 2,
				Integer.valueOf((String) firstPagedData.get("pageNumber")).intValue());
		List<String> secondPageStates = (List<String>) secondPagedData.get("pageStates");
		assertEquals("Second page states should be 3", 3, secondPageStates.size());
		List<String> secondPageIds = new LinkedList<>();
		secondPageRows.stream().forEach(map -> secondPageIds
				.add((String) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) map).get("key")).get("id")));

		assertTrue("Tow pages should not have same rows", Collections.disjoint(firstPageIds, secondPageIds));

		// First page
		ResponseEntity<?> firstPageResponse2 = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(new Pagination(false, pageSize, secondPageStates), TestUtil.getMandatoryHeaders()),
				ApiStatus.class);
		assertEquals(200, firstPageResponse2.getStatusCodeValue());
		Map<?, ?> firstPagedData2 = (Map<?, ?>) ((ApiStatus<?>) firstPageResponse2.getBody()).getData();
		List<?> firstPageRows2 = (List<?>) firstPagedData2.get("data");
		assertEquals("First page (second time) should contain two rows", 2, firstPageRows2.size());
		assertEquals("First page (second time) size should be 2", 2,
				(int) Integer.valueOf((String) firstPagedData.get("pageSize")).intValue());
		assertEquals("First page (second time) number should be 1", 1,
				Integer.valueOf((String) firstPagedData.get("pageNumber")).intValue());
		List<String> firstPageStates2 = (List<String>) firstPagedData2.get("pageStates");
		assertEquals("First page (second time) states should be 2", 2, firstPageStates2.size());

		List<String> firstPagedData2Ids = new LinkedList<>();
		firstPageRows2.stream().forEach(map -> firstPagedData2Ids
				.add((String) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) map).get("key")).get("id")));
		assertEquals(firstPagedData2, firstPagedData);

		// Second page
		ResponseEntity<?> secondPageResponse2 = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(new Pagination(true, pageSize, firstPageStates2), TestUtil.getMandatoryHeaders()),
				ApiStatus.class);
		assertEquals(200, secondPageResponse2.getStatusCodeValue());
		Map<?, ?> secondPagedData2 = (Map<?, ?>) ((ApiStatus<?>) secondPageResponse2.getBody()).getData();
		List<?> secondPageRows2 = (List<?>) secondPagedData2.get("data");
		assertEquals("Second page (second time) should contain two rows", 2, secondPageRows2.size());
		assertEquals("Second page (second time) size should be 2", 2, Integer.valueOf((String) firstPagedData.get("pageSize")).intValue());
		assertEquals("Second page (second time) number should be 1", 2, Integer.valueOf((String) firstPagedData.get("pageNumber")).intValue());
		List<String> secondPageStates2 = (List<String>) secondPagedData2.get("pageStates");
		assertEquals("Second page (second time) states should be 2", 3, secondPageStates2.size());

		List<String> secondPageIds2 = new LinkedList<>();
		secondPageRows2.stream().forEach(map -> secondPageIds2
				.add((String) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) map).get("key")).get("id")));
		assertEquals(secondPagedData2, secondPagedData);

		assertTrue("Tow pages should not have same rows", Collections.disjoint(firstPagedData2Ids, secondPageIds2));

		// Third page
		ResponseEntity<?> thirdPageResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(new Pagination(true, pageSize, secondPageStates2), TestUtil.getMandatoryHeaders()),
				ApiStatus.class);
		assertEquals(200, thirdPageResponse.getStatusCodeValue());
		Map<?, ?> thirdPagedData = (Map<?, ?>) ((ApiStatus<?>) thirdPageResponse.getBody()).getData();
		List<?> thirdPageRows = (List<?>) thirdPagedData.get("data");
		assertEquals("Third page should contain one row", 1, thirdPageRows.size());
		assertEquals("Third page size should be 2", 2, Integer.valueOf((String) firstPagedData.get("pageSize")).intValue());
		assertEquals("Third page number should be 3", 3, Integer.valueOf((String) firstPagedData.get("pageNumber")).intValue());
		assertEquals("Third page states should be 4", 4, ((List<?>) thirdPagedData.get("pageStates")).size());
		System.out.println(firstPagedData2Ids);
		System.out.println(secondPageIds2);
		System.out.println(thirdPageRows.get(0));
		List<String> thirdPageIds = new LinkedList<>();
		thirdPageRows.stream().forEach(map -> thirdPageIds
				.add((String) ((LinkedHashMap<?, ?>) ((LinkedHashMap<?, ?>) map).get("key")).get("id")));
		assertTrue("Tow pages should not have same rows", Collections.disjoint(firstPagedData2Ids, thirdPageIds));
		assertTrue("Tow pages should not have same rows", Collections.disjoint(secondPageIds2, thirdPageIds));

	}

	@Test
	public void deleteBatchUpload() {

		BatchUpload batchUpload = createAndAssert();

		HttpEntity<BatchUpload> deleteEntity = new HttpEntity<BatchUpload>(batchUpload, TestUtil.getMandatoryHeaders());

		UriComponentsBuilder builder = getUriComponentBuilder(batchUpload);

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE,
				deleteEntity, Void.class);

		assertEquals(200, deleteResponse.getStatusCodeValue());

	}

}
