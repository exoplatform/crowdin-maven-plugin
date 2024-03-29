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
package org.exoplatform.crowdin.mojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.utils.IOSResouceBundleFileUtils;
import org.exoplatform.crowdin.utils.PropsToXML;
import org.exoplatform.crowdin.utils.XMLResourceBundleUtils;

/**
 * Update projects sources from crowdin translations
 */
@Mojo(name = "update-sources")
public class UpdateSourcesMojo extends AbstractCrowdinMojo {

  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    List<String> languagesToProcess = new ArrayList<String>();
    if (getLanguages().contains("all")) {
      languagesToProcess = getLanguagesListFromCrowdInArchive(crowdInArchive);
    } else {
      languagesToProcess = getLanguages();
    }

    if(languagesToProcess == null || languagesToProcess.isEmpty()) {
      getLog().info("No language to process");
    }

    String currentDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());

    for (String language : languagesToProcess) {
      getLog().info("Updates for locale " + language);
      applyTranslations(getWorkingDir(), crowdInArchive.getPath(), language);
      try {
        File localVersionRepository = getWorkingDir();
        getLog().info("Extract/Apply/Commit/Push changes");
        // Check changes
        getLog().info("Check changes...");
        File statusFile = new File(getProject().getBuild().getDirectory(), "translations-" + language + ".status");
        execGit(localVersionRepository, "status -s > " + statusFile.getAbsolutePath());
        getLog().info("Done.");
        BufferedReader br = new BufferedReader(new FileReader(statusFile));
        if (br.readLine() == null) {
          getLog().info("No change for locale " + language + " from crowdin extract done on " + currentDate);
        } else {
          // Commit the changes
          execGit(localVersionRepository, "add .");
          getLog().info("Commit changes...");
          execGit(localVersionRepository, "commit -a -m '" + language + " injection on " + currentDate + "'");
          getLog().info("Done.");
          // Push it
          if (!isDryRun()) {
            getLog().info("Pushing changes...");
            execGit(localVersionRepository, "push origin HEAD");
            getLog().info("Done.");
          }
        }
      } catch (Exception e) {
        throw new MojoExecutionException("Error while updating project", e);
      }
    }

  }

  private List<String> getLanguagesListFromCrowdInArchive(File zip) {
    List<String> languagesToProcess = new ArrayList<String>();
    // Let's extract the list of languages from crowdIn archive
    try {
      ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zip));
      ZipEntry zipentry = zipinputstream.getNextEntry();
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
        if (!languagesToProcess.contains(path[0]))
          languagesToProcess.add(path[0]);
        zipentry = zipinputstream.getNextEntry();
      }// while
      zipinputstream.close();
    } catch (Exception e) {
      getLog().error("Update aborted !", e);
    }
    return languagesToProcess;
  }

  private void applyTranslations(File _destFolder, String _zipFile, String locale) throws MojoExecutionException {
    ZipInputStream zipinputstream = null;

    try {
      zipinputstream = new ZipInputStream(new FileInputStream(_zipFile));
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Cannot found translations archive file at " + _zipFile + " - Update Aborted.", e);
    }

    try {
      Properties currentProj = getProperties();
      // ignore projects that is not managed by the plugin
      if (currentProj == null) {
        return;
      }

      String baseDir = currentProj.getProperty("baseDir");

      byte[] buf = new byte[1024];
      ZipEntry zipentry;

      zipentry = zipinputstream.getNextEntry();
      while (zipentry != null) {
        // for each entry to be extracted
        if (zipentry.isDirectory()) {
          zipentry = zipinputstream.getNextEntry();
          continue;
        }
        String zipentryName = zipentry.getName();
        getLog().debug("Processing : " + zipentryName);
        zipentryName = CrowdinFileFactory.encodeMinusCharacterInPath(zipentryName, false);
        zipentryName = zipentryName.replace('/', File.separatorChar);
        zipentryName = zipentryName.replace('\\', File.separatorChar);
        String[] path = zipentryName.split(File.separator);
        String lang = path[0];
        String fileName = "";
        String cp = "";
        String proj="";
        for (int i=1;i<path.length-1;i++) {
          if (i==1 || i==2) {
            cp += path[i] + File.separator;
          }
          if (i==path.length-2) {
            proj=path[i];
          }
        }
        // process only the languages specified
        if (!(lang.equalsIgnoreCase(locale))) {
          zipentry = zipinputstream.getNextEntry();
          continue;
        }

        // process only files of the current project
        if(StringUtils.isEmpty(baseDir) || !cp.equals(baseDir)) {
          zipentry = zipinputstream.getNextEntry();
          continue;
        }

        try {
          String key = zipentryName.substring(zipentryName.indexOf(cp) + cp.length());
          String value = currentProj.getProperty(key);
          
          if (value == null) {
            zipentry = zipinputstream.getNextEntry();
            continue;
          }
          
          /**
           * if android, don't save to default master folder but save to
           * "values-language" folder (for example) res/values/strings.xml > res/values-fr/strings.xml
           */
          if (zipentryName.contains("android")) {
            if (!locale.contains("en")) {
              String localizable = CrowdinTranslation.encodeAndroidLocale(locale);
              value = value.replace("res/values/", "res/values-" + localizable + "/");
            }
          }
          /**
           * if iOS, don't save to default master folder but save to
           * "language.proj" folder (for example)
           * /Resources/en.lproj/Localizable.string > /Resources/fr.lproj/Localizable.string
           */
          else if (zipentryName.contains("ios")) {
            if (!locale.contains("en")) {
              String localizable = CrowdinTranslation.encodeIOSLocale(locale);
              value = value.replace("en.lproj", localizable + ".lproj");
            }
          }
          zipentryName = zipentryName.substring(0, zipentryName.indexOf(proj) + proj.length());

          lang = CrowdinTranslation.encodeLanguageName(lang, false);

          fileName = value.substring(value.lastIndexOf(File.separatorChar) + 1);

          getLog().info("Updating " + zipentryName + " - " + value.substring(0, value.lastIndexOf(File.separatorChar) + 1) + fileName);

          String name = fileName.substring(0, fileName.lastIndexOf("."));
          String extension = fileName.substring(fileName.lastIndexOf("."));
          if (name.lastIndexOf("_en") > 0) {
            name = name.substring(0, name.lastIndexOf("_en"));
          }

          if (key.contains("gadget") || value.contains("gadget")) {
            if ("default".equalsIgnoreCase(name)) {
              fileName = lang + extension;
            } else if (name.contains("_ALL")) {
              fileName = lang + "_ALL" + extension;
            } else {
              fileName = name + "_" + lang + extension;
            }

          }
          //if android, don't change xml to properties
          else if (zipentryName.contains("android") ){
            fileName = name + ".xml";
          }
          //if iOS
          else if(zipentryName.contains("ios") ){
            fileName = name + extension;
          }
          else {
            fileName = name + "_" + lang + extension;
          }

          String parentDir = _destFolder + File.separator + value.substring(0, value.lastIndexOf(File.separatorChar) + 1);
          getLog().debug("parentDir : " + parentDir);
          parentDir = parentDir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
          String entryName = parentDir + fileName;
          Type resourceBundleType = (key.indexOf("gadget") >= 0) ? Type.GADGET : Type.PORTLET;

          File newFile = new File(entryName.substring(0, entryName.lastIndexOf(File.separatorChar)));
          newFile.mkdirs();

          // Need improve, some portlets in CS use xml format for vi, ar locales
          boolean isXML = (entryName.indexOf(".xml") > 0);

          if (isXML) {
            //if is Android resouce bundle
            if(zipentryName.contains("mobile") && zipentryName.contains("android")){
              String resourceTranslationFilePath  = parentDir + name + extension;
              String localizable = CrowdinTranslation.encodeAndroidLocale(locale);
              String masterFilePath = resourceTranslationFilePath.replaceAll("res/values-" + localizable, "res/values");

              //create temporary file to persists zipinputstream
              int n;
              FileOutputStream fileoutputstream;
              fileoutputstream = new FileOutputStream(resourceTranslationFilePath+".ziptempo");
              while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                fileoutputstream.write(buf, 0, n);
              }
              fileoutputstream.close();
              String crowdinFilePath = resourceTranslationFilePath + ".ziptempo";
              FileInputStream input = new FileInputStream(crowdinFilePath);
              XMLResourceBundleUtils.setLog(getLog());
              XMLResourceBundleUtils.injectTranslation(input, resourceTranslationFilePath, masterFilePath);
              
              //delete ziptempo file
              try{
                File file = new File(crowdinFilePath);
                if(file.delete()){
                  if(getLog().isDebugEnabled())
                    getLog().debug(file.getName() + " is deleted!");
                }else{
                  if(getLog().isDebugEnabled())
                    getLog().debug("Delete operation is failed.");
                }
              }catch(Exception e){
                getLog().error(e);
              }
            }
            else{
              // create the temporary properties file to be used for PropsToXML (use the file in Crowdin zip) 
              //if not in mobile project, convert xml to properties
              if (!zipentryName.contains("mobile")) {
                entryName = entryName.replaceAll(".xml", ".properties");
              }
              
              int n;
              FileOutputStream fileoutputstream;
              fileoutputstream = new FileOutputStream(entryName);
              while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                fileoutputstream.write(buf, 0, n);
              }
              fileoutputstream.close();
  
              File propertiesFile = new File(entryName);     
              
              // don't convert to ascii in mobile project
              if (!zipentryName.contains("mobile")) {
                PropsToXML.execShellCommand("native2ascii -encoding UTF8 " + propertiesFile.getPath()
                    + " " + propertiesFile.getPath());
              }
  
              PropsToXML.parse(propertiesFile.getPath(), resourceBundleType);
              propertiesFile.delete();
            }
          }
          
          // when project is iOS
          else if(zipentryName.contains("ios")){
            
            String localFile = parentDir + name + extension;
            String localizable = CrowdinTranslation.encodeIOSLocale(locale);
            String masterFile = localFile.replace(localizable + ".lproj", "en.lproj");   
            String resourceTranslationFilePath = localFile;            
            //Write tempo zipinputstream
            int n;
            FileOutputStream fileoutputstream;
            fileoutputstream = new FileOutputStream(resourceTranslationFilePath+".ziptempo");
            while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
              fileoutputstream.write(buf, 0, n);
            }
            fileoutputstream.close();
            
            String crowdinFilePath = resourceTranslationFilePath + ".ziptempo";
              
              //master file code base EN
            String resourceMasterFilePath = masterFile;
              
            //translation file code base LANGUAGE
            IOSResouceBundleFileUtils.setLog(getLog());
            IOSResouceBundleFileUtils.injectTranslation(crowdinFilePath, resourceMasterFilePath, resourceTranslationFilePath);
          
          } else {
            // identify the master properties file
            String masterFile = parentDir + name + extension;
            if (!new File(masterFile).exists())
              masterFile = parentDir + name + "_en" + extension;
            if (!new File(masterFile).exists())
              throw new FileNotFoundException("Cannot create or update " + entryName + " as the master file " + name + extension + " (or " + name + "_en" + extension + ")" + " does not exist!");

            // use the master file as a skeleton and fill in with translations from Crowdin
            PropertiesConfiguration config = new PropertiesConfiguration(masterFile);
            PropertiesConfiguration.setDefaultListDelimiter('=');
            config.setEncoding("UTF-8");

            Properties propsCrowdin = new Properties();
            propsCrowdin.load(zipinputstream);            
            
            Properties props = new Properties();
            props.load(new FileInputStream(new File(masterFile)));
            
            
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
              String propKey = (String) e.nextElement();
              String crowdinValue = propsCrowdin.getProperty(propKey);
             
              if (null != crowdinValue && crowdinValue.length() > 0)
                config.setProperty(propKey, crowdinValue);
            }

            // if language is English, update master file and the English file if it exists (do not create new)
            if ("en".equals(lang)) {
              config.save(masterFile);
              // perform post-processing for the output file
              org.exoplatform.crowdin.utils.FileUtils.replaceCharactersInFile(masterFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");

              if (new File(entryName).exists()) {
                config.save(entryName);
                //use java
                org.exoplatform.crowdin.utils.FileUtils.replaceCharactersInFile(entryName, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");

              }
            } else {
              // always create new (or update) for other languages
              config.save(entryName);
              //user java
              org.exoplatform.crowdin.utils.FileUtils.replaceCharactersInFile(entryName, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");
            }
          }
          zipinputstream.closeEntry();
        } catch (Exception e) {
          getLog().warn("Error while applying change for " + zipentryName + " - " + fileName + " : " + e.getMessage());
        }
        zipentry = zipinputstream.getNextEntry();
      }// while

      zipinputstream.close();
    } catch (Exception e) {
      getLog().error("Update aborted !", e);
    }
  }
}
