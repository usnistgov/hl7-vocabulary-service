package gov.nist.hit.vs.bootstrap.configuration;

import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;

@ConfigurationProperties(prefix = "spring.boot.config.vcsms")
@Component
public class SpringBootConfiguration {

	private String protocol;
	private String root;
	private String groupMnemonic;
	private String nodeID;
	private String ftpHost;
	private String ftpUserName;
	private String ftpUserPassword;
	private String adminUsername;
	private String adminPassword;

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getGroupMnemonic() {
		return groupMnemonic;
	}

	public void setGroupMnemonic(String groupMnemonic) {
		this.groupMnemonic = groupMnemonic;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getFtpHost() {
		return ftpHost;
	}

	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}

	public String getFtpUserName() {
		return ftpUserName;
	}

	public void setFtpUserName(String ftpUserName) {
		this.ftpUserName = ftpUserName;
	}

	public String getFtpUserPassword() {
		return ftpUserPassword;
	}

	public void setFtpUserPassword(String ftpUserPassword) {
		this.ftpUserPassword = ftpUserPassword;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

}
