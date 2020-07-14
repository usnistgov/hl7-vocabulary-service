/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.auth.service;

import org.springframework.stereotype.Service;

import gov.nist.hit.vs.auth.domain.ApplicationUser;

@Service("userService")
public interface UserService {

	public void saveUser(ApplicationUser user);
	
	public boolean usernameExist(String username);
}
