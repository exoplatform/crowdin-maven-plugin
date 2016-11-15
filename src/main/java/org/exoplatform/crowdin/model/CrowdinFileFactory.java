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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.mojo.AbstractCrowdinMojo;
import org.exoplatform.crowdin.utils.XMLToProps;

public class CrowdinFileFactory {

  private AbstractCrowdinMojo currentMojo;

  public CrowdinFileFactory(AbstractCrowdinMojo _mojo) {
    currentMojo = _mojo;
  }

  /**
   * Creates and returns a CrowdinFile (master).
   *
   * @param _path    the real path of the file on the system
   * @param _name    the name with the Crowdin path
   * @param _project the project in Crowdin
   * @return the CrowdinFile object that represents this file.
   */
  public CrowdinFile prepareCrowdinFile(String _path, String _name, String _project) {


    File file = new File(_path);
    String type = _path.substring(_path.lastIndexOf('.') + 1);
    boolean shouldBeCleaned = false;
    
    // process only for projects, not for mobile 
    if (type.equals("xml") && (!_project.contains("mobile"))) {
      file = fromXMLToProps(file);
      _path = file.getPath();
      type = "properties";
      shouldBeCleaned = true;
    }
    
    else if (_project.contains("android")){
      type = "xml";
    }
    else if (_project.contains("ios")){
      type = "strings";
    }


    // replace all - by __ because of an unknown bug in RestAssured.get
    _project = encodeMinusCharacterInPath(_project, true);
    _name = encodeMinusCharacterInPath(_name, true);

    if (currentMojo.getLog().isDebugEnabled()) {
      currentMojo.getLog().debug("*** Creating CrowdinFile with path: " + _path);
      currentMojo.getLog().debug("*** Creating CrowdinFile with name: " + _name);
      currentMojo.getLog().debug("*** Creating CrowdinFile with type: " + type);
      currentMojo.getLog().debug("*** Creating CrowdinFile in project: " + _project);
    }

    return new CrowdinFile(file, _name, type, _project, shouldBeCleaned);
  }

  /**
   * Checks whether a file is a translation or not, depending of the format of its filename.
   *
   * @param _filename the name of the file
   * @return true if the file represented by _filename is a translation, false otherwise
   */
  public boolean isTranslation(String _filename) {
    return matchTranslation(_filename).matches();
  }

  /**
   * Compares the given filename on a defined pattern to identify its parts (name, lang, type).
   *
   * @param _filename the filename to match on the pattern
   * @return the Matcher object
   */
  public Matcher matchTranslation(String _filename) {
    /*
     * This is a regular expression that matches filenames with a language code, and a possible variant.
		 * Here is how to read it:
		 * - group 1 : ([a-zA-Z_0-9-]*) : any character in a-z, A-Z, 0-9, _ and -, any number of time
		 *                                i.e. the name of the file
		 *                            _ : the character _ only once
		 * - group 2 : ([a-z]{2})       : any character in a-z exactly twice
		 *                                i.e. the language code (e.g. fr, pt)
		 * - group 3 : (_([A-Z]{2}))?   : the character _ exactly once followed by any character in A-Z exactly twice, the whole thing zero or once
		 *                                i.e. the language variant (e.g. _BR in pt_BR)
		 *                            . : the character . only once
		 * - group 4 : ([a-z]*)         : any character in a-z, any number of time
		 *                                i.e. the file extension
		 */
    Pattern p = Pattern.compile("^([a-zA-Z_0-9-]*)_([a-z]{2})(_[A-Z]{2})?.([a-z]*)$");
    Matcher m = p.matcher(_filename);
    m.matches(); // seems necessary otherwise the regex won't be executed
    if (currentMojo.getLog().isDebugEnabled()) currentMojo.getLog().debug("*** Does " + _filename + " Matches? " + m.matches() + "; " + m);
    return m;
  }

  /**
   * Transforms an XML resource bundle into a Properties one.
   *
   * @param _xmlFile the File to transform
   * @return the Properties File.
   */
  private File fromXMLToProps(File _xmlFile) {
    String path = _xmlFile.getPath();
    try {
      Type type = path.contains("gadget") && !path.contains("GadgetPortlet") ? Type.GADGET : Type.PORTLET;
      if (XMLToProps.parse(path, type)) {
        path = path.replaceAll(".xml", ".properties");
        return new File(path);
      }
    } catch (Exception e) {
      currentMojo.getLog().error("Cannot transform " + path + " into a properties file. Reason:\n", e);
    }
    return _xmlFile;
  }

  /**
   * Creates and return a CrowdinTranslation.
   *
   * @param _master          The master CrowdinFile for this translation
   * @param _translationFile the real File
   * @return the CrowdinTranslation object that represents this translation file.
   */
  public CrowdinTranslation prepareCrowdinTranslation(CrowdinFile _master, File _translationFile) {
    String type = _translationFile.getName().substring(_translationFile.getName().lastIndexOf('.') + 1);

    boolean shouldBeCleaned = false;
    // don't convert xml to properties when in mobile project
    if (type.equals("xml") && !(_master.getProject().contains("mobile"))) {
      _translationFile = fromXMLToProps(_translationFile);
      shouldBeCleaned = true;
    }

    String name, lang="";
    
    // if project is mobile
    if (_master.getProject().contains("mobile")){
      List<String> fileSplitTempo =  Arrays.asList(_translationFile.getPath().split("/"));
      //if android
      if (_translationFile.getPath().contains("android")){
        for (String matchValue : fileSplitTempo) {
          if (matchValue.contains("values-")){
            String[] values_language = matchValue.split("values-");
            //get language in "values-languages"
            lang = values_language[1];
          }
        }
      }
      //if ios
      if (_translationFile.getPath().contains("ios")){
        for (String matchValue : fileSplitTempo) {
          if (matchValue.contains("lproj") && (!matchValue.contains("en"))){
            String[] values_language = matchValue.split(".lproj");
            //get langugage in "language.lproj"
            lang = values_language[0];
          }
        }
      }
    }
    // if project is not mobile, use ordinary processing
    else{
    if (_translationFile.getPath().contains("gadget") && !_translationFile.getPath().contains("GadgetPortlet")) {
      lang = _translationFile.getName().substring(0, _translationFile.getName().lastIndexOf('.'));
      if ("default".equals(lang)) {
        lang = "en";
      } else if (lang.indexOf("_ALL") > 0) {
        lang = lang.substring(0, lang.indexOf("_ALL"));
      }
    } else {
      Matcher m = matchTranslation(_translationFile.getName());
      lang = m.group(2);
      if (m.group(3) != null)
        lang += m.group(3);
    }
    }    
    
    name = encodeMinusCharacterInPath(_translationFile.getName(), true);

    return new CrowdinTranslation(_translationFile, name, _master.getType(), _master.getProject(), lang, _master, shouldBeCleaned);
  }

  /**
   * @param path     path
   * @param isEncode encode minus characters in path if isEncode is true, decode
   *                 minus characters if isEncode is false,
   * @return encoded or decoded path
   */
  public static String encodeMinusCharacterInPath(String path, boolean isEncode) {
    if (isEncode) {
      return (path == null || path.isEmpty()) ? path : path.replace("-", "__");
    } else {
      return (path == null || path.isEmpty()) ? path : path.replace("__", "-");
    }
  }
}
