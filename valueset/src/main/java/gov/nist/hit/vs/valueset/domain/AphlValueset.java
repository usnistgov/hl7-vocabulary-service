package gov.nist.hit.vs.valueset.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "aphl-valueset")
public class AphlValueset {

	@Id
	private String id;
	@CreatedDate
	private Date creationDate;
	private Date updateDate;
	private Set<Code> codes = new HashSet<Code>();
	private int version;
	private String name;
	@Indexed
	private String program;
	
	
	/**
	 * @param updateDate
	 * @param codes
	 * @param version
	 * @param name
	 * @param program
	 */
	public AphlValueset(Date updateDate, Set<Code> codes, int version, String name, String program) {
		super();
		this.updateDate = updateDate;
		this.codes = codes;
		this.version = version;
		this.name = name;
		this.program = program;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	
	
}
