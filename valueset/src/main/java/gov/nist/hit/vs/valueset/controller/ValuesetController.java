package gov.nist.hit.vs.valueset.controller;

import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet;
import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet.ValidateCodeResult;
import ca.uhn.fhir.jpa.provider.r4.JpaResourceProviderR4;
import gov.nist.hit.vs.valueset.service.ValuesetService;

import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import org.hl7.fhir.r4.model.ValueSet;

@RestController
@RequestMapping("/{source}/ValueSet")
public class ValuesetController extends JpaResourceProviderR4<ValueSet> {

	@Autowired
	ValuesetService valuesetService;

	@RequestMapping(value = "", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesets(@PathVariable String source,
			@RequestParam(required = false) String _format) throws IOException {
		String result = valuesetService.getValuesets(source, _format);
		return result;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetById(@PathVariable String source, @PathVariable String id,
			@RequestParam(required = false) String _format, @RequestParam(required = false) String name)
			throws IOException {
		String result = valuesetService.getValueset(source, id, _format, false, false);
		return result;
	}

	@RequestMapping(value = "/{id}/$meta", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetMetaById(@PathVariable String source, @PathVariable String id,
			@RequestParam(required = false) String _format, @RequestParam(required = false) String name)
			throws IOException {
		String result = valuesetService.getValueset(source, id, _format, true, false);
		return result;
	}
	
	@RequestMapping(value = "/{id}/$expand", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getExpandedValueset(@PathVariable String source, @PathVariable String id,
			@RequestParam(required = false) String _format, @RequestParam(required = false) String name)
			throws IOException {
		String result = valuesetService.getValueset(source, id, _format, false, true);
		return result;
	}

	@RequestMapping(value = "/{id}/$validate-code", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String validateCode(@PathVariable String source, @PathVariable String id,
			@RequestParam String code, @RequestParam String system, @RequestParam(required = false) String _format)
			throws IOException {
		UriType url = new UriType();
		url.setValue("http://hl7.org/fhir/ValueSet/animal-breeds");

		CodeType theCode = new CodeType();
		theCode.setValue("684003");

		UriType theSystem = new UriType();
		theSystem.setValue("http://snomed.info/sct");

		String result = valuesetService.validateCode(source, id, code, system, _format);

		return result;
	}

}
