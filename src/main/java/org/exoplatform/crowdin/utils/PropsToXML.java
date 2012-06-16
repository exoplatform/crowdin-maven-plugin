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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh
 * tan.pham@exoplatform.com Mar 4, 2009
 */
public class PropsToXML {

  /** Character flags for XML 1.1. */
  private static final byte XML11CHARS[] = new byte[1 << 16];

  /** XML 1.1 Name start character mask. */
  private static final int MASK_XML11_NAME_START = 0x04;

  static {
    // Initializing the Character Flag Array
    // Code generated by: XML11CharGenerator.
    Arrays.fill(XML11CHARS, 1, 9, (byte) 17); // Fill 8 of value (byte) 17
    XML11CHARS[9] = 35;
    XML11CHARS[10] = 3;
    Arrays.fill(XML11CHARS, 11, 13, (byte) 17); // Fill 2 of value (byte) 17
    XML11CHARS[13] = 3;
    Arrays.fill(XML11CHARS, 14, 32, (byte) 17); // Fill 18 of value (byte) 17
    XML11CHARS[32] = 35;
    Arrays.fill(XML11CHARS, 33, 38, (byte) 33); // Fill 5 of value (byte) 33
    XML11CHARS[38] = 1;
    Arrays.fill(XML11CHARS, 39, 45, (byte) 33); // Fill 6 of value (byte) 33
    Arrays.fill(XML11CHARS, 45, 47, (byte) -87); // Fill 2 of value (byte) -87
    XML11CHARS[47] = 33;
    Arrays.fill(XML11CHARS, 48, 58, (byte) -87); // Fill 10 of value (byte) -87
    XML11CHARS[58] = 45;
    XML11CHARS[59] = 33;
    XML11CHARS[60] = 1;
    Arrays.fill(XML11CHARS, 61, 65, (byte) 33); // Fill 4 of value (byte) 33
    Arrays.fill(XML11CHARS, 65, 91, (byte) -19); // Fill 26 of value (byte) -19
    Arrays.fill(XML11CHARS, 91, 93, (byte) 33); // Fill 2 of value (byte) 33
    XML11CHARS[93] = 1;
    XML11CHARS[94] = 33;
    XML11CHARS[95] = -19;
    XML11CHARS[96] = 33;
    Arrays.fill(XML11CHARS, 97, 123, (byte) -19); // Fill 26 of value (byte) -19
    Arrays.fill(XML11CHARS, 123, 127, (byte) 33); // Fill 4 of value (byte) 33
    Arrays.fill(XML11CHARS, 127, 133, (byte) 17); // Fill 6 of value (byte) 17
    XML11CHARS[133] = 35;
    Arrays.fill(XML11CHARS, 134, 160, (byte) 17); // Fill 26 of value (byte) 17
    Arrays.fill(XML11CHARS, 160, 183, (byte) 33); // Fill 23 of value (byte) 33
    XML11CHARS[183] = -87;
    Arrays.fill(XML11CHARS, 184, 192, (byte) 33); // Fill 8 of value (byte) 33
    Arrays.fill(XML11CHARS, 192, 215, (byte) -19); // Fill 23 of value (byte) -19
    XML11CHARS[215] = 33;
    Arrays.fill(XML11CHARS, 216, 247, (byte) -19); // Fill 31 of value (byte) -19
    XML11CHARS[247] = 33;
    Arrays.fill(XML11CHARS, 248, 768, (byte) -19); // Fill 520 of value (byte) -19
    Arrays.fill(XML11CHARS, 768, 880, (byte) -87); // Fill 112 of value (byte) -87
    Arrays.fill(XML11CHARS, 880, 894, (byte) -19); // Fill 14 of value (byte) -19
    XML11CHARS[894] = 33;
    Arrays.fill(XML11CHARS, 895, 8192, (byte) -19); // Fill 7297 of value (byte) -19
    Arrays.fill(XML11CHARS, 8192, 8204, (byte) 33); // Fill 12 of value (byte) 33
    Arrays.fill(XML11CHARS, 8204, 8206, (byte) -19); // Fill 2 of value (byte) -19
    Arrays.fill(XML11CHARS, 8206, 8232, (byte) 33); // Fill 26 of value (byte) 33
    XML11CHARS[8232] = 35;
    Arrays.fill(XML11CHARS, 8233, 8255, (byte) 33); // Fill 22 of value (byte) 33
    Arrays.fill(XML11CHARS, 8255, 8257, (byte) -87); // Fill 2 of value (byte) -87
    Arrays.fill(XML11CHARS, 8257, 8304, (byte) 33); // Fill 47 of value (byte) 33
    Arrays.fill(XML11CHARS, 8304, 8592, (byte) -19); // Fill 288 of value (byte) -19
    Arrays.fill(XML11CHARS, 8592, 11264, (byte) 33); // Fill 2672 of value (byte) 33
    Arrays.fill(XML11CHARS, 11264, 12272, (byte) -19); // Fill 1008 of value (byte) -19
    Arrays.fill(XML11CHARS, 12272, 12289, (byte) 33); // Fill 17 of value (byte) 33
    Arrays.fill(XML11CHARS, 12289, 55296, (byte) -19); // Fill 43007 of value (byte) -19
    Arrays.fill(XML11CHARS, 57344, 63744, (byte) 33); // Fill 6400 of value (byte) 33
    Arrays.fill(XML11CHARS, 63744, 64976, (byte) -19); // Fill 1232 of value (byte) -19
    Arrays.fill(XML11CHARS, 64976, 65008, (byte) 33); // Fill 32 of value (byte) 33
    Arrays.fill(XML11CHARS, 65008, 65534, (byte) -19); // Fill 526 of value (byte) -19
  }
  
  public static boolean parse(String inputFilePath, Type type) throws Exception {
    File inputFile = new File(inputFilePath);
    if (!inputFile.exists() || !inputFile.isFile())
      return false;
    String fullFileName = inputFile.getName();
    String fileName = fullFileName;
    if (fileName.contains(".")) {
      fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }
    String outputPath = inputFile.getParent();
    String outputFile = outputPath + (outputPath.endsWith("/") ? "" : "/") + fileName + ".xml";
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
    String line;
    boolean isComment = false;
    String comment = "";

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    Element root = null;
    if (Type.PORTLET.equals(type)) {
      root = doc.createElement("bundle");
    } else if (Type.GADGET.equals(type)) {
      root = doc.createElement("messagebundle");
    }

    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0)
        continue;
      line = line.trim();
      if (line.startsWith("#")) {
        if (!isComment) {
          isComment = true;
          comment += "\n  ";
        }
        comment += line + "\n  ";
      } else {
        if (isComment) {
          root.appendChild(doc.createComment(comment));
          comment = "";
          isComment = false;
        }
        makeListTag(line, doc, root, type);
      }
    }
    br.close();

    doc.appendChild(root);
    TransformerFactory transformFactory = TransformerFactory.newInstance();
    Transformer transformer = transformFactory.newTransformer();
    transformer.setOutputProperty("method", "xml");
    transformer.setOutputProperty("encoding", "UTF-8");
    transformer.setOutputProperty("indent", "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    Source source = new DOMSource(doc);
    Result result = new StreamResult(new File(outputFile));
    transformer.transform(source, result);
    return true;
  }

  @SuppressWarnings("unchecked")
  private static void makeListTag(String line, Document doc, Element root, Type type) {
    int eqPos = line.indexOf("=");
    String key = line.substring(0, eqPos).trim();
    String value = line.substring(eqPos + 1, line.length()).trim();

    if (Type.PORTLET.equals(type)) {
      List<String> tagList = getTagList(key);
      Element parentEle = root;
      int i = 0;
      for (String tag : tagList) {
        i++;
        Element child = getChildNode(parentEle, tag);
        if (child != null) {
          parentEle = child;
          continue;
        }
        Element ele = doc.createElement(tag);
        if (i == tagList.size()) {
          if ((value.indexOf("<") >= 0 && value.indexOf(">") >= 0) || value.indexOf("&") >= 0) {
            CDATASection cdata = doc.createCDATASection(value);
            ele.appendChild(cdata);
          } else
            ele.setTextContent(value);
        }
        parentEle.appendChild(ele);
        parentEle = ele;
      }
    } else if (Type.GADGET.equals(type)) {
      Element ele = doc.createElement("msg");
      ele.setAttribute("name", key);
      if ((value.indexOf("<") >= 0 && value.indexOf(">") >= 0) || value.indexOf("&") >= 0) {
        CDATASection cdata = doc.createCDATASection(value);
        ele.appendChild(cdata);
      } else {
        ele.setTextContent(value);
      }
      root.appendChild(ele);
    }

  }
  
  private static Element getChildNode(Element parentEle, String name) {
    NodeList nodes = parentEle.getChildNodes();
    if (nodes != null && nodes.getLength() > 0) {
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i).getNodeName().equals(name)) {
          return (Element) nodes.item(i);
        }
      }
    }
    return null;
  }
  
  private static List<String> getTagList(String key) {
    String[] tags = key.split("\\.");
    List<String> tagList = new ArrayList<String>();
    String temp = "";
    for (int i = tags.length - 1; i >= 0; i--) {
      char ch = tags[i].charAt(0);
      if (isXML11NameStart(ch)) {
        if (temp.length() == 0) {
          tagList.add(0, tags[i]);
        } else {
          temp = tags[i] + "." + temp;
          tagList.add(0, temp);
          temp = "";
        }
      } else {
        if (temp.length() == 0) {
          temp = tags[i];
        } else {
          temp = tags[i] + "." + temp;
        }
      }
    }
    return tagList;
  }

  /**
   * Returns true if the specified character is a valid name start character as
   * defined by production [4] in the XML 1.1 specification.
   * 
   * @param c
   *          The character to check.
   */
  private static boolean isXML11NameStart(int c) {
    return (c < 0x10000 && (XML11CHARS[c] & MASK_XML11_NAME_START) != 0)
        || (0x10000 <= c && c < 0xF0000);
  }
  
}
