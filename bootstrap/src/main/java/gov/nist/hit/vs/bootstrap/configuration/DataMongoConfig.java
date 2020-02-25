package gov.nist.hit.vs.bootstrap.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

@Configuration
@EnableMongoRepositories(basePackages = { "gov.nist.hit.vs" })
@ComponentScan("gov.nist.hit.vs")
public class DataMongoConfig extends AbstractMongoConfiguration {

	@Autowired
	Environment env;

	private static final String DB_NAME = "db.name";
	private static final String DB_HOST = "db.host";
	private static final String DB_PORT = "db.port";

	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}

	@Override
	public MongoClient mongoClient() {
		System.out.println("********####**** Mongo properties: " + env.getProperty(DB_HOST) + " : " + env.getProperty(DB_PORT) );
		return new MongoClient(new ServerAddress(env.getProperty(DB_HOST), Integer.parseInt(env.getProperty(DB_PORT))));
	}

	@Override
	protected String getDatabaseName() {
		return env.getProperty(DB_NAME);
	}

	@Override
	protected String getMappingBasePackage() {
		return "gov.nist.hit.vs";
	}

}
