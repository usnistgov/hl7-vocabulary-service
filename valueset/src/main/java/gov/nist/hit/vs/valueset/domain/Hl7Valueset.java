/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.valueset.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "hl7-valueset")
public class Hl7Valueset {
	@Id
	private String id;
	private String name;
	private int numberOfCodes;
	private Set<Code> codes = new HashSet<Code>();
	private int version;
	private String hl7Version;
	
	
	/**
	 * @param name
	 * @param codes
	 */
	public Hl7Valueset(String name, Set<Code> codes) {
		super();
		this.name = name;
		this.codes = codes;
		this.numberOfCodes = codes.size();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumberOfCodes() {
		return numberOfCodes;
	}
	public void setNumberOfCodes(int numberOfCodes) {
		this.numberOfCodes = numberOfCodes;
	}
	public Set<Code> getCodes() {
		return codes;
	}
	public void setCodes(Set<Code> codes) {
		this.codes = codes;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getHl7Version() {
		return hl7Version;
	}
	public void setHl7Version(String hl7Version) {
		this.hl7Version = hl7Version;
	}
	
	
}
