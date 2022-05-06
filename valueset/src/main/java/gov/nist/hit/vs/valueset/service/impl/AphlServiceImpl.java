
/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
	public int saveValuesetsFromMap(String program, Date date, Map<String, Set<Code>> map) {
		// TODO Auto-generated method stub
		int addedVs = 0;
		for (String key : map.keySet()) {
			Aggregation aggregation = newAggregation(AphlValueset.class,
					match(Criteria.where("program").is(program).andOperator(Criteria.where("name").is(key))),
					sort(Sort.Direction.DESC, "name").and(Sort.Direction.DESC, "version"),
					group("name", "program").first("version").as("version").first("$$ROOT").as("doc"),
					replaceRoot("$doc"));
			AggregationResults<AphlValueset> result = mongoTemplate.aggregate(aggregation, "aphl-valueset",
					AphlValueset.class);
			AphlValueset vs = result.getUniqueMappedResult();

			if (vs != null) {
				// Compare vs and map[key]. If there's a difference, create a new version of the
				// vs and save it
				if (key.equals(vs.getName()) && program.equals(vs.getProgram())) {
					Set<Code> addedCodes = this.difference(map.get(key), vs.getCodes());
					Set<Code> removedCodes = this.difference(vs.getCodes(), map.get(key));
					if (addedCodes.size() > 0 || removedCodes.size() > 0) {
						// Create map[key] as a new valueset with +1 version
						AphlValueset newValueset = new AphlValueset(date, map.get(key), vs.getVersion() + 1, key,
								program);
						addedVs++;
						mongoTemplate.insert(newValueset);
					}
				}
			} else {
				// Create map[key] as a new valueset
				AphlValueset newValueset = new AphlValueset(date, map.get(key), 1, key, program);
				addedVs++;
				mongoTemplate.insert(newValueset);
			}
		}
		return addedVs;

	}

	private Set<Code> difference(Set<Code> c1, Set<Code> c2) {
		Set<Code> newCodesCopy = new HashSet<Code>(c1);
		newCodesCopy.removeAll(c2);
		return newCodesCopy;
	}

}
