package com.smartticket.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class FeignConfigTest {

	@Test
	void httpMessageConvertersBeanCreated() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(FeignConfig.class);

		HttpMessageConverters converters = context.getBean(HttpMessageConverters.class);
		assertNotNull(converters);

		boolean hasJackson = converters.getConverters().stream()
				.anyMatch(c -> c instanceof MappingJackson2HttpMessageConverter);

		assertTrue(hasJackson, "Should contain MappingJackson2HttpMessageConverter");

		context.close();
	}

	@Test
	void httpMessageConvertersReturnsExpectedConverter() {
		FeignConfig config = new FeignConfig();
		HttpMessageConverters converters = config.httpMessageConverters();

		assertNotNull(converters);
		HttpMessageConverter<?> converter = converters.getConverters().get(0);
		assertFalse(converter instanceof MappingJackson2HttpMessageConverter);
	}
}
