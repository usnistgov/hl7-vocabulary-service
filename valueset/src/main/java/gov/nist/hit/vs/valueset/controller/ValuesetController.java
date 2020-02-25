package gov.nist.hit.vs.valueset.controller;

import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet;
import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet.ValidateCodeResult;
import ca.uhn.fhir.jpa.provider.r4.JpaResourceProviderR4;
import gov.nist.hit.vs.valueset.domain.Code;
import gov.nist.hit.vs.valueset.service.ValuesetService;
import io.swagger.annotations.ApiParam;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.ValueSet;

@RestController
@RequestMapping("/{source}/ValueSet")
public class ValuesetController extends JpaResourceProviderR4<ValueSet> {

	@Autowired
	ValuesetService valuesetService;
	@Autowired
	MongoTemplate mongoTemplate;

	@RequestMapping(value = "", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesets(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format)
			throws IOException {
		String result = valuesetService.getLatestValuesets(source, _format);
		return result;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetById(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@ApiParam(example = "MVX") @PathVariable String id,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, null, _format, false, false);
		return result;
	}

	@RequestMapping(value = "/{id}/$meta", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetMetaById(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, null, _format, true, false);
		return result;
	}

	@RequestMapping(value = "/{id}/$expand", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getExpandedValueset(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, null, _format, false, true);
		return result;
	}

	@RequestMapping(value = "/{id}/$validate-code", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String validateCode(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id, @RequestParam String code, @RequestParam(required = false) String system,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format)
			throws IOException {

		String result = valuesetService.validateCode(source, id, null, code, system, _format);
		return result;
	}

	@RequestMapping(value = "/{id}/_history/{vid}", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetByVersion(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id, @PathVariable String vid,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, vid, _format, false, false);
		return result;
	}

	@RequestMapping(value = "/{id}/_history/{vid}/$meta", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody String getValuesetMetaByVersion(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id, @PathVariable String vid,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, vid, _format, true, false);
		return result;
	}

	@RequestMapping(value = "/{id}/_history/{vid}/$expand", method = RequestMethod.GET, produces = {
			"application/json" })
	public @ResponseBody String getExpandedValuesetByVersion(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id, @PathVariable String vid,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format,
			@RequestParam(required = false) String name) throws IOException {
		String result = valuesetService.getValueset(source, id, vid, _format, false, true);
		return result;
	}

	@RequestMapping(value = "/{id}/_history/{vid}/$validate-code", method = RequestMethod.GET, produces = {
			"application/json" })
	public @ResponseBody String validateCodeByVersion(
			@ApiParam(example = "cdc", allowableValues = "cdc, phinvads") @PathVariable String source,
			@PathVariable String id, @PathVariable String vid, @RequestParam String code, @RequestParam String system,
			@ApiParam(example = "json", allowableValues = "json, xml") @RequestParam(required = false) String _format)
			throws IOException {

		String result = valuesetService.validateCode(source, id, vid, code, system, _format);

		return result;
	}

}
