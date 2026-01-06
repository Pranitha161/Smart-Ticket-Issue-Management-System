package com.smartticket.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

class GlobalExceptionHandlerTest {
	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleValidationExceptions_returnsBadRequestWithErrors() {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
		bindingResult.addError(new FieldError("test", "field", "must not be blank"));
		WebExchangeBindException ex = new WebExchangeBindException(null, bindingResult);
		ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Bad Request", response.getBody().get("error"));
		assertTrue(((java.util.List<?>) response.getBody().get("errors")).contains("must not be blank"));
	}

	@Test
	void handleGenericException_returnsInternalServerError() {
		Exception ex = new Exception("Something went wrong");
		ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("Internal Server Error", response.getBody().get("error"));
		assertEquals("Something went wrong", response.getBody().get("message"));
	}

	@Test
	void handleRuntimeException_returnsBadRequest() {
		RuntimeException ex = new RuntimeException("Invalid input");
		ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Bad Request", response.getBody().get("error"));
		assertEquals("Invalid input", response.getBody().get("message"));
	}
}
