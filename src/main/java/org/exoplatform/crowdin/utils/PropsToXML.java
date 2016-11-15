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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh
 * tan.pham@exoplatform.com Mar 4, 2009
 */
public class PropsToXML {

  private static boolean isLeafNode(Node node) {
    return node.getChildNodes().getLength() == 1 && node.getFirstChild().getNodeType() == Node.TEXT_NODE;
  }

  public static boolean parse(String propsFilePath, Type type) throws Exception {
    File propsFile = new File(propsFilePath);
    if (!propsFile.exists() || !propsFile.isFile())
      return false;
    String fullFileName = propsFile.getName();
    String fileName = fullFileName;
    if (fileName.contains(".")) {
      fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }

    String outputPath = propsFile.getParent();
    outputPath = outputPath + (outputPath.endsWith("/") ? "" : "/");

    String outputFile = outputPath + fileName + ".xml";
    String masterFile = "";

    if (type.equals(Type.PORTLET)) {
      String origFileName = fileName.substring(0, fileName.lastIndexOf("_"));
      masterFile = outputPath + origFileName + ".xml";

      if (!(new File(masterFile)).exists()) masterFile = outputPath + origFileName + "_en.xml";

      //find master file for file has name like "ContentListViewer_pt_BR.xml"
      if (!(new File(masterFile)).exists()) {
        origFileName = origFileName.substring(0, origFileName.lastIndexOf("_"));
        masterFile = outputPath + origFileName + ".xml";
        if (!(new File(masterFile)).exists()) masterFile = outputPath + origFileName + "_en.xml";
      }

      if (!(new File(masterFile)).exists()) throw new FileNotFoundException("Cannot create or update " + outputFile + " as the master file " + origFileName + ".xml (or " + origFileName + "_en.xml)" + " does not exist!");
    } else if (type.equals(Type.GADGET)) {
      masterFile = outputPath + "default.xml";
      if (!(new File(masterFile)).exists()) masterFile = outputPath + "ALL_ALL.xml";
      if (!(new File(masterFile)).exists()) masterFile = outputPath + "en_ALL.xml";
      if (!(new File(masterFile)).exists()) throw new FileNotFoundException("Cannot create or update " + outputFile + " as the master file default.xml (or ALL_ALL.xml, en_ALL.xml) does not exist!");
    }

    // Replace special characters in master file
    FileUtils.replaceCharactersInFile(masterFile, "config/special_character_processing.properties", "PropertiesToXMLSpecialCharacters");


    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(masterFile);
    doc.setXmlStandalone(true);
    XPathFactory factory = XPathFactory.newInstance();

    Properties props = new Properties();
    props.load(new FileInputStream(propsFile));
    Enumeration e = props.propertyNames();
    String key = "";

    if (type.equals(Type.PORTLET)) {
      // loop through all properties
      while (e.hasMoreElements()) {
        key = (String) e.nextElement();
        XPath xpath = factory.newXPath();
        // find the nodes those match the property
        NodeList nodes = null;
        try {
          nodes = (NodeList) xpath.evaluate("//" + key.replace(".", "/"), doc, XPathConstants.NODESET);
          for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            // update only the 1st leaf node matched
            if (isLeafNode(node)) {
              node.setTextContent(props.getProperty(key).trim());
              break;
            }
          }
        } catch (XPathExpressionException xpe) {
          System.out.println("[WARNING] XPath exception when looking for key '" + key + "' in '" + masterFile + "' : " + xpe.getCause().getMessage());
          continue;
        }
      }
    } else if (type.equals(Type.GADGET)) {
      while (e.hasMoreElements()) {
        try {
          key = (String) e.nextElement();
          XPath xpath = factory.newXPath();
          // find that node that match the property
          Node node = (Node) xpath.evaluate("//messagebundle/msg[@name='" + key + "']", doc, XPathConstants.NODE);
          // try again with 'messageBundle' (Social uses this in their resource XMLs)
          if (null == node) node = (Node) xpath.evaluate("//messageBundle/msg[@name='" + key + "']", doc, XPathConstants.NODE);

          if (null == node) {
            System.out.println("[WARNING] Cannot get the node for key '" + key + "' in '" + masterFile);
            continue;
          }
          node.setTextContent(props.getProperty(key).trim());
        } catch (XPathExpressionException xpe) {
          System.out.println("[WARNING] XPath exception when looking for key '" + key + "' in '" + masterFile + "' : " + xpe.getCause().getMessage());
          continue;
        }
      }
    }

    TransformerFactory transformFactory = TransformerFactory.newInstance();
    Transformer transformer = transformFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    Source source = new DOMSource(doc);
    File fout = new File(outputFile);
    // if language is English, update master file and the English file if it exists (do not create new)
    if (propsFilePath.endsWith("en.properties") || propsFilePath.equalsIgnoreCase("en_ALL.properties")) {
      transformer.transform(source, new StreamResult(new File(masterFile)));
      FileUtils.replaceCharactersInFile(masterFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");
      if (fout.exists()) {
        transformer.transform(source, new StreamResult(fout));
        FileUtils.replaceCharactersInFile(outputFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");
      }
    } else {
      // always create new (or update) for other languages
      transformer.transform(source, new StreamResult(fout));
      FileUtils.replaceCharactersInFile(outputFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");
      // revert changes in master file
      FileUtils.replaceCharactersInFile(masterFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");
    }

    return true;
  }

  public static void execShellCommand(String cmd) {
    try {
      Runtime rt = Runtime.getRuntime();
      Process pr = rt.exec(cmd);

      BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

      String line = null;
      while ((line = input.readLine()) != null) {
        System.out.println(line);
      }

      int exitVal = pr.waitFor();
      //System.out.println("'" + cmd + "' exited with error code " + exitVal);

    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

}
