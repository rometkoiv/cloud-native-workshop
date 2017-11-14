package com.github.rometkoiv.pgbroker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;


@Repository
class PostgresClient {
	private static final Logger log = LoggerFactory.getLogger(PostgresClient.class);
    private JdbcTemplate jdbcTemplate;

    PostgresClient(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    String createDatabase() {
        String db = getRandomId();
        jdbcTemplate.execute("CREATE DATABASE " + db);
        log.info("Database: " + db + " created successfully...");
        return db;
    }

    void deleteDatabase(String db) {
        jdbcTemplate.execute("DROP DATABASE IF EXISTS " + db);
        log.info("Database: " + db + " deleted successfully...");
    }

    boolean checkDatabaseExists(String db) {
        return jdbcTemplate.queryForObject("SELECT count(*) from pg_database WHERE datname = ?", new Object[]{db}, Integer.class) > 0;
    }

    private String getRandomId() {
        String s= UUID.randomUUID().toString();
        return "s" + s.replaceAll("[^a-zA-Z0-9]", "");
    }
    
    Map<String, String> createUserCreds(ServiceBinding binding) {
        String db = binding.getParameters().get(PostgresConfig.POSTGRES_DB).toString();
        Map<String, String> userCredentials = new HashMap<String, String>();
        
        userCredentials.put(PostgresConfig.POSTGRES_USER, getRandomId());
        userCredentials.put(PostgresConfig.POSTGRES_PASSWORD, getRandomId());
        userCredentials.put(PostgresConfig.POSTGRES_DB, db);
        log.debug("creds: " + userCredentials.toString());

        jdbcTemplate.execute("CREATE USER " + userCredentials.get(PostgresConfig.POSTGRES_USER) + " WITH PASSWORD '" + userCredentials.get(PostgresConfig.POSTGRES_PASSWORD) + "'");
        jdbcTemplate.execute("GRANT ALL PRIVILEGES ON DATABASE " + userCredentials.get(PostgresConfig.POSTGRES_DB) + " to " + userCredentials.get(PostgresConfig.POSTGRES_USER));

        log.info("Created user: " + userCredentials.get(PostgresConfig.POSTGRES_USER));
        return userCredentials;
    }

    void deleteUserCreds(String uid) {
        jdbcTemplate.execute("DROP USER IF EXISTS " + uid);
    }
    

}

