/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.AphlValueset;
import gov.nist.hit.vs.valueset.domain.Code;

@Service("aphlService")
public interface AphlService {

	public List<AphlValueset> getValuesetsByProgram(String program);

	public int saveValuesetsFromMap(String program, Date date, Map<String, Set<Code>> map);

}
