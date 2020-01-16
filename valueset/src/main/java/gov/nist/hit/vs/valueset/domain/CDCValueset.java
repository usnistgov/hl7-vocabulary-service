package gov.nist.hit.vs.valueset.domain;

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
	public List<CDCCode> cdcCodes;

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

}
