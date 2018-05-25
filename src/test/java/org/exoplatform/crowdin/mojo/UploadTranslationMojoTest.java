package org.exoplatform.crowdin.mojo;

import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class UploadTranslationMojoTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldInitTranslationsOnlyForFilesRelatedToMasterFile() throws Exception {
        // Given
        AbstractCrowdinMojo mojo = Mockito.spy(AbstractCrowdinMojo.class);
        when(mojo.getLanguages()).thenReturn(Arrays.asList("en", "fr", "de"));
        when(mojo.getFactory()).thenReturn(new CrowdinFileFactory(mojo));
        doNothing().when(mojo).prepareAndUploadTranslation(anyString(), any(CrowdinFile.class), any(File.class), anyBoolean());

        File file = folder.newFile("test_en.properties");
        folder.newFile("test_fr.properties");
        folder.newFile("test_de.properties");
        folder.newFile("other_en.properties");
        folder.newFile("other_fr.properties");
        CrowdinFile master = new CrowdinFile(file, "test_en.properties", "properties", "social", false);

        // When
        mojo.initTranslations(master);

        // Then
        // only files related to the master file ("test") must be processed, not others, even if they are in the same folder
        verify(mojo, times(3)).prepareAndUploadTranslation(anyString(), any(CrowdinFile.class), any(File.class), anyBoolean());
    }
}