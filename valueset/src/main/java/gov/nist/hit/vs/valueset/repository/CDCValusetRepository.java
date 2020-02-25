package gov.nist.hit.vs.valueset.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import gov.nist.hit.vs.valueset.domain.CDCValueset;


public interface CDCValusetRepository  extends MongoRepository<CDCValueset, String> {
	
	List<CDCValueset> findByMetadataId(String id);
	
	CDCValueset findLatestByMetadataId(String id);
	
	@Query("{ 'metadata.name' : ?0 }")
	CDCValueset findByMetadataName(String name);
}
