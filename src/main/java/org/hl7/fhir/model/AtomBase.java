package org.hl7.fhir.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AtomBase {

  private String id;
  private Map<String, String> links = new LinkedHashMap<String, String>();
  private Map<String, String> tags = new HashMap<String, String>();
  private java.util.Calendar updated;
  private String authorName;
  private String authorUri;
  private String title;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }


  public Map<String, String> getLinks() {
    return links;
  }
  
  public String getAuthorName() {
    return authorName;
  }
  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }
  public String getAuthorUri() {
    return authorUri;
  }
  public void setAuthorUri(String authorUri) {
    this.authorUri = authorUri;
  }
  public java.util.Calendar getUpdated() {
    return updated;
  }
  public void setUpdated(java.util.Calendar updated) {
    this.updated = updated;
  }
	public Map<String, String> getTags() {
		return tags;
	}

  
}
