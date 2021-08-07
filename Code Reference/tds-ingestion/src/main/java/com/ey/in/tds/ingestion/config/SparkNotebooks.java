package com.ey.in.tds.ingestion.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spark")
public class SparkNotebooks {
	private Map<String, Notebook> notebooks = new HashMap<>();

	public Map<String, Notebook> getNotebooks() {
		return notebooks;
	}

	public static class Notebook {
		private int jobId;
		private String url;

		public int getJobId() {
			return jobId;
		}

		public void setJobId(int jobId) {
			this.jobId = jobId;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}
}
