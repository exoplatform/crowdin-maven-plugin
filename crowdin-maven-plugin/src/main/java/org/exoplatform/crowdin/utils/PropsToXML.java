package org.exoplatform.crowdin.utils;

/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh
 * tan.pham@exoplatform.com Mar 4, 2009
 */
public class PropsToXML {

  public static boolean parse(String inputFilePath) throws Exception {
    File inputFile = new File(inputFilePath) ;
    if(!inputFile.exists() || !inputFile.isFile()) return false; 
    String fullFileName = inputFile.getName() ;
    String fileName = fullFileName ;
    if(fileName.contains(".")) {
      fileName = fileName.substring(0, fileName.lastIndexOf(".")) ;
    }
    String outputPath = inputFile.getParent() ;
    String outputFile = outputPath + (outputPath.endsWith("/") ? "" : "/") + fileName + ".xml" ;
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
    String line;
    boolean isComment = false;
    String comment = "";

    Document doc = new Document();
    Element root = new Element("bundle");

    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0)
        continue;

      line = line.trim() ;
      if (line.startsWith("#")) {
        if (!isComment) {
          isComment = true;
          comment += "\n  ";
        }
        comment += line + "\n  ";
      } else {
        if (isComment) {
          root.addContent(new Comment(comment));
          comment = "";
          isComment = false;
        }
        makeListTag(line, root);
      }
    }
    br.close();

    doc.setRootElement(root);
    XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
    FileOutputStream fos = new FileOutputStream(outputFile, false);
    output.output(doc, fos);
    return true;
  }

  @SuppressWarnings("unchecked")
  private static void makeListTag(String line, Element root) {
    int eqPos = line.indexOf("=");
    String key = line.substring(0, eqPos).trim();
    String value = line.substring(eqPos + 1, line.length()).trim();

    String[] tags = key.split("\\.");
    Element parentEle = root;
    for (int i = 0; i < tags.length; i++) {
      Element temp = parentEle.getChild(tags[i]);
      if (temp != null) {
        parentEle = temp;
        continue;
      }
      Element ele = new Element(tags[i]);
      if (i == tags.length - 1) {
        if ((value.indexOf("<") >= 0 && value.indexOf(">") >= 0) || value.indexOf("&") >= 0) {
          CDATA cdata = new CDATA(value);
          ele.addContent(cdata);
        } else
          ele.setText(value);
      }
      parentEle.getChildren().add(ele);
      parentEle = ele;
    }
  }

}
