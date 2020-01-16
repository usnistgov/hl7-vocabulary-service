package gov.nist.hit.vs.valueset.domain;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cdc-valueset-metadata")
public class CDCValuesetMetadata {

	@Id
	public String id;

	public String packageUID;
	//how to declare as unique
	public String name;
	public String title;
	public String date;

	public CDCValuesetMetadata() {
	}

	public CDCValuesetMetadata(String packageUID, String name, String title, String date) {
		this.packageUID = packageUID;
		this.name = name;
		this.title = title;
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPackageUID() {
		return packageUID;
	}

	public void setPackageUID(String packageUID) {
		this.packageUID = packageUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return String.format("valueset[packageUID=%s, name='%s', title='%s', date='%s']", packageUID, name, title,
				date);
	}
}
