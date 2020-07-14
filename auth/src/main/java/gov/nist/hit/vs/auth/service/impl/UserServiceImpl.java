/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nist.hit.vs.auth.domain.ApplicationUser;
import gov.nist.hit.vs.auth.repository.ApplicationUserRepository;
import gov.nist.hit.vs.auth.service.UserService;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private ApplicationUserRepository applicationUserRepository;

	@Override
	public void saveUser(ApplicationUser user) {
		applicationUserRepository.save(user);
	}

	@Override
	public boolean usernameExist(String username) {
		ApplicationUser user = applicationUserRepository.findByUsername(username);
		if (user != null) {
			return true;
		}
		return false;
	}
}
