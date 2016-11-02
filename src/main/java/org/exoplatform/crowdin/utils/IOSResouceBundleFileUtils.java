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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

/**
 * This class contains the utilities to inject Crowdin translation file to iOS resource file 
 */
public class IOSResouceBundleFileUtils {

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

  /*
   * Read all lines of iOS resource file to list
   * One line will input to one list's item
   */
  public static List<String> readAllIOSResource(String filePath) {
    List<String> output = new ArrayList<String>();
    try {
      File file = new File(filePath);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";

      while ((line = reader.readLine()) != null) {
        output.add(line);
      }
      reader.close();
      return output;
    } catch (IOException ioe) {
      getLog().error(ioe);
      return new ArrayList<String>();
    }
  }

  /*
   * Read all lines of iOS resource file to list
   * One line will input to one list's item
   * Don't add comment lines and empty lines
   */
  public static List<String> readIOSResourceSkipCommentAndEmtyLine(String filePath) {
    List<String> output = new ArrayList<String>();
    try {
      File file = new File(filePath);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";
      boolean isCommentOrEmptyLine = false;
      while ((line = reader.readLine()) != null) {
        if (line.trim().indexOf("//") == 0 || line.trim().length() == 0) {
          continue;
        }
        
        if(line.trim().indexOf("/*") == 0 && line.trim().indexOf("*/") >= 0){
          continue;
        }
        
        if(line.trim().indexOf("/*") == 0){
          isCommentOrEmptyLine=true;
          continue;
        }
        
        if(line.trim().indexOf("*/") >= 0){
          isCommentOrEmptyLine=false;
          continue;
        }
        
        if(isCommentOrEmptyLine==false){
          output.add(line);
        }
          
      }
      reader.close();
      return output;
    } catch (IOException ioe) {
      getLog().error(ioe);
      return new ArrayList<String>();
    }
  }

  /*
   * Update translation from crowdin a crowdin file line to a resouce bundle file line
   */
  public static String updateTranslationByLine(String sourceLine, String crowdinLine) {
    if(sourceLine.toString().trim().length()==0 || crowdinLine.trim().length() ==0)
      return "";
    try{
      String sourceKey = sourceLine.toString().split("=")[0].trim();
      String crowdinKey = crowdinLine.split("=")[0].trim();
      if(sourceKey.equals(crowdinKey)){
        StringBuffer buffer = new  StringBuffer(sourceKey);
        String crowdinValue = crowdinLine.split("=")[1].trim();
        buffer.append(" = ").append(crowdinValue);
        sourceLine = buffer.toString();
        return sourceLine;
      }      
      
      return "";
    }catch (Exception e) {
      getLog().info(e);
      return "";
    }
  }

  /*
   * Check a line is comment or empty line
   */
  public static boolean isCommentOrEmptyLine(int lineIndex, List<String> linesOfFile) {
    if (linesOfFile == null || linesOfFile.isEmpty())
      return true;
    
    String lineStr = "";
    try{
      lineStr = linesOfFile.get(lineIndex).trim();
    }
    catch (Exception e) {
      return false;
    }
    if (lineStr.length() == 0)
      return true;

    if (lineStr.indexOf("//") == 0 || lineStr.indexOf("/*") == 0 || lineStr.indexOf("*/") >= 0)
      return true;

    int checkIndex = lineIndex-1;

    while (checkIndex >= 0) {
      String previousLineString = linesOfFile.get(checkIndex).trim();
      if (previousLineString.indexOf("/*") == 0)
        return true;
      
      if( previousLineString.indexOf("*/") >= 0)
        return false;
      
      checkIndex--;
    }
    return false;
  }

/**
 * Inject translation from crowdin translation file to resouce bundle file
 * After injection, file @crowdinFilePath will be deleted
 * @param crowdinFilePath: temporaire zip locale file extracted
 * @param resourceMasterFilePath: master file (en) in codebase
 * @param resourceTranslationFilePath: locale file in codebase
 * @return
 */
  public static boolean injectTranslation(String crowdinFilePath, String resourceMasterFilePath, String resourceTranslationFilePath) {
    List<String> crowdinList = readIOSResourceSkipCommentAndEmtyLine(crowdinFilePath);
    List<String> resourcelist = readAllIOSResource(resourceMasterFilePath);

    if (resourcelist == null || resourcelist.isEmpty())
      return false;

    Iterator<String> resourceIterator = resourcelist.iterator();
    int resouceIndex = 0;
    while (resourceIterator.hasNext()) {
      if(getLog().isDebugEnabled()){
        getLog().debug("\n Before Synch: codebase line " + resouceIndex + " = " + resourceIterator.next());
      }
      
      if (isCommentOrEmptyLine(resouceIndex, resourcelist) == false) {
        Iterator<String> crowdinIterator = crowdinList.iterator();
        int crowdinIndex = 0;
        
        while (crowdinIterator.hasNext()) {
          String sourceLine = resourcelist.get(resouceIndex);
          String newSourceLine = updateTranslationByLine(sourceLine, crowdinList.get(crowdinIndex));
          if (newSourceLine.length() > 0 && sourceLine.equals(newSourceLine) == false) {
            resourcelist.set(resouceIndex, newSourceLine);
            crowdinList.remove(crowdinIndex);
            break;
          }
          crowdinIterator.next();
          crowdinIndex++;
        }
        
      }
      if(getLog().isDebugEnabled()){
        getLog().debug("\n After Synch: codebase line " + resouceIndex + " = " + resourcelist.get(resouceIndex));
      }
      resourceIterator.next();
      resouceIndex++;
    }

    boolean saveTranslation = saveListStringToFile(resourceTranslationFilePath, resourcelist);
    
    //Delete crowdin temporary file
    try{
      File file = new File(crowdinFilePath);
      if(file.delete()){
        getLog().info(file.getName() + " is deleted!");
      }else{
        getLog().info("Delete operation is failed.");
      }
    }catch(Exception e){
      getLog().error(e);
    }
    
    return saveTranslation;
  }

  /*
   * Save a list<String> that contains iOS resouce bundle file lines to file
   */
  public static boolean saveListStringToFile(String filePath, List<String> listString) {
    System.out.println("Save translation to file " + filePath);
    try {
      StringBuffer content = new StringBuffer();

      for (String str : listString) {
        content.append(str + System.getProperty("line.separator"));
      }
      FileWriter writer = new FileWriter(filePath);
      writer.write(content.toString());
      writer.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return false;
    }
    return true;
  }

}
