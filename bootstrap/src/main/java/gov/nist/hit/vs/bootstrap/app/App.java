package gov.nist.hit.vs.bootstrap.app;

import java.net.MalformedURLException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.caucho.hessian.client.HessianProxyFactory;
import com.mongodb.MongoClient;

import ca.uhn.fhir.context.FhirContext;
import gov.cdc.vocab.service.VocabService;
import gov.nist.healthcare.vcsms.domain.ClientConfiguration;
import gov.nist.healthcare.vcsms.domain.RESTClientInfo;
import gov.nist.healthcare.vcsms.service.impl.NISTVCSMSClientImpl;
import gov.nist.hit.vs.bootstrap.configuration.SpringBootConfiguration;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({ "gov.nist.hit.vs" })
@Configuration
@EnableSwagger2
@EnableScheduling
public class App implements CommandLineRunner {
	static String phinvadsUrl = "https://phinvads.cdc.gov/vocabService/v2";


	@Autowired
	SpringBootConfiguration config;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);

	}

	@Bean()
	public FhirContext fhirR4Context() {
		return FhirContext.forR4();
	}

	@Bean()
	public VocabService vocabService() {
		HessianProxyFactory factory = new HessianProxyFactory();
		try {
			return (VocabService) factory.create(VocabService.class, phinvadsUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Bean()
	public NISTVCSMSClientImpl cdcClient() {

		NISTVCSMSClientImpl client = new NISTVCSMSClientImpl(
				new ClientConfiguration(config.getProtocol(), config.getRoot(), config.getGroupMnemonic(),
						config.getNodeID(), config.getFtpHost(), config.getFtpUserName(), config.getFtpUserPassword()),
				new RESTClientInfo("1.0.0", "TEST Client"), "/Users/inm1/Desktop/CDCCodes1");
		return client;
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub

	}
}
