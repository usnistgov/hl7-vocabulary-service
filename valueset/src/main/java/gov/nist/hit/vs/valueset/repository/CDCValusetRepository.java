package gov.nist.hit.vs.valueset.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import gov.nist.hit.vs.valueset.domain.CDCValueset;

public interface CDCValusetRepository extends MongoRepository<CDCValueset, String> {

	CDCValueset findByName(String name);
}
