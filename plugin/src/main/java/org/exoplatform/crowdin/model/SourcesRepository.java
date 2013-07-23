package org.exoplatform.crowdin.model;

/**
 * Sources Repository
 */
public class SourcesRepository {
  private String uri;
  private String branch;
  private String localDirectory;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getName() {
    return getUri().substring(getUri().lastIndexOf('/') + 1, getUri().lastIndexOf('.'));
  }

  public String getLocalDirectory() {
    return localDirectory;
  }

  public void setLocalDirectory(String localDirectory) {
    this.localDirectory = localDirectory;
  }
}
