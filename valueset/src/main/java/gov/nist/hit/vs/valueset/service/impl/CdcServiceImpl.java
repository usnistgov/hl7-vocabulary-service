package gov.nist.hit.vs.valueset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.CDCCode;
import gov.nist.hit.vs.valueset.domain.CDCValueset;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import gov.nist.hit.vs.valueset.repository.CDCValusetMetadataRepository;
import gov.nist.hit.vs.valueset.repository.CDCValusetRepository;
import gov.nist.hit.vs.valueset.service.CdcService;

@Service("cdcService")
public class CdcServiceImpl implements CdcService {

	@Autowired
	private CDCValusetMetadataRepository cdcValusetMetadataRepository;

	@Autowired
	private CDCValusetRepository cdcValusetRepository;

	@Override
	public List<CDCValuesetMetadata> getCdcValuesetsMetadata() {
		// TODO Auto-generated method stub
		return cdcValusetMetadataRepository.findAll();
	}

	@Override
	public List<CDCCode> parseCodes(String codes, String packageUid) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		switch (packageUid) {
		case "419ea9b7-9dd8-e911-a18e-0ecd7015b0a4":
			cdcCodes = parseCVXCodes(codes);
			break;
		case "84209684-0cde-e911-a18f-0ecd7015b0a4":
			cdcCodes = parseVISCodes(codes);
			break;
		case "7fc180c2-9dd8-e911-a18e-0ecd7015b0a4":
			cdcCodes = parseMVXCodes(codes);
			break;
		case "e5c09239-0ede-e911-a18f-0ecd7015b0a4":
			cdcCodes = parseNDCUse(codes);
			break;
		case "56037ed5-9dd8-e911-a18e-0ecd7015b0a4":
			cdcCodes = parseNDCSale(codes);
			break;
		default:
			break;
		}
		return cdcCodes;
	}

	public List<CDCCode> parseNDCSale(String codes) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		Scanner scanner = new Scanner(codes);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] code = line.split("\\|");
			CDCCode cdcCode = new CDCCode();
			cdcCode.setCode(code[14].trim().replaceAll("\\\"", ""));
			cdcCode.setName(code[13].trim().replaceAll("\\\"", ""));
			cdcCode.setSystem("NDC");
			cdcCodes.add(cdcCode);
		}
		scanner.close();
		return cdcCodes;
	}
	public List<CDCCode> parseNDCUse(String codes) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		Scanner scanner = new Scanner(codes);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] code = line.split("\\|");
			CDCCode cdcCode = new CDCCode();
			cdcCode.setCode(code[14].trim().replaceAll("\\\"", ""));
			cdcCode.setName(code[12].trim().replaceAll("\\\"", ""));
			cdcCode.setSystem("NDC");
			cdcCodes.add(cdcCode);
		}
		scanner.close();
		return cdcCodes;
	}
	
	public List<CDCCode> parseVISCodes(String codes) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		Scanner scanner = new Scanner(codes);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] code = line.split("\\|");
			CDCCode cdcCode = new CDCCode();
			cdcCode.setCode(code[2].trim().replaceAll("\\\"", ""));
			cdcCode.setName(code[0].trim().replaceAll("\\\"", ""));
			cdcCode.setStatus(code[4].trim().replaceAll("\\\"", ""));
			cdcCode.setLastUpdated(code[5].trim().replaceAll("\\\"", ""));
			cdcCode.setSystem("cdcgs1vis");
			cdcCodes.add(cdcCode);
		}
		scanner.close();
		return cdcCodes;
	}

	public List<CDCCode> parseCVXCodes(String codes) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		Scanner scanner = new Scanner(codes);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] code = line.split("\\|");
			CDCCode cdcCode = new CDCCode();
			cdcCode.setCode(code[0].trim().replaceAll("\\\"", ""));
			cdcCode.setDescription(code[1].trim().replaceAll("\\\"", ""));
			cdcCode.setName(code[2].trim().replaceAll("\\\"", ""));
			cdcCode.setNotes(code[3].trim().replaceAll("\\\"", ""));
			cdcCode.setStatus(code[4].trim().replaceAll("\\\"", ""));
			cdcCode.setBool(code[6].trim().replaceAll("\\\"", ""));
			cdcCode.setLastUpdated(code[7].trim().replaceAll("\\\"", ""));
			cdcCode.setSystem("CVX");
			cdcCodes.add(cdcCode);
		}
		scanner.close();
		return cdcCodes;
	}

	public List<CDCCode> parseMVXCodes(String codes) {
		List<CDCCode> cdcCodes = new ArrayList<CDCCode>();
		Scanner scanner = new Scanner(codes);
		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] code = line.split("\\|");
			CDCCode cdcCode = new CDCCode();
			cdcCode.setCode(code[0].trim().replaceAll("\\\"", ""));
			cdcCode.setName(code[1].trim().replaceAll("\\\"", ""));
			cdcCode.setNotes(code[2].trim().replaceAll("\\\"", ""));
			cdcCode.setStatus(code[3].trim().replaceAll("\\\"", ""));
			cdcCode.setLastUpdated(code[4].trim().replaceAll("\\\"", ""));
			cdcCode.setSystem("MVX");
			cdcCodes.add(cdcCode);
		}
		scanner.close();
		return cdcCodes;
	}

	@Override
	public CDCValueset createCDCValueset(CDCValueset cdcValueset) {
		return cdcValusetRepository.insert(cdcValueset);
	}

	@Override
	public CDCValueset saveCDCValueset(CDCValueset cdcValueset) {
		return cdcValusetRepository.save(cdcValueset);
	}

	@Override
	public CDCValueset getCDCValuesetByMetaId(String id) {
		// TODO Auto-generated method stub
		return cdcValusetRepository.findByMetadataId(id);
	}

}
