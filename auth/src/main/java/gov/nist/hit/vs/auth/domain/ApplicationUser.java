/**
 * @author Ismail Mellouli (NIST)
 *
 */
package gov.nist.hit.vs.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class ApplicationUser {
	@Id
	private String id;
	private String username;
	private String password;

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}