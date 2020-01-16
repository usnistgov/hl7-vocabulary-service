package gov.nist.hit.vs.bootstrap.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;

import gov.nist.healthcare.vcsms.service.impl.NISTVCSMSClientImpl;
import gov.nist.hit.vs.valueset.domain.CDCCode;
import gov.nist.hit.vs.valueset.domain.CDCValueset;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import gov.nist.hit.vs.valueset.service.CdcService;
import gov.nist.hit.vs.valueset.service.ValuesetService;

@Component
public class CDCCodesScheduler {
	@Autowired
	public CdcService cdcService;

	@Autowired
	NISTVCSMSClientImpl cdcClient;

	private static MongoOperations mongoOps = new MongoTemplate(
			new SimpleMongoDbFactory(new MongoClient(), "vocabulary-service"));

	@PostConstruct
	public void initCDCValuesetMetada() {
		CDCValuesetMetadata cvxMeta = mongoOps.findOne(
				Query.query(Criteria.where("packageUID").is("419ea9b7-9dd8-e911-a18e-0ecd7015b0a4")),
				CDCValuesetMetadata.class);
		if (cvxMeta == null) {
			CDCValuesetMetadata cvx = new CDCValuesetMetadata("419ea9b7-9dd8-e911-a18e-0ecd7015b0a4", "CVX",
					"CDC CVX Report", "2019-09-16T12:19:12.433");
			mongoOps.save(cvx);
		}

		CDCValuesetMetadata ndc_useMeta = mongoOps.findOne(
				Query.query(Criteria.where("packageUID").is("e5c09239-0ede-e911-a18f-0ecd7015b0a4")),
				CDCValuesetMetadata.class);
		if (ndc_useMeta == null) {
			CDCValuesetMetadata ndc_use = new CDCValuesetMetadata("e5c09239-0ede-e911-a18f-0ecd7015b0a4",
					"NDC_UNIT_OF_USE", "CDC NDC Unit of Use", "2019-09-23T10:27:09.85");
			mongoOps.save(ndc_use);
		}

		CDCValuesetMetadata ndc_saleMeta = mongoOps.findOne(
				Query.query(Criteria.where("packageUID").is("56037ed5-9dd8-e911-a18e-0ecd7015b0a4")),
				CDCValuesetMetadata.class);
		if (ndc_saleMeta == null) {
			CDCValuesetMetadata ndc_sale = new CDCValuesetMetadata("56037ed5-9dd8-e911-a18e-0ecd7015b0a4",
					"NDC_UNIT_OF_SALE", "CDC NDC Unit of Sale", "2019-09-23T10:14:56.893");
			mongoOps.save(ndc_sale);

		}

		CDCValuesetMetadata visMeta = mongoOps.findOne(
				Query.query(Criteria.where("packageUID").is("84209684-0cde-e911-a18f-0ecd7015b0a4")),
				CDCValuesetMetadata.class);
		if (visMeta == null) {
			CDCValuesetMetadata vis = new CDCValuesetMetadata("84209684-0cde-e911-a18f-0ecd7015b0a4", "VIS",
					"CDC VIS Barcode Lookup", "2019-09-23T10:14:56.893");
			mongoOps.save(vis);

		}

		CDCValuesetMetadata mvxMeta = mongoOps.findOne(
				Query.query(Criteria.where("packageUID").is("7fc180c2-9dd8-e911-a18e-0ecd7015b0a4")),
				CDCValuesetMetadata.class);
		if (mvxMeta == null) {
			CDCValuesetMetadata mvx = new CDCValuesetMetadata("7fc180c2-9dd8-e911-a18e-0ecd7015b0a4", "MVX",
					"CDC MVX Report", "2019-09-16T12:19:30.62");
			mongoOps.save(mvx);

		}

		try {
			updateCdcCodes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Scheduled(cron = "0 0 1 * * SAT")
//	@Scheduled(fixedDelay = 1000000000)
	public void updateCdcCodes() throws IOException {
		System.out.println("Updating CDC codes started at: " + new Date());
		List<CDCValuesetMetadata> cdcValuesetsMetadata = this.cdcService.getCdcValuesetsMetadata();
		for (CDCValuesetMetadata meta : cdcValuesetsMetadata) {
			System.out.println("Updating CDC codes for: " + meta.getName());
			String codes = cdcClient.getValuesetString(meta.getPackageUID());
			if (codes != null) {
				List<CDCCode> cdcCodes = this.cdcService.parseCodes(codes, meta.getPackageUID());
				System.out.println("Codes found: " + cdcCodes.size());
				CDCValueset cdcValueSet = this.cdcService.getCDCValuesetByMetaId(meta.getId());
				if (cdcValueSet == null) {
					// Create new valueset
					cdcValueSet = new CDCValueset();
					cdcValueSet.setMetadata(meta);
					cdcValueSet.setCdcCodes(cdcCodes);
					this.cdcService.createCDCValueset(cdcValueSet);
				} else {
					// Save valueset with new codes
					cdcValueSet.setCdcCodes(cdcCodes);
					this.cdcService.saveCDCValueset(cdcValueSet);
				}
			}
			System.out.println("No codes found");
		}
		System.out.println("Updating CDC codes finished at: " + new Date());
	}
}
