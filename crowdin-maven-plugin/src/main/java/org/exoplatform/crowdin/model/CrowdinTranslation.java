package org.exoplatform.crowdin.model;

import java.io.File;

public class CrowdinTranslation extends CrowdinFile {

	/**
	 * The language code
	 */
	private String lang;
	
	/**
	 * Reference to the master file (that is not a translation)
	 */
	private CrowdinFile master;
	
	public CrowdinTranslation(File _file, String _name, String _type, String _project, String _lang, CrowdinFile _master) {
		super(_file, _name, _type, _project);
		lang = _lang;
		if (lang.equals("vn")) lang = "vi";
		else if (lang.equals("es")) lang = "es-ES";
		master = _master;
	}
	
	public String getLang() {
		return lang;
	}
	
	public CrowdinFile getMaster() {
		return master;
	}
	
}
