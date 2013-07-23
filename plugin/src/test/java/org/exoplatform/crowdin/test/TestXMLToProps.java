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
package org.exoplatform.crowdin.test;

import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.utils.XMLToProps;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestXMLToProps {

  public void testXMLToProps(String baseName, CrowdinFile.Type type) throws Exception {
    XMLToProps.parse(new File(System.getProperty("basedir"), "/target/test-classes/input/xml/" + baseName + ".xml").getAbsolutePath(), type);
    assertEquals("The files differ for " + baseName, FileUtils.fileRead(new File(System.getProperty("basedir"), "/target/test-classes/input/xml/" + baseName + ".properties"), "UTF-8"), FileUtils.fileRead(new File(System.getProperty("basedir"), "/target/test-classes/expected/properties/" + baseName + ".properties"), "UTF-8"));
  }

  @Test
  public void test01() throws Exception {
    testXMLToProps("Agenda-fr", Type.GADGET);
  }

  @Test
  public void test02() throws Exception {
    testXMLToProps("ForumStatistic-fr", Type.GADGET);
  }

  @Test
  public void test03() throws Exception {
    testXMLToProps("LoginHistory-default", Type.GADGET);
  }

  @Test
  public void test04() throws Exception {
    testXMLToProps("CalendarPortlet_ar", Type.PORTLET);
  }

  @Test
  public void test05() throws Exception {
    testXMLToProps("ContentListViewer_fr", Type.PORTLET);
  }

  @Test
  public void test06() throws Exception {
    testXMLToProps("ECMS-JCRExplorerPortlet_fr", Type.PORTLET);
  }

  @Test
  public void test07() throws Exception {
    testXMLToProps("exogtn-webui_ar", Type.PORTLET);
  }

  @Test
  public void test08() throws Exception {
    testXMLToProps("web-contributors_en", Type.PORTLET);
  }

}
