/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.Code;

@Service("hl7Service")
public interface Hl7Service {

	public void saveTable(String name, String hl7Version, Set<Code> codes);

}
