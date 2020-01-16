package gov.nist.hit.vs.valueset.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import gov.nist.healthcare.vcsms.service.impl.NISTVCSMSClientImpl;
import gov.nist.hit.vs.valueset.domain.CDCCode;
import gov.nist.hit.vs.valueset.domain.CDCValueset;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import gov.nist.hit.vs.valueset.domain.Code;
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

	@Override
	public String getValuesets(String source, String format) throws IOException {

		switch (source) {
		case "fhir":
			return null;
		case "phinvads":
			return getPhinvadsValuesets(format);
		case "cdc":
			return getCDCValuesets(format);
		}
		return null;

	}

	@Override
	public String getValueset(String source, String theValueSetIdentifier, String format, Boolean meta, Boolean expand)
			throws IOException {
		switch (source) {
		case "fhir":
			return getFhirValueset(theValueSetIdentifier, format, meta, expand);
		case "phinvads":
			return getPhinvadsValueset(theValueSetIdentifier, format, meta, expand);
		case "cdc":
			return getCDCValueset(theValueSetIdentifier, format, meta, expand);
		}
		return null;

	}

	public String getCDCValuesets(String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);
		List<CDCValueset> cdcValuesets = cdcValusetRepository.findAll();
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

	public String getPhinvadsValuesets(String format) throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		List<PhinvadsValueset> valuesets = phinvadsValuesetRepository.findAll();
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

	public String getCDCValueset(String theValueSetIdentifier, String format, Boolean meta, Boolean expand)
			throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);
		CDCValuesetMetadata metadata = cdcValusetMetadataRepository.findByName(theValueSetIdentifier);
		if (metadata != null) {
			CDCValueset cdcValueset = cdcValusetRepository.findByMetadataId(metadata.getId());
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

	public String getPhinvadsValueset(String theValueSetIdentifier, String format, Boolean meta, Boolean expand)
			throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();
		parser.setPrettyPrint(true);

		PhinvadsValueset vs = phinvadsValuesetRepository.findByOid(theValueSetIdentifier);
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
		fhirVs.setExpansion(vsExpansion);
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

	@Override
	public String validateCode(String source, String theValueSetIdentifier, String theCode, String theSystem,
			String format) throws IOException {

		boolean haveCode = theCode != null && theCode.isEmpty() == false;
		if (!haveCode) {
			throw new InvalidRequestException("No code provided to validate");
		}
		switch (source) {
		case "fhir":
			return validateFhirCode(theValueSetIdentifier, theCode, theSystem, format);
		case "phinvads":
			return validatePhinvadsCode(theValueSetIdentifier, theCode, theSystem, format);
		case "cdc":
			return validateCDCCode(theValueSetIdentifier, theCode, theSystem, format);
		}
		return null;
	}

	public String validateCDCCode(String theValueSetIdentifier, String theCode, String theSystem, String format)
			throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();

		CDCValuesetMetadata metadata = cdcValusetMetadataRepository.findByName(theValueSetIdentifier);
		if (metadata != null) {
			CDCValueset cdcValueset = cdcValusetRepository.findByMetadataId(metadata.getId());
			if (cdcValueset != null) {
				List<CDCCode> codes = cdcValueset.getCdcCodes();
				CDCCode codeFound = codes.stream().filter(c -> c.getCode().toLowerCase().equals(theCode.toLowerCase())
						&& c.getSystem().toLowerCase().equals(theSystem.toLowerCase())).findAny().orElse(null);
				Parameters retVal = new Parameters();
				retVal.addParameter().setName("result").setValue(new BooleanType(codeFound != null));
				String encoded = parser.encodeResourceToString(retVal);
				return encoded;
			}
		}

		CDCValueset cdcValueset = cdcValusetRepository.findByMetadataName(theValueSetIdentifier);
		String r = null;
		if (cdcValueset != null) {
//			 r = cdcClient.fetchPackage(cdcValueset.getPackageUID());
//			r = cdcClient.getValuesetString(cdcValueset.getPackageUID());
			if (r != null) {
				return r;
			}

		}

		OperationOutcome oo = new OperationOutcome();
		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Value set not found");
		String encoded = parser.encodeResourceToString(oo);
		return encoded;
//		ValueSetResultDto vsSearchResult = vocabService.getValueSetByOid(theValueSetIdentifier);
//		List<gov.cdc.vocab.service.bean.ValueSet> valueSets = vsSearchResult.getValueSets();
//
//		if (valueSets != null && valueSets.size() > 0) {
//			gov.cdc.vocab.service.bean.ValueSet vs = valueSets.get(0);
//			ValueSetVersion vsv = vocabService.getValueSetVersionsByValueSetOid(vs.getOid()).getValueSetVersions()
//					.get(0);
//			ValidateResultDto result = vocabService.validateConceptValueSetMembership(theSystem, theCode,
//					theValueSetIdentifier, vsv.getVersionNumber());
//
//			Parameters retVal = new Parameters();
//			retVal.addParameter().setName("result").setValue(new BooleanType(result.isValid()));
//			String encoded = parser.encodeResourceToString(retVal);
//			return encoded;
//		}
//
//		OperationOutcome oo = new OperationOutcome();
//		oo.addIssue().setSeverity(IssueSeverity.ERROR).setDiagnostics("Value set not found");
//		String encoded = parser.encodeResourceToString(oo);
//		return encoded;
	}

	public String validatePhinvadsCode(String theValueSetIdentifier, String theCode, String theSystem, String format)
			throws IOException {

		IParser parser = (format != null && format.equals("xml")) ? fhirR4Context.newXmlParser()
				: fhirR4Context.newJsonParser();

		PhinvadsValueset vs = phinvadsValuesetRepository.findByOid(theValueSetIdentifier);

		if (vs != null) {

			if (vs.getHasPartCodes().equals(true)) {
				ValueSetResultDto vsSearchResult = vocabService.getValueSetByOid(theValueSetIdentifier);
				List<gov.cdc.vocab.service.bean.ValueSet> valueSets = vsSearchResult.getValueSets();
				if (valueSets != null && valueSets.size() > 0) {
					gov.cdc.vocab.service.bean.ValueSet phinvadsValueset = valueSets.get(0);
					ValueSetVersion vsv = vocabService.getValueSetVersionsByValueSetOid(phinvadsValueset.getOid())
							.getValueSetVersions().get(0);
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
									theCode, theValueSetIdentifier, vsv.getVersionNumber());
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
				Code codeFound = codes.stream()
						.filter(c -> c.getValue().equals(theCode) && c.getCodeSystem().equals(theSystem)).findAny()
						.orElse(null);
				Parameters retVal = new Parameters();
				retVal.addParameter().setName("result").setValue(new BooleanType(codeFound != null));
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
