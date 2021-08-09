package com.ey.in.tds.onboarding.web.rest.error;

/**
 * Base interface for statuses.
 */
public interface StatusType {

	/**
	 * Get the associated status code.
	 *
	 * @return the status code.
	 */
	int getStatusCode();

	/**
	 * Get the reason phrase.
	 *
	 * @return the reason phrase.
	 */
	String getReasonPhrase();

}
