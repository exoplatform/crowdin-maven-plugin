package org.exoplatform.crowdin.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.utils.XMLToProps;
import org.junit.Test;

public class TestXMLToProps extends AbstractTest{

  @Test
  public void test() throws Exception {
    String basePath = Thread.currentThread().getContextClassLoader().getResource("input/xml/CalendarPortlet_ar.xml").getPath();
    basePath = basePath.substring(0, basePath.lastIndexOf("/"));
    XMLToProps.parse(basePath+ "/Agenda-fr.xml", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/Agenda-fr.properties"), new File(basePath+ "/../../expected/properties/Agenda-fr.properties")));
    XMLToProps.parse(basePath+ "/ForumStatistic-fr.xml", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/ForumStatistic-fr.properties"), new File(basePath+ "/../../expected/properties/ForumStatistic-fr.properties")));
    XMLToProps.parse(basePath+ "/LoginHistory-default.xml", Type.GADGET);
    assertTrue(isSameContent(new File(basePath+ "/LoginHistory-default.properties"), new File(basePath+ "/../../expected/properties/LoginHistory-default.properties")));
    XMLToProps.parse(basePath+ "/CalendarPortlet_ar.xml", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/CalendarPortlet_ar.properties"), new File(basePath+ "/../../expected/properties/CalendarPortlet_ar.properties")));
    XMLToProps.parse(basePath+ "/ContentListViewer_fr.xml", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/ContentListViewer_fr.properties"), new File(basePath+ "/../../expected/properties/ContentListViewer_fr.properties")));
    XMLToProps.parse(basePath+ "/ECMS-JCRExplorerPortlet_fr.xml", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/ECMS-JCRExplorerPortlet_fr.properties"), new File(basePath+ "/../../expected/properties/ECMS-JCRExplorerPortlet_fr.properties")));
    XMLToProps.parse(basePath+ "/exogtn-webui_ar.xml", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/exogtn-webui_ar.properties"), new File(basePath+ "/../../expected/properties/exogtn-webui_ar.properties")));
    XMLToProps.parse(basePath+ "/web-contributors_en.xml", Type.PORTLET);
    assertTrue(isSameContent(new File(basePath+ "/web-contributors_en.properties"), new File(basePath+ "/../../expected/properties/web-contributors_en.properties")));
  }

}
