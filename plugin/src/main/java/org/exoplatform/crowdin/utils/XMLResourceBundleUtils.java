/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.crowdin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SAS 30 Sep 2013 Reuse from
 * org.exoplatform.services.resources.XMLResourceBundleParser with modifications
 */
public class XMLResourceBundleUtils {

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
   * Read data from XML InputStream
   * @param in
   * @return a Map<String, List<String>> that contains all xpath:node-data of XML InputStream
   */
  public static Map<String, List<String>> readXMLToMap(InputStream in) {
    if (in == null) {
      throw new IllegalArgumentException("No null input stream allowed");
    }
    return readXMLToMap(new InputSource(in));
  }

  /**
   * Read data from XML Reader
   * @param in
   * @return a Map<String, List<String>> that contains all xpath:node-data of XML InputStream
   */
  public static Map<String, List<String>> readXMLToMap(Reader in) {
    if (in == null) {
      throw new IllegalArgumentException("No null reader allowed");
    }
    return readXMLToMap(new InputSource(in));
  }

  public static Map<String, List<String>> readXMLToMap(InputSource in) {
    if (in == null) {
      throw new IllegalArgumentException("No null input source allowed");
    }
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    HashMap<String, List<String>> bundle = new HashMap<String, List<String>>();
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(in);

      Element bundleElt = document.getDocumentElement();

      LinkedList<String> path = new LinkedList<String>();
      path.addLast("//" + document.getDocumentElement().getNodeName());
      if(getLog().isDebugEnabled())
        getLog().debug("Start traverse XML doc with root Node: " + document.getDocumentElement().getNodeName());
      collect(path, bundleElt, bundle);
      if(getLog().isDebugEnabled())
        getLog().debug("End traverse XML doc with root Node: " + document.getDocumentElement().getNodeName());
    } catch (SAXException e) {
      getLog().error(e);
    } catch (IOException e) {
      getLog().error(e);
    } catch (ParserConfigurationException e) {
      getLog().error(e);
    }

    return bundle;
  }

  private static void collect(LinkedList<String> path, Element currentElt, Map<String, List<String>> bundle) {
    NodeList children = currentElt.getChildNodes();

    boolean text = true;
    for (int i = children.getLength() - 1; i >= 0; i--) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        text = false;
        Element childElt = (Element) child;
        StringBuffer name = new StringBuffer();
        name.append(childElt.getTagName());

        NamedNodeMap atts = child.getAttributes();
        if (atts != null && atts.getLength() > 0) {
          name.append("[");
          for (int j = 0; j < atts.getLength(); j++) {
            name.append("@" + atts.item(j).getNodeName());
            name.append("='" + atts.item(j).getNodeValue() + "'");
            if (j < atts.getLength() - 1)
              name.append(" and ");
          }
          name.append("]");
        }
        if(getLog().isDebugEnabled())
          getLog().debug("Current Node: " + name.toString());
        path.addLast(name.toString());
        collect(path, childElt, bundle);
        path.removeLast();
      }
    }
    if (text && path.size() > 0) {
      String valueStr = currentElt.getTextContent();
      StringBuffer sb = new StringBuffer();
      for (Iterator<String> i = path.iterator(); i.hasNext();) {
        String name = i.next();

        sb.append(name);
        if (i.hasNext()) {
          sb.append('/');
        }
      }
      String key = sb.toString();
      if(getLog().isDebugEnabled())
        getLog().debug("Current path: " + key);
      if (bundle.containsKey(key)) {
        bundle.get(key).add(valueStr);
      } else {
        List<String> value = new LinkedList<String>();
        value.add(valueStr);
        bundle.put(key, value);
      }

    }
  }

  /**
   * Inject translation from a Map (crowdinMapData) to xmlFile (xmlTranslationResouceFilePath)
   * @param xmlTranslationResouceFilePath
   * @param xmlMasterResourceFilePath
   * @param crowdinMapData
   * @return
   */
  public static String saveMapToXMLFile(String xmlTranslationResouceFilePath,
                                        String xmlMasterResourceFilePath,
                                        Map<String, List<String>> crowdinMapData) {
    try {

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      // Document translationResourceDoc =
      // docBuilder.parse(xmlTranslationResoucePathFile);
      Document masterResourceDoc = docBuilder.parse(xmlMasterResourceFilePath);

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      Iterator crowdinIterator = crowdinMapData.entrySet().iterator();
      while (crowdinIterator.hasNext()) {
        Map.Entry mapEntry = (Map.Entry) crowdinIterator.next();
        List<String> listData = (List<String>) mapEntry.getValue();
        String nodeData = "";
        if (mapEntry != null && listData.size() > 0) {
          nodeData = listData.get(0);
        }
        if(getLog().isDebugEnabled())
          getLog().debug("The key is: " + mapEntry.getKey() + ",value is :" + nodeData);
        Node node = null;
        try {
          node = (Node) xpath.evaluate(mapEntry.getKey() + "", masterResourceDoc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
          continue;
        }

        // update new value for node
        if (node != null) {
          node.setTextContent(null != nodeData ? nodeData : "");
        }
      }

      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      DOMSource source = new DOMSource(masterResourceDoc);
      StreamResult result = new StreamResult(new File(xmlTranslationResouceFilePath));
      transformer.transform(source, result);

      // return XML file path after save mapData to XML file
      return xmlTranslationResouceFilePath;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }

  /**
   * Inject translation from crowdin to code base resource translation
   * 
   * @param crowdinFile
   * @param resourceMasterFile
   * @param resourceTranslationFilePath
   * @return
   */
  public static String injectTranslation(InputSource crowdinFile,
                                         String resourceTranslationFilePath,
                                         String resourceMasterFilePath) {

    Map<String, List<String>> crowdinDataMap = new HashMap<String, List<String>>();
    try {
      // Read Crowdin translation into map
      crowdinDataMap = readXMLToMap(crowdinFile);
      resourceTranslationFilePath = saveMapToXMLFile(resourceTranslationFilePath, resourceMasterFilePath, crowdinDataMap);
      return resourceTranslationFilePath;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Inject translation from crowdin to code base resource translation
   * @param crowdinFile
   * @param resourceTranslationFilePath
   * @param resourceMasterFilePath
   * @return
   */
  public static String injectTranslation(InputStream crowdinFile,
                                         String resourceTranslationFilePath,
                                         String resourceMasterFilePath) {

    if (crowdinFile == null) {
      throw new IllegalArgumentException("No null input stream allowed");
    }
    return injectTranslation(new InputSource(crowdinFile), resourceTranslationFilePath, resourceMasterFilePath);
  }

  /**
   * Inject translation from crowdin to code base resource translation
   * @param crowdinFile
   * @param resourceTranslationFilePath
   * @param resourceMasterFilePath
   * @return
   */
  public static String injectTranslation(Reader crowdinFile, String resourceTranslationFilePath, String resourceMasterFilePath) {

    if (crowdinFile == null) {
      throw new IllegalArgumentException("No null input stream allowed");
    }
    return injectTranslation(new InputSource(crowdinFile), resourceTranslationFilePath, resourceMasterFilePath);
  }
}
