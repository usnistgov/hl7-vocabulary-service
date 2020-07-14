/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.AphlValueset;
import gov.nist.hit.vs.valueset.domain.Code;
import gov.nist.hit.vs.valueset.domain.Hl7Valueset;
import gov.nist.hit.vs.valueset.service.Hl7Service;

@Service("hl7Service")
public class Hl7ServiceImpl implements Hl7Service{
	
	@Autowired
	MongoTemplate mongoTemplate;


	@Override
	public void saveTable(String name, String hl7Version, Set<Code> codes) {
	
		Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name).andOperator(Criteria.where("hl7Version").is(hl7Version)));
		Update update = new Update();
		update.set("codes", codes);
		update.set("numberOfCodes", codes.size());
		mongoTemplate.upsert(query, update, Hl7Valueset.class);

	}

}
