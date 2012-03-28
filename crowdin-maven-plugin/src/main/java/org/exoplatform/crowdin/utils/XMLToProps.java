package org.exoplatform.crowdin.utils;

/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh
 * tan.pham@exoplatform.com Mar 4, 2009
 */
public class XMLToProps {

  public XMLToProps() {

  }

  @SuppressWarnings("unchecked")
  public static boolean parse(String inputFilePath) throws Exception {
    File inputFile = new File(inputFilePath) ;
    if(!inputFile.exists() || !inputFile.isFile()) return false; 
    String fullFileName = inputFile.getName() ;
    String fileName = fullFileName ;
    if(fileName.contains(".")) {
      fileName = fileName.substring(0, fileName.lastIndexOf(".")) ;
    }
    String outputFile = "target/" + fileName + ".properties" ;
    
    InputStream fis = new FileInputStream(inputFile);
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(fis);
    Element root = doc.getRootElement();
    fis.close();

    String content = "";
    Iterator<Object> objs = root.getDescendants();
    while (objs.hasNext()) {
      Object obj = objs.next();
      if (obj instanceof Text) {
        Text textEle = (Text) obj;
        if (textEle.getTextTrim().length() > 0)
          content += makeKey(textEle) + "=" + textEle.getTextTrim() + "\n";
        continue;
      }
      if (obj instanceof Comment) {
        Comment cm = (Comment) obj;
        content += cm.getText() + "\n";
        continue;
      }
    }

    FileOutputStream fos = new FileOutputStream(outputFile, false);
    fos.write(content.getBytes());
    fos.close();
    return true;
  }

  private static String makeKey(Text textEle) {
    Element parent = textEle.getParentElement();
    String key = parent.getName();
    parent = parent.getParentElement();
    while (parent != null && !parent.isRootElement()) {
      key = parent.getName() + "." + key;
      parent = parent.getParentElement();
    }
    return key;
  }
}
