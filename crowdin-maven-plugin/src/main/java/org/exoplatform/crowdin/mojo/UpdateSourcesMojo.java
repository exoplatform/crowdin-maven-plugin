package org.exoplatform.crowdin.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal update
 * @author Philippe Aristote
 */
public class UpdateSourcesMojo extends AbstractCrowdinMojo {

	@Override
	public void executeMojo() throws MojoExecutionException, MojoFailureException {
		try {
			getHelper().downloadTranslations();
		} catch (FileNotFoundException e) {
			getLog().error("Error downloading the translations from Crowdin. Exception:\n"+e.getMessage());
		} catch (IOException e) {
			getLog().error("Error downloading the translations from Crowdin. Exception:\n"+e.getMessage());
		}
		File zip = new File("target/all.zip");
		if (zip.exists()) {
			extractZip("target/translations/", zip.getPath());
		}
	}
	
	private void extractZip(String _destFolder, String _zipFile) {
		try {
	        String destinationname = _destFolder;
	        byte[] buf = new byte[1024];
	        ZipInputStream zipinputstream = null;
	        ZipEntry zipentry;
	        zipinputstream = new ZipInputStream(
	                new FileInputStream(_zipFile));

	        zipentry = zipinputstream.getNextEntry();
	        while (zipentry != null) {
	            //for each entry to be extracted
	            String entryName = destinationname + zipentry.getName();
	            entryName = entryName.replace('/', File.separatorChar);
	            entryName = entryName.replace('\\', File.separatorChar);
	            System.out.println("entryname " + entryName);
	            int n;
	            FileOutputStream fileoutputstream;
	            File newFile = new File(entryName);
	            if (zipentry.isDirectory()) {
	                if (!newFile.mkdirs()) {
	                    break;
	                }
	                zipentry = zipinputstream.getNextEntry();
	                continue;
	            }

	            fileoutputstream = new FileOutputStream(entryName);

	            while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
	                fileoutputstream.write(buf, 0, n);
	            }

	            fileoutputstream.close();
	            zipinputstream.closeEntry();
	            zipentry = zipinputstream.getNextEntry();

	        }//while

	        zipinputstream.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
