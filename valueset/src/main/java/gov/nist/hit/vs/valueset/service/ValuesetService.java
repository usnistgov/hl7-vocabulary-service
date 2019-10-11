/**
 * 
 */
package gov.nist.hit.vs.valueset.service;

import java.io.IOException;

import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet.ValidateCodeResult;

/**
 * @author Ismail Mellouli
 *
 */

@Service("igService")
public interface ValuesetService {

	public String getValuesets(String source, String format) throws IOException;
	
	public String getValueset(String source, String theValueSetIdentifier, String format, Boolean meta, Boolean expand) throws IOException;

	public String validateCode(String source, String theValueSetIdentifier, String theCode, String theSystem,
			String format) throws IOException;

	
}
