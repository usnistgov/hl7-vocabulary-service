
/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import gov.nist.hit.vs.valueset.domain.AphlValueset;
import gov.nist.hit.vs.valueset.domain.Code;
import gov.nist.hit.vs.valueset.service.AphlService;

@Service("aphlService")
public class AphlServiceImpl implements AphlService {
	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<AphlValueset> getValuesetsByProgram(String program) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveValuesetsFromMap(String program, Date date, Map<String, Set<Code>> map) {
		// TODO Auto-generated method stub
		for (String key : map.keySet()) {
			System.out.println(key);
			AphlValueset vs = mongoTemplate.findOne(
					Query.query(Criteria.where("name").is(key).andOperator(Criteria.where("program").is(program))),
					AphlValueset.class, "aphl-valueset");
			if (vs != null) {
				// Compare vs and map[key]. If there's a difference create a new version of the vs and save it
			} else {
				// Create map[key] as a new valueset
				AphlValueset newValueset = new AphlValueset(date, map.get(key), 1, key, program);
				mongoTemplate.insert(newValueset);
			}
		}

	}

}
