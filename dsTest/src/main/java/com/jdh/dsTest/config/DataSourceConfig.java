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

	// mater datavase DataSource
	@Bean(MASTER_DATASOURCE)
	@ConfigurationProperties(prefix = "spring.datasource.master.hikari")
	public DataSource masterDataSource() {
		return DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.build();
	}

	// slave datavase DataSource
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

		// dataSource Map 설정
		routingDatasource.setTargetDataSources(dataSourceMap);
		// default DataSource는 master로 설정
		routingDatasource.setDefaultTargetDataSource(masterDataSource);

		return routingDatasource;
	}

	// seting lazy connection
	@Bean
	@Primary
	@DependsOn("routingDataSource")
	public LazyConnectionDataSourceProxy dataSource(DataSource routingDataSource) {
		return new LazyConnectionDataSourceProxy(routingDataSource);
	}
	
	// SqlSessionTemplate에서 사용할 SqlSession을 생성하는 Factory
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		/*
		 * MyBatis는 JdbcTemplate 대신 Connection 객체를 통한 질의를 위해서 SqlSession을 사용한다.
		 * 내부적으로 SqlSessionTemplate가 SqlSession을 구현한다.
		 * Thread-Safe하고 여러 개의 Mapper에서 공유할 수 있다.
		 */
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		
		// MyBatis Mapper Source
		// MyBatis의 SqlSession에서 불러올 쿼리 정보
		Resource[] res = new PathMatchingResourcePatternResolver().getResources("classpath:mappers/*Mapper.xml");
		bean.setMapperLocations(res);
		
		// MyBatis Config Setting
		// MyBatis 설정 파일
		Resource myBatisConfig = new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml");
		bean.setConfigLocation(myBatisConfig);
		
		return bean.getObject();
	}
	
	// DataSource에서 Transaction 관리를 위한 Manager 클래스 등록
	@Bean
	public DataSourceTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
