package gov.nist.hit.vs.valueset.example;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;

import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;

public class Main {

	private static MongoOperations mongoOps = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "vocabulary-service"));

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CDCValuesetMetadata cvx = new CDCValuesetMetadata("419ea9b7-9dd8-e911-a18e-0ecd7015b0a4", "CVX", "CDC CVX Report",
				"2019-09-16T12:19:12.433");
		CDCValuesetMetadata ndc_use = new CDCValuesetMetadata("e5c09239-0ede-e911-a18f-0ecd7015b0a4", "NDC_UNIT_OF_USE", "CDC NDC Unit of Use",
				"2019-09-23T10:27:09.85");
		CDCValuesetMetadata ndc_sale = new CDCValuesetMetadata("56037ed5-9dd8-e911-a18e-0ecd7015b0a4", "NDC_UNIT_OF_SALE", "CDC NDC Unit of Sale",
				"2019-09-23T10:14:56.893");
		CDCValuesetMetadata vis = new CDCValuesetMetadata("84209684-0cde-e911-a18f-0ecd7015b0a4", "VIS", "CDC VIS Barcode Lookup",
				"2019-09-23T10:14:56.893");
		CDCValuesetMetadata mvx = new CDCValuesetMetadata("7fc180c2-9dd8-e911-a18e-0ecd7015b0a4", "MVX", "CDC MVX Report",
				"2019-09-16T12:19:30.62");
		mongoOps.save(cvx);
		mongoOps.save(ndc_use);
		mongoOps.save(ndc_sale);
		mongoOps.save(vis);
		mongoOps.save(mvx);
	}

}
