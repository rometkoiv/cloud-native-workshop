package com.github.rometkoiv.pgbroker;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.pivotal.ecosystem.servicebroker.model.LastOperation;
import io.pivotal.ecosystem.servicebroker.model.ServiceBinding;
import io.pivotal.ecosystem.servicebroker.model.ServiceInstance;
import io.pivotal.ecosystem.servicebroker.service.DefaultServiceImpl;

@Service
public class PostgresBroker extends DefaultServiceImpl {
	private static final Logger log = LoggerFactory.getLogger(PostgresBroker.class);
	private PostgresClient client;
	private String dbUrl;

	public PostgresBroker(PostgresClient client, String dbUrl) {
		super();
		this.client = client;
		this.dbUrl = dbUrl;
	}

	@Override
	public LastOperation createInstance(ServiceInstance instance) {
		log.info("creating database...");
		try {
			String db = client.createDatabase();
			log.info("database: " + db + " created.");
			instance.getParameters().put(PostgresConfig.POSTGRES_DB, db);
		} catch (Throwable t) {
			log.error("error creating database.", t);
			return new LastOperation(LastOperation.CREATE, LastOperation.FAILED, t.getMessage());
		}
		return new LastOperation(LastOperation.CREATE, LastOperation.SUCCEEDED, instance.getId() + " creating.");
	}

	@Override
	public LastOperation deleteInstance(ServiceInstance instance) {
		try {
			String db = instance.getParameters().get(PostgresConfig.POSTGRES_DB).toString();
			String user = instance.getParameters().get(PostgresConfig.POSTGRES_USER).toString();
			log.info("deleting database: " + db);
			client.deleteDatabase(db);
			log.info("********DELETED database: " + db);
			client.deleteUserCreds(user);
			log.info("********DELETED User creds: " + user);
		} catch (Throwable t) {
			log.error("error deleting database.", t);
			return new LastOperation(LastOperation.DELETE, LastOperation.FAILED, t.getMessage());
		}
		return new LastOperation(LastOperation.DELETE, LastOperation.SUCCEEDED, instance.getId() + " deleting.");
	}

	@Override
	public LastOperation updateInstance(ServiceInstance instance) {
		log.info("update not yet implemented");
		return new LastOperation(LastOperation.UPDATE, LastOperation.FAILED, instance.getId() + " updating.");
	}

	@Override
	public LastOperation createBinding(ServiceInstance instance, ServiceBinding binding) {
		String db = instance.getParameters().get(PostgresConfig.POSTGRES_DB).toString();
		binding.getParameters().put(PostgresConfig.POSTGRES_DB, db);

		Map<String, String> userCredentials = client.createUserCreds(binding);
		binding.getParameters().put(PostgresConfig.POSTGRES_USER, userCredentials.get(PostgresConfig.POSTGRES_USER));

		binding.getParameters().put(PostgresConfig.POSTGRES_PASSWORD,
				userCredentials.get(PostgresConfig.POSTGRES_PASSWORD));
		log.info("bound app: " + binding.getAppGuid() + " to database: " + db);
		return new LastOperation(LastOperation.BIND, LastOperation.SUCCEEDED, "bound.");
	}
	@Override
    public LastOperation deleteBinding(ServiceInstance instance, ServiceBinding binding) {
        log.info("unbinding app: " + binding.getAppGuid() + " from database: " + instance.getParameters().get(PostgresConfig.POSTGRES_DB));
        return new LastOperation(LastOperation.UNBIND, LastOperation.SUCCEEDED, "bound.");
    }
	@Override
    public Map<String, Object> getCredentials(ServiceInstance instance, ServiceBinding binding) {
        log.info("returning credentials.");

        Map<String, Object> m = new HashMap<String, Object>();
        m.put(PostgresConfig.POSTGRES_URI, dbUrl + "/" + binding.getParameters().get(PostgresConfig.POSTGRES_DB).toString());

        m.put(PostgresConfig.POSTGRES_USER, binding.getParameters().get(PostgresConfig.POSTGRES_USER));
        m.put(PostgresConfig.POSTGRES_PASSWORD, binding.getParameters().get(PostgresConfig.POSTGRES_PASSWORD));
        m.put(PostgresConfig.POSTGRES_DB, binding.getParameters().get(PostgresConfig.POSTGRES_DB));

        return m;
    }
    @Override
    public boolean isAsync() {
        return false;
    }




}