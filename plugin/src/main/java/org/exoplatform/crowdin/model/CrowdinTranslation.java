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
	
	public CrowdinTranslation(File _file, String _name, String _type, String _project, String _lang, CrowdinFile _master, boolean _shouldBeCleaned) {
		super(_file, _name, _type, _project, _shouldBeCleaned);
		lang = _lang;
		if (lang.equals("vn")) lang = "vi";
		else if (lang.equals("es")) lang = "es-ES";
		lang = encodeLanguageName(lang, true);
		master = _master;
	}
	
	public String getLang() {
		return lang;
	}
	
	public CrowdinFile getMaster() {
		return master;
	}
	
  /**
   * @param lang language name
   * @param isEncode encode _ character in language name if isEncode is true,
   *          decode _ character in language name if isEncode is false
   * @return encoded or decoded language name
   */
  public static String encodeLanguageName(String lang, boolean isEncode) {
    if (isEncode) {
      return (lang == null || lang.isEmpty()) ? lang : lang.replace("_", "-");
    } else {
      return (lang == null || lang.isEmpty()) ? lang : lang.replace("-", "_");
    }
  }
	
}
