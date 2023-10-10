/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
    lang = getCrowdinLangFromPlatformLang(_lang);
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
   * @param lang     language name
   * @param isEncode encode _ character in language name if isEncode is true,
   *                 decode _ character in language name if isEncode is false
   * @return encoded or decoded language name
   */
  public static String encodeLanguageName(String lang, boolean isEncode) {
    if (isEncode) {
      return (lang == null || lang.isEmpty()) ? lang : lang.replace("_", "-");
    } else {
      return (lang == null || lang.isEmpty()) ? lang : lang.replace("-", "_");
    }
  }

  /**
   * Return the right language code for Crowdin from the language code used by eXo
   * This is useful for some specific case, for example the indonesian language code in Crowdin
   * is "id" whereas it is "in" in the platform (and Java).
   * @param lang Language code in platform
   * @return Language code in Crowdin
   */
  public static String getCrowdinLangFromPlatformLang(String lang) {
    String language = lang;
    if("es".equals(language)) {
      // make sure that resources files in eXo with only "_es" are correctly mapped to spanish language in Crowdin (es-ES)
      language = "es-ES";
    } else if("vn".equals(language)) {
      language = "vi";
    } else if("in".equals(language)) {
      // Indonesian language code is "id" in Crowdin but "in" in Java and eXo
      language = "id";
    }
    return language;
  }

  /**
* tranform xx-XX to xx-rXX
* @param locale
* @return
*/
  public static String encodeAndroidLocale(String locale){
    if (locale.contains("-")){
      return locale = locale.replaceAll("-", "-r");
    }
    else{
      return locale;
    }
  }
  
  /**
* transform en-GB to en_GB
* @param locale
* @return
*/
  public static String encodeIOSLocale(String locale){
    return encodeLanguageName(locale, false);
  }  
}
