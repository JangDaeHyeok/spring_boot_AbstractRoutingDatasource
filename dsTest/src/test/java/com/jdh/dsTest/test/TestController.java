package com.jdh.dsTest.test;

import com.jdh.dsTest.model.service.TestService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class TestController {
	Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	TestService service;
	
	@Test
	public void test() throws Exception {
		log.info("Transaction Read Only 미적용 :: {}", service.getTest());
		log.info("Transaction Read Only 적용 :: {}", service.getTestReadOnly());
	}
}
