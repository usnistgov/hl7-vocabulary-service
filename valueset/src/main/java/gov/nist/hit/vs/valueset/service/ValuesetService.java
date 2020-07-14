/**
 * 
 */
package gov.nist.hit.vs.valueset.service;

import java.io.IOException;

import org.springframework.stereotype.Service;



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
