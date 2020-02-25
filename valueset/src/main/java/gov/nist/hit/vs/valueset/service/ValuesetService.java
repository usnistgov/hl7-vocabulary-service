/**
 * 
 */
package gov.nist.hit.vs.valueset.service;

import java.io.IOException;
import java.util.List;

import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoValueSet.ValidateCodeResult;
import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;
import gov.nist.hit.vs.valueset.domain.PhinvadsValueset;

/**
 * @author Ismail Mellouli
 *
 */

@Service("valuesetService")
public interface ValuesetService {

	public String getLatestValuesets(String source, String format) throws IOException;

	public String getValueset(String source, String theValueSetIdentifier, String vid, String format, Boolean meta,
			Boolean expand) throws IOException;

	public String validateCode(String source, String theValueSetIdentifier, String vid, String theCode,
			String theSystem, String format) throws IOException;

}
