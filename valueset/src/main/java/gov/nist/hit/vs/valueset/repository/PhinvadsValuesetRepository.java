package gov.nist.hit.vs.valueset.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import gov.nist.hit.vs.valueset.domain.PhinvadsValueset;


public interface PhinvadsValuesetRepository extends MongoRepository<PhinvadsValueset, String> {
	PhinvadsValueset findByOid(String oid);
}
