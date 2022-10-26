package gov.nist.hit.vs.valueset.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import gov.nist.healthcare.vcsms.service.impl.NISTVCSMSClientImpl;
import gov.nist.hit.vs.valueset.domain.AphlValueset;
import gov.nist.hit.vs.valueset.domain.CDCCode;
import gov.nist.hit.vs.valueset.domain.CDCValueset;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import gov.nist.hit.vs.valueset.domain.Code;
import gov.nist.hit.vs.valueset.domain.Hl7Valueset;
import gov.nist.hit.vs.valueset.domain.PhinvadsValueset;
import gov.nist.hit.vs.valueset.repository.CDCValusetMetadataRepository;
import gov.nist.hit.vs.valueset.repository.CDCValusetRepository;
import gov.nist.hit.vs.valueset.repository.PhinvadsValuesetRepository;
import gov.nist.hit.vs.valueset.service.ValuesetService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import gov.cdc.vocab.service.VocabService;
import gov.cdc.vocab.service.bean.CodeSystem;
import gov.cdc.vocab.service.bean.ValueSetVersion;
import gov.cdc.vocab.service.dto.input.CodeSystemSearchCriteriaDto;
import gov.cdc.vocab.service.dto.output.ValidateResultDto;
import gov.cdc.vocab.service.dto.output.ValueSetResultDto;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Service("valuesetService")
public class SimpleValuesetService implements ValuesetService {

	static String fhirUrl = "http://hl7.org/fhir/ValueSet/";
	static String phinvadsUrl = "https://phinvads.cdc.gov/vads/ViewValueSet.action?oid=";
	@Autowired
	FhirContext fhirR4Context;

	@Autowired
	VocabService vocabService;

	@Autowired
	private PhinvadsValuesetRepository phinvadsValuesetRepository;

	@Autowired
	private CDCValusetMetadataRepository cdcValusetMetadataRepository;

	@Autowired
	private CDCValusetRepository cdcValusetRepository;

	@Autowired
	NISTVCSMSClientImpl cdcClient;

	@Autowired
	MongoOperations mongoOps;

	@Autowired
	MongoTemplate mongoTemplate;
	
	AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();

	@Override
	public String getLatestValuesets(String source, String format) throws IOException {

		if (source.equalsIgnoreCase("fhir")) {
			return null;
		} else if (source.equalsIgnoreCase("phinvads")) {
			return getLatestPhinvadsValuesets(format);
		} else if (source.equalsIgnoreCase("cdc")) {
			return getLatestCDCValuesets(format);
		} else if (source.toLowerCase().startsWith("aphl")) {
			String[] vsInfo = source.toLowerCase().split("_");
			if (!vsInfo[1].isEmpty()) {
				return getLatestAphlValuesets(vsInfo[1].toLowerCase(), format);
			} else {
				return null;
			}
		} else if (source.equalsIgnoreCase("hl7")) {
			return getLatestHL7Valuesets(format);
		} else {
			return null;
		}
	}

	@Override
	public String getValueset(String source, String theValueSetIdentifier, String vid, String format, Boolean meta,
			Boolean expand) throws IOException {

		if (source.equalsIgnoreCase("fhir")) {
			return getFhirValueset(theValueSetIdentifier, format, meta, expand);
		} else if (source.equalsIgnoreCase("phinvads")) {
			return getPhinvadsValueset(theValueSetIdentifier, format, vid, meta, expand);
		} else if (source.equalsIgnoreCase("cdc")) {
			return getCDCValueset(theValueSetIdentifier, format, vid, meta, expand);
		} else if (source.toLowerCase().startsWith("aphl")) {
			String[] vsInfo = source.toLowerCase().split("_");
			if (!vsInfo[1].isEmpty()) {
				return getAphlValueset(vsInfo[1].toLowerCase(), theValueSetIdentifier, format, vid, meta, expand);
			} else {
				return null;
			}
		} else if (source.equalsIgnoreCase("hl7")) {
			return getHl7Valueset(theValueSetIdentifier, format, vid, meta, expand);
		} else {
			return null;
		}

	}

	@Override
	public String validateCode(String source, String theValueSetIdentifier, String vid, String theCode,
			String theSystem, String format) throws IOException {

		boolean haveCode = theCode != null && theCode.isEmpty() == false;
		if (!haveCode) {
			throw new InvalidRequestException("No code provided to validate");
		}

		if (source.equalsIgnoreCase("fhir")) {
			return validateFhirCode(theValueSetIdentifier, theCode, theSystem, format);
		} else if (source.equalsIgnoreCase("phinvads")) {
			return validatePhinvadsCode(theValueSetIdentifier, vid, theCode, theSystem, format);
		} else if (source.equalsIgnoreCase("cdc")) {
			return validateCDCCode(theValueSetIdentifier, vid, theCode, theSystem, format);
		} else if (source.toLowerCase().startsWith("aphl")) {
			String[] vsInfo = source.toLowerCase().split("_");
			if (!vsInfo[1].isEmpty()) {
				return validateAphlCode(vsInfo[1].toLowerCase(), theValueSetIdentifier, vid, theCode, theSystem,
						format);
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

	public String getLatestCDCValuesets(String format) throws IOException {
		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		// Query to get all valuesets with the latest version for each one
		Aggregation aggregation = newAggregation(CDCValueset.class,
				sort(Sort.Direction.DESC, "metadata").and(Sort.Direction.DESC, "version"),
				group("metadata").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
		AggregationResults<CDCValueset> result = mongoTemplate.aggregate(aggregation, "cdc-valueset",
				CDCValueset.class);
		List<CDCValueset> cdcValuesets = result.getMappedResults();

		Bundle bundle = new Bundle();
		if (cdcValuesets != null && cdcValuesets.size() > 0) {
			bundle.setTotal(cdcValuesets.size());
			for (CDCValueset vs : cdcValuesets) {
				ValueSet fhirVs = convertCDCToFhir(vs, false);
				bundle.addEntry().setResource(fhirVs);
			}
			String encoded = parser.encodeResourceToString(bundle);
			return encoded;
		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String getLatestHL7Valuesets(String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		// Query to get all valuesets with the latest version for each one
		Aggregation aggregation = newAggregation(Hl7Valueset.class,
				sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
				group("name").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
		AggregationResults<Hl7Valueset> result = mongoTemplate.aggregate(aggregation, "hl7-valueset",
				Hl7Valueset.class);
		List<Hl7Valueset> valuesets = result.getMappedResults();

		Bundle bundle = new Bundle();
		if (valuesets != null && valuesets.size() > 0) {
			bundle.setTotal(valuesets.size());
			for (Hl7Valueset vs : valuesets) {
				ValueSet fhirVs = convertHl7ToFhir(vs, false);

				bundle.addEntry().setResource(fhirVs);
			}
			String encoded = parser.encodeResourceToString(bundle);
			return encoded;
		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String getLatestPhinvadsValuesets(String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		// Query to get all valuesets with the latest version for each one
		Aggregation aggregation = newAggregation(PhinvadsValueset.class,
				sort(Sort.Direction.DESC, "oid").and(Sort.Direction.DESC, "version"),
				group("oid").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
		AggregationResults<PhinvadsValueset> result = mongoTemplate.aggregate(aggregation, "phinvads-valueset",
				PhinvadsValueset.class);
		List<PhinvadsValueset> valuesets = result.getMappedResults();

		Bundle bundle = new Bundle();
		if (valuesets != null && valuesets.size() > 0) {
			bundle.setTotal(valuesets.size());
			for (PhinvadsValueset vs : valuesets) {
				ValueSet fhirVs = convertPhinvadsToFhir(vs, false);

				bundle.addEntry().setResource(fhirVs);
			}
			String encoded = parser.encodeResourceToString(bundle);
			return encoded;
		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String getLatestAphlValuesets(String program, String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		// Query to get all valuesets with the latest version for each one
		Aggregation aggregation = newAggregation(AphlValueset.class, match(Criteria.where("program").is(program)),
				sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
				group("name", "program").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
		AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
				AphlValueset.class);
		List<AphlValueset> valuesets = result.getMappedResults();

		Bundle bundle = new Bundle();
		if (valuesets != null && valuesets.size() > 0) {
			bundle.setTotal(valuesets.size());
			for (AphlValueset vs : valuesets) {
				ValueSet fhirVs = convertAphlToFhir(vs, false);

				bundle.addEntry().setResource(fhirVs);
			}
			String encoded = parser.encodeResourceToString(bundle);
			return encoded;
		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String getCDCValueset(String theValueSetIdentifier, String format, String vid, Boolean meta, Boolean expand)
			throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);
		CDCValuesetMetadata metadata = cdcValusetMetadataRepository.findByName(theValueSetIdentifier);
		if (metadata != null) {
			CDCValueset cdcValueset = null;

			if (vid != null) {
				int version = Integer.parseInt(vid);

				Aggregation aggregation = newAggregation(CDCValueset.class,
						match(Criteria.where("metadata.$id").is(new ObjectId(metadata.getId()))
								.andOperator(Criteria.where("version").is(version))),
						sort(Sort.Direction.DESC, "metadata").and(Sort.Direction.DESC, "version"),
						group("metadata").first("version").as("version").first("$$ROOT").as("doc"),
						replaceRoot("$doc")).withOptions(options);
				AggregationResults<CDCValueset> result = mongoTemplate.aggregate(aggregation, "cdc-valueset",
						CDCValueset.class);
				cdcValueset = result.getUniqueMappedResult();

			} else {
				Aggregation aggregation = newAggregation(CDCValueset.class,
						match(Criteria.where("metadata.$id").is(new ObjectId(metadata.getId()))),
						sort(Sort.Direction.DESC, "metadata").and(Sort.Direction.DESC, "version"),
						group("metadata").first("version").as("version").first("$$ROOT").as("doc"),
						replaceRoot("$doc")).withOptions(options);
				AggregationResults<CDCValueset> result = mongoTemplate.aggregate(aggregation, "cdc-valueset",
						CDCValueset.class);
				cdcValueset = result.getUniqueMappedResult();
			}
			if (cdcValueset != null) {
				ValueSet fhirVs = convertCDCToFhir(cdcValueset, expand);
				if (meta) {
					Meta m = fhirVs.getMeta();
					Parameters parameters = new Parameters();
					parameters.addParameter().setName("return").setValue(m);
					String encoded = parser.encodeResourceToString(parameters);
					return encoded;
				}

				if (expand) {

				}
				String encoded = parser.encodeResourceToString(fhirVs);
				return encoded;
			} else {
				OperationOutcome oo = new OperationOutcome();
				oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
				String encoded = parser.encodeResourceToString(oo);
				return encoded;
			}
		}
		OperationOutcome oo = new OperationOutcome();
		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
		String encoded = parser.encodeResourceToString(oo);
		return encoded;

	}

	public String getPhinvadsValueset(String theValueSetIdentifier, String format, String vid, Boolean meta,
			Boolean expand) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		PhinvadsValueset vs = null;

		if (vid != null) {
			int version = Integer.parseInt(vid);
			Aggregation aggregation = newAggregation(PhinvadsValueset.class,
					match(Criteria.where("oid").is(theValueSetIdentifier)
							.andOperator(Criteria.where("version").is(version))),
					sort(Sort.Direction.DESC, "oid").and(Sort.Direction.DESC, "version"),
					group("oid").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<PhinvadsValueset> result = mongoTemplate.aggregate(aggregation, "phinvads-valueset",
					PhinvadsValueset.class);
			vs = result.getUniqueMappedResult();

		} else {
			Aggregation aggregation = newAggregation(PhinvadsValueset.class,
					match(Criteria.where("oid").is(theValueSetIdentifier)),
					sort(Sort.Direction.DESC, "oid").and(Sort.Direction.DESC, "version"),
					group("oid").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<PhinvadsValueset> result = mongoTemplate.aggregate(aggregation, "phinvads-valueset",
					PhinvadsValueset.class);
			vs = result.getUniqueMappedResult();
		}
		if (vs != null) {
			System.out.println("Successfully got the metadata from PHINVADS web service for " + theValueSetIdentifier);
			System.out.println(theValueSetIdentifier + " last updated date is " + vs.getUpdateDate().toString());
			ValueSet fhirVs = convertPhinvadsToFhir(vs, expand);
			if (meta) {
				Meta m = fhirVs.getMeta();
				Parameters parameters = new Parameters();
				parameters.addParameter().setName("return").setValue(m);
				String encoded = parser.encodeResourceToString(parameters);
				return encoded;
			}

			String encoded = parser.encodeResourceToString(fhirVs);
			return encoded;
		} else {
			System.out.println("Failed to get the metadata from PHINVADS web service for " + theValueSetIdentifier);
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}
	
	public String getHl7Valueset(String theValueSetIdentifier, String format, String vid, Boolean meta,
			Boolean expand) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		Hl7Valueset vs = null;

		if (vid != null) {
			int version = Integer.parseInt(vid);
			Aggregation aggregation = newAggregation(Hl7Valueset.class,
					match(Criteria.where("name").is(theValueSetIdentifier)
							.andOperator(Criteria.where("version").is(version))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<Hl7Valueset> result = mongoTemplate.aggregate(aggregation, "hl7-valueset",
					Hl7Valueset.class);
			vs = result.getUniqueMappedResult();

		} else {
			Aggregation aggregation = newAggregation(Hl7Valueset.class,
					match(Criteria.where("name").is(theValueSetIdentifier)),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<Hl7Valueset> result = mongoTemplate.aggregate(aggregation, "hl7-valueset",
					Hl7Valueset.class);
			vs = result.getUniqueMappedResult();
		}
		if (vs != null) {
			System.out.println("Successfully got the metadata from Hl7 web service for " + theValueSetIdentifier);
			ValueSet fhirVs = convertHl7ToFhir(vs, expand);
			if (meta) {
				Meta m = fhirVs.getMeta();
				Parameters parameters = new Parameters();
				parameters.addParameter().setName("return").setValue(m);
				String encoded = parser.encodeResourceToString(parameters);
				return encoded;
			}

			String encoded = parser.encodeResourceToString(fhirVs);
			return encoded;
		} else {
			System.out.println("Failed to get the metadata from PHINVADS web service for " + theValueSetIdentifier);
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String getAphlValueset(String program, String theValueSetIdentifier, String format, String vid, Boolean meta,
			Boolean expand) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		AphlValueset vs = null;

		if (vid != null) {
			int version = Integer.parseInt(vid);
			Aggregation aggregation = newAggregation(AphlValueset.class,
					match(Criteria.where("program").is(program)
							.andOperator(Criteria.where("name").is(theValueSetIdentifier))
							.andOperator(Criteria.where("version").is(version))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name", "program").first("version").as("version").first("$$ROOT").as("doc"),
					replaceRoot("$doc")).withOptions(options);
			AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
					AphlValueset.class);
			vs = result.getUniqueMappedResult();

		} else {
			Aggregation aggregation = newAggregation(AphlValueset.class,
					match(Criteria.where("program").is(program)
							.andOperator(Criteria.where("name").is(theValueSetIdentifier))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name", "program").first("version").as("version").first("$$ROOT").as("doc"),
					replaceRoot("$doc")).withOptions(options);
			AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
					AphlValueset.class);
			vs = result.getUniqueMappedResult();
		}
		if (vs != null) {
			System.out.println("Successfully got the metadata from PHINVADS web service for " + theValueSetIdentifier);
			System.out.println(theValueSetIdentifier + " last updated date is " + vs.getUpdateDate().toString());
			ValueSet fhirVs = convertAphlToFhir(vs, expand);
			if (meta) {
				Meta m = fhirVs.getMeta();
				Parameters parameters = new Parameters();
				parameters.addParameter().setName("return").setValue(m);
				String encoded = parser.encodeResourceToString(parameters);
				return encoded;
			}

			String encoded = parser.encodeResourceToString(fhirVs);
			return encoded;
		} else {
			System.out.println("Failed to get the metadata from APHL web service for " + theValueSetIdentifier);
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Not Found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public ValueSet convertPhinvadsToFhir(PhinvadsValueset vs, Boolean expand) {
		ValueSet fhirVs = new ValueSet();
		ValueSetExpansionComponent vsExpansion = new ValueSetExpansionComponent();
		if (expand) {
			if (!vs.getHasPartCodes()) {

				vsExpansion.setTotal(vs.getCodes().size());
				for (Code code : vs.getCodes()) {
					ValueSetExpansionContainsComponent contain = new ValueSetExpansionContainsComponent();
					contain.setCode(code.getValue());
					contain.setDisplay(code.getDescription());
					contain.setSystem(code.getCodeSystem());
					vsExpansion.addContains(contain);
				}
			}
		}
		fhirVs.setName(vs.getBindingIdentifier());
		fhirVs.setDescription(vs.getComment());
		fhirVs.setId(vs.getOid());
		fhirVs.setTitle(vs.getName());
		fhirVs.setUrl(phinvadsUrl.concat(vs.getOid()));
		fhirVs.setExpansion(vsExpansion);
		fhirVs.setVersion(String.valueOf(vs.getVersion()));
		Extension ext = new Extension("numberOfCodes", new PositiveIntType(vs.getNumberOfCodes()));
		fhirVs.getExtension().add(ext);
		return fhirVs;
	}

	public ValueSet convertCDCToFhir(CDCValueset vs, Boolean expand) {
		ValueSet fhirVs = new ValueSet();
		ValueSetExpansionComponent vsExpansion = new ValueSetExpansionComponent();
		if (expand) {
			vsExpansion.setTotal(vs.getCdcCodes().size());
			for (CDCCode code : vs.getCdcCodes()) {
				ValueSetExpansionContainsComponent contain = new ValueSetExpansionContainsComponent();
				contain.setCode(code.getCode());
				contain.setDisplay(code.getName());
//				contain.setSystem(code.getCodeSystem());
				vsExpansion.addContains(contain);
			}
		}

		fhirVs.setName(vs.getMetadata().getName());
		fhirVs.setId(vs.getMetadata().getPackageUID());
		fhirVs.setTitle(vs.getMetadata().getTitle());
		fhirVs.setVersion(String.valueOf(vs.getVersion()));
		fhirVs.setExpansion(vsExpansion);
		Extension ext = new Extension("numberOfCodes", new PositiveIntType(vs.getCdcCodes().size()));
		fhirVs.getExtension().add(ext);
		return fhirVs;
	}

	public ValueSet convertAphlToFhir(AphlValueset vs, Boolean expand) {
		ValueSet fhirVs = new ValueSet();
		ValueSetExpansionComponent vsExpansion = new ValueSetExpansionComponent();
		if (expand) {
			vsExpansion.setTotal(vs.getCodes().size());
			for (Code code : vs.getCodes()) {
				ValueSetExpansionContainsComponent contain = new ValueSetExpansionContainsComponent();
				contain.setCode(code.getValue());
				contain.setSystem(code.getCodeSystem());
				vsExpansion.addContains(contain);
			}
		}

		fhirVs.setName(vs.getName());
		fhirVs.setId(vs.getName());
		fhirVs.setVersion(String.valueOf(vs.getVersion()));
		fhirVs.setExpansion(vsExpansion);
		Extension ext = new Extension("numberOfCodes", new PositiveIntType(vs.getCodes().size()));
		fhirVs.getExtension().add(ext);
		return fhirVs;
	}

	public ValueSet convertHl7ToFhir(Hl7Valueset vs, Boolean expand) {
		ValueSet fhirVs = new ValueSet();
		ValueSetExpansionComponent vsExpansion = new ValueSetExpansionComponent();
		if (expand) {

			vsExpansion.setTotal(vs.getCodes().size());
			for (Code code : vs.getCodes()) {
				ValueSetExpansionContainsComponent contain = new ValueSetExpansionContainsComponent();
				contain.setCode(code.getValue());
				contain.setDisplay(code.getDisplay());
				contain.setSystem(code.getCodeSystem());

				Extension defExt = new Extension("definition", new StringType(code.getDefinition()));
				Extension statusExt = new Extension("v2TableStatus", new StringType(code.getV2TableStatus()));
				Extension depExt = new Extension("deprecated", new StringType(code.getDeprecated()));
				Extension conceptComExt = new Extension("v2ConceptComment", new StringType(code.getV2ConceptComment()));
				Extension conceptComPExt = new Extension("v2ConceptCommentAsPublished",
						new StringType(code.getV2ConceptCommentAsPublished()));
				Extension typeExt = new Extension("codeType", new StringType(code.getCodeType()));
				Extension regexExt = new Extension("regexRule", new StringType(code.getRegexRule()));
				Extension excludeExt = new Extension("exclude", new BooleanType(code.isExclude()));
				Extension commentsExt = new Extension("comments", new StringType(code.getComments()));

				contain.getExtension().add(defExt);
				contain.getExtension().add(statusExt);
				contain.getExtension().add(depExt);
				contain.getExtension().add(conceptComExt);
				contain.getExtension().add(conceptComPExt);
				contain.getExtension().add(typeExt);
				contain.getExtension().add(regexExt);
				contain.getExtension().add(excludeExt);
				contain.getExtension().add(commentsExt);

				vsExpansion.addContains(contain);
			}

		}
		fhirVs.setName(vs.getName());
		fhirVs.setId(vs.getName());
		fhirVs.setExpansion(vsExpansion);
		fhirVs.setVersion(vs.getHl7Version());
		Extension ext = new Extension("numberOfCodes", new PositiveIntType(vs.getNumberOfCodes()));
		fhirVs.getExtension().add(ext);
		return fhirVs;
	}

	public String getFhirValueset(String theValueSetIdentifier, String format, Boolean meta, Boolean expand)
			throws IOException {
		UriType url = new UriType();
		url.setValue(fhirUrl.concat(theValueSetIdentifier));
		OkHttpClient client = new OkHttpClient.Builder().build();
		Request request = new Request.Builder().url(url.getValue()).build();
		Call call = client.newCall(request);
		Response response = call.execute();
		IParser responseParser = fhirR4Context.newXmlParser();
		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);
		if (response.code() == 200) {
			ValueSet vs = responseParser.parseResource(ValueSet.class, response.body().string());
			if (meta) {
				Meta m = vs.getMeta();
				Parameters parameters = new Parameters();
				parameters.addParameter().setName("return").setValue(m);
				String encoded = parser.encodeResourceToString(parameters);
				return encoded;
			}
			String encoded = parser.encodeResourceToString(vs);
			return encoded;
		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics(response.message());
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}

	}

	public String validateCDCCode(String theValueSetIdentifier, String vid, String theCode, String theSystem,
			String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();

		CDCValuesetMetadata metadata = cdcValusetMetadataRepository.findByName(theValueSetIdentifier);
		CDCValueset cdcValueset = null;

		if (metadata != null) {

			if (vid != null) {
				int version = Integer.parseInt(vid);

				Aggregation aggregation = newAggregation(CDCValueset.class,
						match(Criteria.where("metadata.$id").is(new ObjectId(metadata.getId()))
								.andOperator(Criteria.where("version").is(version))),
						sort(Sort.Direction.DESC, "metadata").and(Sort.Direction.DESC, "version"),
						group("metadata").first("version").as("version").first("$$ROOT").as("doc"),
						replaceRoot("$doc")).withOptions(options);
				AggregationResults<CDCValueset> result = mongoTemplate.aggregate(aggregation, "cdc-valueset",
						CDCValueset.class);
				cdcValueset = result.getUniqueMappedResult();
			} else {
				Aggregation aggregation = newAggregation(CDCValueset.class,
						match(Criteria.where("metadata.$id").is(new ObjectId(metadata.getId()))),
						sort(Sort.Direction.DESC, "metadata").and(Sort.Direction.DESC, "version"),
						group("metadata").first("version").as("version").first("$$ROOT").as("doc"),
						replaceRoot("$doc")).withOptions(options);
				AggregationResults<CDCValueset> result = mongoTemplate.aggregate(aggregation, "cdc-valueset",
						CDCValueset.class);
				cdcValueset = result.getUniqueMappedResult();
			}
			if (cdcValueset != null) {
				Parameters retVal = new Parameters();
				List<CDCCode> codesFound = cdcValueset.getCdcCodes().stream()
						.filter(c -> c.getCode().toLowerCase().equals(theCode.toLowerCase()))
						.collect(Collectors.toList());
				if (theSystem == null || theSystem.isEmpty()) {
					retVal.addParameter().setName("result").setValue(new BooleanType(codesFound.size() > 0));
				} else {
					CDCCode codeFound = cdcValueset.getCdcCodes().stream()
							.filter(c -> c.getCode().toLowerCase().equals(theCode.toLowerCase())
									&& c.getSystem().toLowerCase().equals(theSystem.toLowerCase()))
							.findAny().orElse(null);
					retVal.addParameter().setName("result").setValue(new BooleanType(codeFound != null));
				}
				for (CDCCode code : codesFound) {
					retVal.addParameter().setName("codeSystem").setValue(new StringType(code.getSystem()));
				}

				String encoded = parser.encodeResourceToString(retVal);
				return encoded;
			}
		}

		OperationOutcome oo = new OperationOutcome();
		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Value set not found");
		String encoded = parser.encodeResourceToString(oo);
		return encoded;
	}

	public String validatePhinvadsCode(String theValueSetIdentifier, String vid, String theCode, String theSystem,
			String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();

		PhinvadsValueset vs = null;
		if (vid != null) {
			int version = Integer.parseInt(vid);
			Aggregation aggregation = newAggregation(PhinvadsValueset.class,
					match(Criteria.where("oid").is(theValueSetIdentifier)
							.andOperator(Criteria.where("version").is(version))),
					sort(Sort.Direction.DESC, "oid").and(Sort.Direction.DESC, "version"),
					group("oid").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<PhinvadsValueset> result = mongoTemplate.aggregate(aggregation, "phinvads-valueset",
					PhinvadsValueset.class);
			vs = result.getUniqueMappedResult();

		} else {
			Aggregation aggregation = newAggregation(PhinvadsValueset.class,
					match(Criteria.where("oid").is(theValueSetIdentifier)),
					sort(Sort.Direction.DESC, "oid").and(Sort.Direction.DESC, "version"),
					group("oid").first("version").as("version").first("$$ROOT").as("doc"), replaceRoot("$doc")).withOptions(options);
			AggregationResults<PhinvadsValueset> result = mongoTemplate.aggregate(aggregation, "phinvads-valueset",
					PhinvadsValueset.class);
			vs = result.getUniqueMappedResult();
		}
		if (vs != null) {

			if (vs.getHasPartCodes().equals(true)) {
				ValueSetResultDto vsSearchResult = vocabService.getValueSetByOid(theValueSetIdentifier);
				List<gov.cdc.vocab.service.bean.ValueSet> valueSets = vsSearchResult.getValueSets();
				if (valueSets != null && valueSets.size() > 0) {
					gov.cdc.vocab.service.bean.ValueSet phinvadsValueset = valueSets.get(0);
//					ValueSetVersion vsv = vocabService.getValueSetVersionsByValueSetOid(phinvadsValueset.getOid())
//							.getValueSetVersions().get(0);
					CodeSystemSearchCriteriaDto csSearchCritDto = new CodeSystemSearchCriteriaDto();
					csSearchCritDto.setCodeSearch(false);
					csSearchCritDto.setTable396Search(true);
					csSearchCritDto.setNameSearch(false);
					csSearchCritDto.setOidSearch(false);
					csSearchCritDto.setDefinitionSearch(false);
					csSearchCritDto.setAssigningAuthoritySearch(false);
					csSearchCritDto.setSearchType(1);
					csSearchCritDto.setSearchText(theSystem);
					List<CodeSystem> css = vocabService.findCodeSystems(csSearchCritDto, 1, 5).getCodeSystems();
					if (css != null && css.size() > 0) {
						CodeSystem cs = css.get(0);
						if (cs != null) {
							ValidateResultDto result = vocabService.validateConceptValueSetMembership(cs.getOid(),
									theCode, theValueSetIdentifier, vs.getVersion());
							Parameters retVal = new Parameters();
							retVal.addParameter().setName("result").setValue(new BooleanType(result.isValid()));
							String encoded = parser.encodeResourceToString(retVal);
							return encoded;
						}
					} else {
						OperationOutcome oo = new OperationOutcome();
						oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Code system not found");
						String encoded = parser.encodeResourceToString(oo);
						return encoded;
					}

				}
			} else {
				Set<Code> codes = vs.getCodes();
				Parameters retVal = new Parameters();
//				Code codeFound = codes.stream()
//						.filter(c -> c.getValue().equals(theCode) && c.getCodeSystem().equals(theSystem)).findAny()
//						.orElse(null);
				List<Code> codesFound = codes.stream().filter(c -> c.getValue().equals(theCode))
						.collect(Collectors.toList());
				if (theSystem == null || theSystem.isEmpty()) {
					retVal.addParameter().setName("result").setValue(new BooleanType(codesFound.size() > 0));
				} else {
					Code codeFound = codes.stream()
							.filter(c -> c.getValue().equals(theCode) && c.getCodeSystem().equals(theSystem)).findAny()
							.orElse(null);
					retVal.addParameter().setName("result").setValue(new BooleanType(codeFound != null));
				}
				for (Code code : codesFound) {
					retVal.addParameter().setName("codeSystem").setValue(new StringType(code.getCodeSystem()));
				}
				String encoded = parser.encodeResourceToString(retVal);
				return encoded;
			}

		}

		OperationOutcome oo = new OperationOutcome();
		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Value set not found");
		String encoded = parser.encodeResourceToString(oo);
		return encoded;
	}

	public String validateFhirCode(String theValueSetIdentifier, String theCode, String theSystem, String format)
			throws IOException {
		System.out.println("Breakpoint: " + format);
		UriType url = new UriType();
		url.setValue(fhirUrl.concat(theValueSetIdentifier));
		OkHttpClient client = new OkHttpClient.Builder().build();
		Request request = new Request.Builder().url(url.getValue()).build();
		Call call = client.newCall(request);
		Response response = call.execute();
		IParser responseParser = fhirR4Context.newXmlParser();
		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);
		if (response.code() == 200) {
			ValueSet vs = responseParser.parseResource(ValueSet.class, response.body().string());
			List<ValueSetExpansionContainsComponent> contains = vs.getExpansion().getContains();
			Parameters result = validateCodeIsInContains(contains, theCode, theSystem);
			if (result != null) {
				String encoded = parser.encodeResourceToString(result);
				return encoded;
			}
			List<ConceptSetComponent> includes = vs.getCompose().getInclude();
			result = validateCodeIsInInclude(includes, theCode, theSystem);
			if (result != null) {
				String encoded = parser.encodeResourceToString(result);
				return encoded;
			}
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Code not found");
			String encoded = parser.encodeResourceToString(oo);
			return encoded;

		} else {
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics(response.message());
			String encoded = parser.encodeResourceToString(oo);
			return encoded;
		}
	}

	public String validateAphlCode(String program, String theValueSetIdentifier, String vid, String theCode,
			String theSystem, String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();

		AphlValueset vs = null;
		if (vid != null) {
			int version = Integer.parseInt(vid);
			Aggregation aggregation = newAggregation(AphlValueset.class,
					match(Criteria.where("program").is(program)
							.andOperator(Criteria.where("name").is(theValueSetIdentifier))
							.andOperator(Criteria.where("version").is(version))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name", "program").first("version").as("version").first("$$ROOT").as("doc"),
					replaceRoot("$doc")).withOptions(options);
			AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
					AphlValueset.class);
			vs = result.getUniqueMappedResult();

		} else {
			Aggregation aggregation = newAggregation(AphlValueset.class,
					match(Criteria.where("program").is(program)
							.andOperator(Criteria.where("name").is(theValueSetIdentifier))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name", "program").first("version").as("version").first("$$ROOT").as("doc"),
					replaceRoot("$doc")).withOptions(options);
			AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
					AphlValueset.class);
			vs = result.getUniqueMappedResult();
		}
		if (vs != null) {

			Set<Code> codes = vs.getCodes();
			Parameters retVal = new Parameters();
			List<Code> codesFound = codes.stream().filter(c -> c.getValue().equals(theCode))
					.collect(Collectors.toList());
			if (theSystem == null || theSystem.isEmpty()) {
				retVal.addParameter().setName("result").setValue(new BooleanType(codesFound.size() > 0));
			} else {
				Code codeFound = codes.stream()
						.filter(c -> c.getValue().equals(theCode) && c.getCodeSystem().equals(theSystem)).findAny()
						.orElse(null);
				retVal.addParameter().setName("result").setValue(new BooleanType(codeFound != null));
			}
			for (Code code : codesFound) {
				retVal.addParameter().setName("codeSystem").setValue(new StringType(code.getCodeSystem()));
			}
			String encoded = parser.encodeResourceToString(retVal);
			return encoded;

		}

		OperationOutcome oo = new OperationOutcome();
		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Value set not found");
		String encoded = parser.encodeResourceToString(oo);
		return encoded;
	}

	public Parameters validateCodeIsInContains(List<ValueSetExpansionContainsComponent> contains, String theCode,
			String theSystem) {
		for (ValueSetExpansionContainsComponent nextCode : contains) {
			Parameters result = validateCodeIsInContains(nextCode.getContains(), theCode, theSystem);
			if (result != null) {
				return result;
			}
			String system = nextCode.getSystem();
			String code = nextCode.getCode();
			if (isNotBlank(theCode)) {
				if (theCode.equals(code) && (isBlank(theSystem) || theSystem.equals(system))) {
					Parameters retVal = new Parameters();
					retVal.addParameter().setName("result").setValue(new BooleanType(true));
					if (isNotBlank(nextCode.getDisplay())) {
						retVal.addParameter().setName("display").setValue(new StringType(nextCode.getDisplay()));
					}
					return retVal;
				}
			}
		}

		return null;
	}

	public Parameters validateCodeIsInInclude(List<ConceptSetComponent> includes, String theCode, String theSystem) {
		for (ConceptSetComponent nextElement : includes) {
			Parameters result = validateCodeIsInConcept(nextElement.getConcept(), nextElement.getSystem(), theCode,
					theSystem);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	public Parameters validateCodeIsInConcept(List<ConceptReferenceComponent> concepts, String system, String theCode,
			String theSystem) {
		for (ConceptReferenceComponent nextConcept : concepts) {
			String code = nextConcept.getCode();
			if (isNotBlank(theCode)) {
				if (theCode.equals(code) && (isBlank(theSystem) || theSystem.equals(system))) {
					Parameters retVal = new Parameters();
					retVal.addParameter().setName("result").setValue(new BooleanType(true));
					if (isNotBlank(nextConcept.getDisplay())) {
						retVal.addParameter().setName("display").setValue(new StringType(nextConcept.getDisplay()));
					}
					return retVal;
				}
			}
		}

		return null;
	}

}
