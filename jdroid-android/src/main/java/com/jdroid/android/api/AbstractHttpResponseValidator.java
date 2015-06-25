package com.jdroid.android.api;

import com.jdroid.android.exception.CommonErrorCode;
import com.jdroid.java.exception.ConnectionException;
import com.jdroid.java.exception.ErrorCode;
import com.jdroid.java.exception.HttpResponseException;
import com.jdroid.java.http.HttpResponseWrapper;
import com.jdroid.java.http.HttpWebServiceProcessor;
import com.jdroid.java.http.WebService;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

public abstract class AbstractHttpResponseValidator implements HttpWebServiceProcessor {
	
	private final static Logger LOGGER = LoggerUtils.getLogger(AbstractHttpResponseValidator.class);
	
	private static final String STATUS_CODE_HEADER = "status-code";
	private static final String SUCCESSFUL_STATUS_CODE = "200";

	@Override
	public void onInit(WebService webService) {
		// Do Nothing
	}

	/**
	 * @see com.jdroid.java.http.HttpWebServiceProcessor#beforeExecute(com.jdroid.java.http.WebService)
	 */
	@Override
	public void beforeExecute(WebService webService) {
		// Do Nothing
		
	}
	
	/**
	 * @see com.jdroid.java.http.HttpWebServiceProcessor#afterExecute(com.jdroid.java.http.WebService,
	 *      com.jdroid.java.http.HttpResponseWrapper)
	 */
	@Override
	public void afterExecute(WebService webService, HttpResponseWrapper httpResponse) {
		// validate response.
		this.validateResponse(httpResponse);
	}
	
	/**
	 * Validate the response generated by the server.
	 * 
	 * @param httpResponse The HttpResponseWrapper
	 */
	protected void validateResponse(HttpResponseWrapper httpResponse) {
		
		String message = httpResponse.logStatusCode();
		if (httpResponse.isSuccess()) {
			ErrorCode errorCode = getErrorCode(httpResponse);
			if (errorCode != null) {
				throw errorCode.newErrorCodeException();
			}
		} else if (httpResponse.isClientError()) {
			ErrorCode errorCode = getErrorCode(httpResponse);
			if (errorCode != null) {
				throw errorCode.newErrorCodeException();
			} else {
				throw new HttpResponseException(message);
			}
		} else if (httpResponse.isServerError()) {
			// 504 - Gateway Timeout
			if (httpResponse.getStatusCode() == 504) {
				throw new ConnectionException(message);
			} else {
				throw new HttpResponseException(message);
			}
		} else {
			throw new HttpResponseException(message);
		}
	}
	
	private ErrorCode getErrorCode(HttpResponseWrapper httpResponse) {
		ErrorCode errorCode = null;
		String statusCode = httpResponse.getHeader(STATUS_CODE_HEADER);
		if (statusCode != null) {
			LOGGER.debug("Server Status code: " + statusCode);
			if (!statusCode.equals(SUCCESSFUL_STATUS_CODE)) {
				errorCode = findByStatusCode(statusCode);
				if (errorCode == null) {
					errorCode = CommonErrorCode.findByStatusCode(statusCode);
					if (errorCode == null) {
						LOGGER.warn("Unknown Server Status code: " + statusCode);
						throw new HttpResponseException("Unknown Server Status code: " + statusCode);
					}
				}
			}
		}
		return errorCode;
	}
	
	protected abstract ErrorCode findByStatusCode(String statusCode);
}