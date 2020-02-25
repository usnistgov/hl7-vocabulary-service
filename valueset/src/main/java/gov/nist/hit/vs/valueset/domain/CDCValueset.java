package gov.nist.hit.vs.valueset.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cdc-valueset")
public class CDCValueset {

	@Id
	public String id;
	@DBRef
	public CDCValuesetMetadata metadata;
	private int version;
	public List<CDCCode> cdcCodes;
	private Date updateDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CDCValuesetMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(CDCValuesetMetadata metadata) {
		this.metadata = metadata;
	}

	public List<CDCCode> getCdcCodes() {
		return cdcCodes;
	}

	public void setCdcCodes(List<CDCCode> cdcCodes) {
		this.cdcCodes = cdcCodes;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	

}
