package com.jdh.dsTest.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jdh.dsTest.model.dao.TestMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService {
	@Autowired TestMapper testMapper;

	@Transactional(readOnly = true)
	public String getTest() throws Exception {
		return testMapper.selectTest();
	}
}
