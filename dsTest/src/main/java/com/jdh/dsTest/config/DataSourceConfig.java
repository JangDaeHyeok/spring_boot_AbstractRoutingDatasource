package com.jdh.dsTest.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

	private static final String MASTER_DATASOURCE = "masterDataSource";
	private static final String SLAVE_DATASOURCE = "slaveDataSource";

	// mater database DataSource
	@Bean(MASTER_DATASOURCE)
	@ConfigurationProperties(prefix = "spring.datasource.master.hikari")
	public DataSource masterDataSource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

	// slave database DataSource
	@Bean(SLAVE_DATASOURCE)
	@ConfigurationProperties(prefix = "spring.datasource.slave.hikari")
	public DataSource slaveDataSource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

	// routing dataSource Bean
	@Bean
	@DependsOn({MASTER_DATASOURCE, SLAVE_DATASOURCE})
	public DataSource routingDataSource (
			@Qualifier(MASTER_DATASOURCE) DataSource masterDataSource,
			@Qualifier(SLAVE_DATASOURCE) DataSource slaveDataSource) {

		RoutingDatasource routingDatasource = new RoutingDatasource();

		Map<Object, Object> dataSourceMap = new HashMap<Object, Object>() {
			{
				put("master", masterDataSource);
				put("slave", slaveDataSource);
			}
		};

		// dataSource Map ??????
		routingDatasource.setTargetDataSources(dataSourceMap);
		// default DataSource??? master??? ??????
		routingDatasource.setDefaultTargetDataSource(masterDataSource);

		return routingDatasource;
	}

	// setting lazy connection
	@Bean
	@Primary
	@DependsOn("routingDataSource")
	public LazyConnectionDataSourceProxy dataSource(DataSource routingDataSource) {
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}
	
	// SqlSessionTemplate ?????? ????????? SqlSession ??? ???????????? Factory
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		/*
		 * MyBatis ??? JdbcTemplate ?????? Connection ????????? ?????? ????????? ????????? SqlSession ??? ????????????.
		 * ??????????????? SqlSessionTemplate ??? SqlSession ??? ????????????.
		 * Thread-Safe ?????? ?????? ?????? Mapper ?????? ????????? ??? ??????.
		 */
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		
		// MyBatis Mapper Source
		// MyBatis ??? SqlSession ?????? ????????? ?????? ??????
		Resource[] res = new PathMatchingResourcePatternResolver().getResources("classpath:mappers/*Mapper.xml");
		bean.setMapperLocations(res);
		
		// MyBatis Config Setting
		// MyBatis ?????? ??????
		Resource myBatisConfig = new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml");
		bean.setConfigLocation(myBatisConfig);
		
		return bean.getObject();
	}
	
	// DataSource ?????? Transaction ????????? ?????? Manager ????????? ??????
	@Bean
	public DataSourceTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
