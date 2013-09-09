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
package org.exoplatform.crowdin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class FileUtils {

  static Log log;

  public static void setLog(Log varLog) {
    log = varLog;
  }

  public static Log getLog() {
    if (log == null) {
      log = new SystemStreamLog();
    }

    return log;
  }

  /**
   * Replace all String "regex" by String "replacement" in file "filePath"
   *
   * @param filePath
   * @param regex
   * @param replacement
   * @return
   */
  public static boolean replaceStringInFile(String filePath, String regex, String replacement) {

    try {
      File file = new File(filePath);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";
      StringBuffer oldtext = new StringBuffer("");
      while ((line = reader.readLine()) != null) {
        oldtext.append(line + System.getProperty("line.separator"));
      }
      reader.close();
      // replace a word in a file
      String newtext = oldtext.toString().replaceAll(regex, replacement);
      FileWriter writer = new FileWriter(filePath);
      writer.write(newtext);
      writer.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Replace special character in file "filePath".
   * The couple (regex,replacement) are defined by "replaceListConfigkey" in .properties file "propertiesFilePath"
   *
   * @param filePath
   * @param propertiesFilePath
   * @param replaceListConfigkey
   * @return
   */
  public static boolean replaceCharactersInFile(String filePath, String propertiesFilePath, String replaceListConfigkey) {
    Properties configProp = new Properties();

    InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(propertiesFilePath);
    try {
      configProp.load(in);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    try {
      File file = new File(filePath);
      if (!file.exists()) {
        getLog().info("File " + file + " is not existed.");
        return false;
      }
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";
      StringBuffer oldtext = new StringBuffer("");
      while ((line = reader.readLine()) != null) {
        //do not use \r\n causes ^M problem, use the correct line separator 
        oldtext.append(line + System.getProperty("line.separator"));
      }
      reader.close();
      String newtext = oldtext.toString();

      String replaceListConfig = configProp.getProperty(replaceListConfigkey);
      String[] replaceListArr = replaceListConfig.split("__AND__");

      for (String obj : replaceListArr) {
        String[] keyValue = obj.replaceAll("\\[", "").replaceAll("\\]", "").split("BY");
        String key = keyValue[0];
        String value = keyValue.length > 1 ? keyValue[1] : "";
        if (value == null || value.length() == 0) {
          value = "";
        }
        //System.out.println("\n\n replace:[" + key + "] by: [" + value + "] in file:  " + filePath);
        newtext = newtext.replaceAll(key, value);
      }

      FileWriter writer = new FileWriter(filePath);
      writer.write(newtext);
      writer.close();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }


}
