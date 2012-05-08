package org.exoplatform.crowdin.model;

import java.io.File;

public class CrowdinFile {

	/**
	 * The File on the file system
	 */
	private File file;
	
	/**
	 * The type of the file: properties or android or strings (iOS) or ... (eXo's XML and gadgets' XML)
	 */
	private String type;
	
	/**
	 * The project + version in which this file belongs
	 */
	private String project;
	
	/**
	 * The name on the file on Crowdin
	 */
	private String name;
	
	/**
	 * Indicate this file should be cleaned or not
	 */
	private boolean shouldBeCleaned;
	
	public CrowdinFile(File _file, String _name, String _type, String _project, boolean _shouldBeCleaned) {
		file = _file;
		name = _name;
		type = _type;
		project = _project;
		shouldBeCleaned = _shouldBeCleaned;
	}

	/*
	 * Getters
	 */
	
	public String getType() {
		return type;
	}

	public File getFile() {
		return file;
	}

	public String getProject() {
		return project;
	}
	
	public String getCrowdinPath() {
		return project+name;
	}

	public String getName() {
		return name;
	}

  public boolean isShouldBeCleaned() {
    return shouldBeCleaned;
  }

}
