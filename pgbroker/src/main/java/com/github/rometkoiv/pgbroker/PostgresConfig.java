package com.github.rometkoiv.pgbroker;

import javax.sql.DataSource;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.cloud.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ComponentScan(basePackages = {"com.github.rometkoiv.pgbroker", "io.pivotal.ecosystem.servicebroker"})
public class PostgresConfig {
	static final String POSTGRES_DB = "POSTGRES_DB";
    static final String POSTGRES_USER = "POSTGRES_USER";
    static final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
    static final String POSTGRES_HOST_KEY = "POSTGRES_HOST";
    static final String POSTGRES_PORT_KEY = "POSTGRES_PORT";
	
    static final String POSTGRES_URI = "postgresuri";
    static final String POSTGRES_URI_SCHEME = "postgresql";
	
    @Bean
    public DataSource datasource(Environment env) {
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setServerName(env.getProperty(POSTGRES_HOST_KEY));
        source.setDatabaseName(env.getProperty(POSTGRES_DB));
        source.setUser(env.getProperty(POSTGRES_USER));
        source.setPassword(env.getProperty(POSTGRES_PASSWORD));
        return source;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    public String dbUrl(Environment env) {
        return POSTGRES_URI_SCHEME + "://" + env.getProperty(POSTGRES_HOST_KEY) + ":" + Integer.parseInt(env.getProperty(POSTGRES_PORT_KEY));
    }

    @Bean
    public BrokerApiVersion brokerApiVersion() {
        return new BrokerApiVersion();
    }
}
