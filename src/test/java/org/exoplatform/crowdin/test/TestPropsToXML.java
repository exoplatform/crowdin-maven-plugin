package org.exoplatform.crowdin.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.utils.PropsToXML;
import org.junit.Test;

public class TestPropsToXML extends AbstractTest{

  @Test
  public void test() throws Exception {
    String basePath = Thread.currentThread().getContextClassLoader().getResource("input/properties/CalendarPortlet_ar.properties").getPath();
    basePath = basePath.substring(0, basePath.lastIndexOf("/"));
    PropsToXML.parse(basePath+ "/Agenda-fr.properties", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/Agenda-fr.xml"), new File(basePath+ "/../../expected/xml/Agenda-fr.xml")));
    PropsToXML.parse(basePath+ "/ForumStatistic-fr.properties", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/ForumStatistic-fr.xml"), new File(basePath+ "/../../expected/xml/ForumStatistic-fr.xml")));
    PropsToXML.parse(basePath+ "/LoginHistory-default.properties", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/LoginHistory-default.xml"), new File(basePath+ "/../../expected/xml/LoginHistory-default.xml")));
    PropsToXML.parse(basePath+ "/CalendarPortlet_ar.properties", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/CalendarPortlet_ar.xml"), new File(basePath+ "/../../expected/xml/CalendarPortlet_ar.xml")));
    PropsToXML.parse(basePath+ "/ContentListViewer_fr.properties", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/ContentListViewer_fr.xml"), new File(basePath+ "/../../expected/xml/ContentListViewer_fr.xml")));
    PropsToXML.parse(basePath+ "/ECMS-JCRExplorerPortlet_fr.properties", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/ECMS-JCRExplorerPortlet_fr.xml"), new File(basePath+ "/../../expected/xml/ECMS-JCRExplorerPortlet_fr.xml")));
    PropsToXML.parse(basePath+ "/exogtn-webui_ar.properties", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/exogtn-webui_ar.xml"), new File(basePath+ "/../../expected/xml/exogtn-webui_ar.xml")));
    PropsToXML.parse(basePath+ "/web-contributors_en.properties", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/web-contributors_en.xml"), new File(basePath+ "/../../expected/xml/web-contributors_en.xml")));
  }

}
