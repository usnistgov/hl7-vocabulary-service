/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import gov.nist.hit.vs.auth.domain.ApplicationUser;

@Repository
public interface ApplicationUserRepository extends MongoRepository<ApplicationUser, String> {
	ApplicationUser findByUsername(String username);
}