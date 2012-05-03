package org.exoplatform.crowdin.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.utils.PropsToXML;

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
      getLog().error("Error downloading the translations from Crowdin. Exception:\n"
          + e.getMessage());
    } catch (IOException e) {
      getLog().error("Error downloading the translations from Crowdin. Exception:\n"
          + e.getMessage());
    }
    File zip = new File("target/all.zip");
    if (zip.exists()) {
      extractZip(getStartDir()+"temp/crowdin/translations/", zip.getPath());
    }
  }

  private void extractZip(String _destFolder, String _zipFile) {
    try {
      String destinationname = _destFolder;
      deleteDir(new File(_destFolder));
      byte[] buf = new byte[1024];
      ZipInputStream zipinputstream = null;
      ZipEntry zipentry;
      zipinputstream = new ZipInputStream(new FileInputStream(_zipFile));

      zipentry = zipinputstream.getNextEntry();
      while (zipentry != null) {
        // for each entry to be extracted
        if (zipentry.isDirectory()) {
          zipentry = zipinputstream.getNextEntry();
          continue;
        }
        String zipentryName = zipentry.getName();
        zipentryName = CrowdinFileFactory.encodeMinusCharacterInPath(zipentryName, false);
        zipentryName = zipentryName.replace('/', File.separatorChar);
        zipentryName = zipentryName.replace('\\', File.separatorChar);
        String[] path = zipentryName.split(File.separator);
        String lang = path[0];
        String proj = path[2];
        Properties currentProj = getProperties().get(proj+"/");
        String key = zipentryName.substring(zipentryName.indexOf(proj) + proj.length() + 1);
        String value = currentProj.getProperty(key);
        zipentryName = zipentryName.substring(0, zipentryName.indexOf(proj) + proj.length());
        
        lang = CrowdinTranslation.encodeLanguageName(lang, false);
        String fileName = value.substring(value.lastIndexOf(File.separatorChar)+1);
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf("."));
        if(name.lastIndexOf("_en")>0){
          name = name.substring(0, name.lastIndexOf("_en"));
        }
        fileName =  name+ "_" + lang + extension ;
        zipentryName = proj + "/" + value.substring(0, value.lastIndexOf(File.separatorChar)+1)+fileName;
        String entryName = destinationname + zipentryName;
        entryName = entryName.replace('/', File.separatorChar);
        entryName = entryName.replace('\\', File.separatorChar);
        System.out.println("entryname " + entryName);
        boolean isXML = (entryName.indexOf(".xml")>0);
        if(isXML){
          entryName = entryName.replaceAll(".xml", ".properties");
        }
        int n;
        FileOutputStream fileoutputstream;
        File newFile = new File(entryName.substring(0, entryName.lastIndexOf(File.separatorChar)));
        newFile.mkdirs();

        fileoutputstream = new FileOutputStream(entryName);

        while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
          fileoutputstream.write(buf, 0, n);
        }

        fileoutputstream.close();
        if(isXML){
        File propertiesFile = new File(entryName);
        PropsToXML.parse(propertiesFile.getPath());
        propertiesFile.delete();
        }
        
        zipinputstream.closeEntry();
        zipentry = zipinputstream.getNextEntry();

      }// while

      zipinputstream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

}
