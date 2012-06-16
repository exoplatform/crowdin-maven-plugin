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
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh
 * tan.pham@exoplatform.com Mar 4, 2009
 */
public class XMLToProps {

  public XMLToProps() {

  }

  @SuppressWarnings("unchecked")
  public static boolean parse(String inputFilePath, Type type) throws Exception {
    File inputFile = new File(inputFilePath);
    if (!inputFile.exists() || !inputFile.isFile())
      return false;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputStream fis = new FileInputStream(inputFile);
    Document document = builder.parse(new InputSource(fis));
    Element bundleElt = document.getDocumentElement();
    StringBuffer bundle = new StringBuffer();
    collect(new LinkedList<String>(), bundleElt, bundle, type);

    String outputFile = inputFilePath.replaceAll("\\.xml", ".properties");
    FileOutputStream fos = new FileOutputStream(outputFile, false);
    fos.write(bundle.toString().getBytes());
    fos.close();
    return true;
  }

  /**
   * Reuse from org.exoplatform.services.resources.XMLResourceBundleParser with modifications
   * to include comments and Gadget locales
   */
  private static void collect(LinkedList<String> path, Element currentElt, StringBuffer bundle, Type type) {
    NodeList children = currentElt.getChildNodes();
    boolean text = true;
    for (int i = 0; i <= children.getLength() - 1; i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        text = false;
        Element childElt = (Element) child;
        String name = childElt.getTagName();
        path.addLast(name);
        collect(path, childElt, bundle, type);
        path.removeLast();
      } else if (child.getNodeType() == Node.COMMENT_NODE) {
        String comment = child.getTextContent();
        comment = makeComment(comment);
        if (comment != null) {
          bundle.append(comment).append("\n");
        }
      }
    }
    if (text && path.size() > 0) {
      String value = currentElt.getTextContent();
      StringBuffer sb = new StringBuffer();
      if (Type.PORTLET.equals(type)) {
        for (Iterator<String> i = path.iterator(); i.hasNext();) {
          String name = i.next();
          sb.append(name);
          if (i.hasNext()) {
            sb.append('.');
          }
        }
      } else {
        sb.append(currentElt.getAttributes().getNamedItem("name").getNodeValue());
        value = value.trim();
      }
      String key = sb.toString();
      bundle.append(key).append("=").append(value.replaceAll("\n", " ")).append("\n");
    }
  }
  
  private static String makeComment(String comment) {
    if (comment != null) {
      comment = comment.trim();
      String[] lines = comment.split("\n");
      if (lines.length <= 0) {
        if (!comment.startsWith("#")) {
          return "#" + comment;
        } else {
          return comment;
        }
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < lines.length; i++) {
        lines[i] = lines[i].trim();
        if (!lines[i].startsWith("#")) {
          sb.append("#").append(lines[i]);
        } else {
          sb.append(lines[i]);
        }
        if (i < lines.length - 1) {
          sb.append("\n");
        }
      }
      return sb.toString();
    }
    return null;
  }

}
