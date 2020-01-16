package gov.nist.hit.vs.valueset.domain;

import java.io.Serializable;

import org.bson.types.ObjectId;

public class Code implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 3734129200317300616L;
  private String value;
  private String description;
  private String codeSystem;
  private String comments;
  private String id;

  public Code() {
    this.setId(new ObjectId().toString());
  }

  public Code(String value, String description, String codeSystem, String comments) {
    this.setId(new ObjectId().toString());
    this.value = value;
    this.description = description;
    this.codeSystem = codeSystem;
    this.comments = comments;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public void setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
