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

import java.io.File;


import org.codehaus.plexus.util.FileUtils;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.utils.PropsToXML;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore("Must be fixed !!!")
public class TestPropsToXML {

  private void testPropsToXML(String baseName, CrowdinFile.Type type) throws Exception {
    PropsToXML.parse(new File(System.getProperty("basedir"), "/target/test-classes/input/properties/" + baseName + ".properties").getAbsolutePath(), type);
    assertEquals("The files differ for " + baseName, FileUtils.fileRead(new File(System.getProperty("basedir"), "/target/test-classes/input/properties/" + baseName + ".xml"), "UTF-8"), FileUtils.fileRead(new File(System.getProperty("basedir"), "/target/test-classes/expected/xml/" + baseName + ".xml"), "UTF-8"));
  }


  @Test
  public void test01() throws Exception {
    testPropsToXML("Agenda-fr", CrowdinFile.Type.GADGET);
  }

  @Test
  public void test02() throws Exception {
    testPropsToXML("ForumStatistic-fr", CrowdinFile.Type.GADGET);
  }

  @Test
  public void test03() throws Exception {
    testPropsToXML("LoginHistory-default", CrowdinFile.Type.GADGET);
  }

  @Test
  public void test04() throws Exception {
    testPropsToXML("CalendarPortlet_ar", CrowdinFile.Type.PORTLET);
  }

  @Test
  public void test05() throws Exception {
    testPropsToXML("ContentListViewer_fr", CrowdinFile.Type.PORTLET);
  }

  @Test
  public void test06() throws Exception {
    testPropsToXML("ECMS-JCRExplorerPortlet_fr", CrowdinFile.Type.PORTLET);
  }

  @Test
  public void test07() throws Exception {
    testPropsToXML("exogtn-webui_ar", CrowdinFile.Type.PORTLET);
  }

  @Test
  public void test08() throws Exception {
    testPropsToXML("web-contributors_en", CrowdinFile.Type.PORTLET);
  }

}
