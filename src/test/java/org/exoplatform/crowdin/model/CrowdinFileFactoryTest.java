package org.exoplatform.crowdin.model;

import org.apache.maven.plugin.logging.Log;
import org.exoplatform.crowdin.mojo.AbstractCrowdinMojo;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CrowdinFileFactoryTest {

    @Test
    public void shouldCheckIfFilenamesMatchTranslationFilenamePattern() {
        AbstractCrowdinMojo mojo = mock(AbstractCrowdinMojo.class);
        Log log = mock(Log.class);
        when(mojo.getLog()).thenReturn(log);
        CrowdinFileFactory factory = new CrowdinFileFactory(mojo);

        assertTrue(factory.isTranslation("file1_en.properties"));
        assertTrue(factory.isTranslation("file1_fr.properties"));
        assertTrue(factory.isTranslation("file1_fil.properties"));
        assertTrue(factory.isTranslation("file1_pt_BR.properties"));
        assertTrue(factory.isTranslation("other_file_1_pt_BR.properties"));
        assertTrue(factory.isTranslation("file1_de.xml"));
        assertFalse(factory.isTranslation("file1.properties"));
        assertFalse(factory.isTranslation("file1.xml"));
    }
}