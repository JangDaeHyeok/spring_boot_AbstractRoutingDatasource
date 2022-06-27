package com.jdh.dsTest.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jdh.dsTest.model.service.TestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class TestController {
	@Autowired TestService service;
	
	@Test
	public void test() throws Exception {
		log.info(service.getTest());
	}
}
