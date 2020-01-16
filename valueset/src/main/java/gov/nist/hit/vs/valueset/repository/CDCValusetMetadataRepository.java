package gov.nist.hit.vs.valueset.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import gov.nist.hit.vs.valueset.domain.CDCValuesetMetadata;

public interface CDCValusetMetadataRepository extends MongoRepository<CDCValuesetMetadata, String> {

	CDCValuesetMetadata findByName(String name);
}
