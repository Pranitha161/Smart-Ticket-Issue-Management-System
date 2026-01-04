package com.smartticket.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(WebExchangeBindException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getDefaultMessage())
				.collect(Collectors.toList());

		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Bad Request");
		body.put("errors", errors);
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", "Internal Server Error");
		body.put("message", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Bad Request");
		body.put("message", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

}
