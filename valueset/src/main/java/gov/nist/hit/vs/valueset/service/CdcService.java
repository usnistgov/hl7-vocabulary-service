package gov.nist.hit.vs.valueset.service;

import java.util.List;

import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.CDCCode;
import gov.nist.hit.vs.valueset.domain.CDCValueset;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;

@Service("cdcService")

public interface CdcService {

	public List<CDCValuesetMetadata> getCdcValuesetsMetadata();
	public List<CDCCode> parseCodes(String codes, String packageUid);
	public CDCValueset getCDCValuesetByMetaId(String id);
	public CDCValueset createCDCValueset(CDCValueset cdcValueset);
	public CDCValueset saveCDCValueset(CDCValueset cdcValueset);
}
