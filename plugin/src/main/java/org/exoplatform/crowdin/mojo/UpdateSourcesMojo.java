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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.exoplatform.crowdin.model.CrowdinFile.Type;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.model.SourcesRepository;
import org.exoplatform.crowdin.utils.PropsToXML;

/**
 * @author Philippe Aristote
 */
@Mojo(name = "update-sources")
public class UpdateSourcesMojo extends AbstractCrowdinMojo {

  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    File zip = downloadCrowdInArchive();
    Date downloadDate = new Date();
    //format 20130730-023900
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
    //get the translations status
    File status_trans = new File(getProject().getBasedir(), "report/translation_status.xml");
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(status_trans));
      writer.write(getHelper().getTranslationStatus());
    } catch (IOException e) {
    } finally {
      try {
        if (writer != null)
          writer.close();
      } catch (IOException e) {
      }
    }
    List<String> languagesToProcess = new ArrayList<String>();
    if (getLanguages().contains("all")) {
      languagesToProcess = getLanguagesListFromCrowdInArchive(zip);
    } else {
      languagesToProcess = getLanguages();
    }
    for (String language : languagesToProcess) {
      getLog().info("------------------------------------------------------------------------");
      getLog().info("Updates for locale " + language);
      getLog().info("------------------------------------------------------------------------");
      applyTranslations(getWorkingDir(), zip.getPath(), language);
      for (SourcesRepository repository : getSourcesRepositories()) {
        try {
          File localVersionRepository = new File(getWorkingDir(), repository.getLocalDirectory());
          getLog().info("Extract/Apply/Commit/Push changes on " + repository.getName() + " (branch: " + repository.getBranch() + ")");
          // Create a patch with local changes
          getLog().info("Create patch(s) for " + repository.getLocalDirectory() + "...");
          File patchFile = new File(getProject().getBuild().getDirectory(), repository.getLocalDirectory() + "-" + language + ".patch");
          execGit(localVersionRepository, "diff --ignore-all-space > " + patchFile.getAbsolutePath());
          getLog().info("Done.");
          // Reset our local copy
          getLog().info("Reset repository " + repository.getLocalDirectory() + "...");
          execGit(localVersionRepository, "reset --hard HEAD");
          execGit(localVersionRepository, "clean -fd");
          getLog().info("Done.");
          BufferedReader br = new BufferedReader(new FileReader(patchFile));
          if (br.readLine() == null) {
            getLog().info("No change for locale " + language + " from crowdin extract done on " + DateFormat.getInstance().format(downloadDate));
          } else {
            // Apply the patch
            getLog().info("Apply patch(s) for " + repository.getLocalDirectory() + "...");
            execGit(localVersionRepository, "apply --ignore-whitespace " + patchFile.getAbsolutePath(), element("successCode", "0"), element("successCode", "1"));
            getLog().info("Done.");
            getLog().info("Commit changes for " + repository.getLocalDirectory() + "...");
            execGit(localVersionRepository, "commit -a -m '" + language + " injection on " + format.format(downloadDate) + "'", element("successCode", "0"), element("successCode", "1"));
            getLog().info("Done.");
            // Push it
            if (!isDryRun()) {
              getLog().info("Pushing changes for " + repository.getLocalDirectory() + "...");
              execGit(localVersionRepository, "push origin " + repository.getBranch());
              getLog().info("Done.");
            }
          }
        } catch (Exception e) {
          throw new MojoExecutionException("Error while updating project " + repository.getName(), e);
        }
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
        if (!languagesToProcess.contains(path[0])) languagesToProcess.add(path[0]);
        zipentry = zipinputstream.getNextEntry();
      }// while
      zipinputstream.close();
    } catch (Exception e) {
      getLog().error("Update aborted !", e);
    }
    return languagesToProcess;
  }

  private File downloadCrowdInArchive() {
    File zip = new File(getProject().getBuild().getDirectory(), "all.zip");
    if (!zip.exists() || !isDryRun()) {
      try {
        getHelper().setApprovedOnlyOption();
        getLog().info("Downloading Crowdin translation zip...");
        getHelper().downloadTranslations(zip);
        getLog().info("Downloading done!");
      } catch (Exception e) {
        getLog().error("Error downloading the translations from Crowdin. Exception:\n" + e.getMessage());
      }
    }
    return zip;
  }

  private void applyTranslations(File _destFolder, String _zipFile, String locale) {
    try {
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
        getLog().debug("Processing " + zipentryName);
        zipentryName = CrowdinFileFactory.encodeMinusCharacterInPath(zipentryName, false);
        zipentryName = zipentryName.replace('/', File.separatorChar);
        zipentryName = zipentryName.replace('\\', File.separatorChar);
        String[] path = zipentryName.split(File.separator);
        String lang = path[0];
        String crowdinProj = path[1];
        String proj = path[2];
        String fileName = "";

        // process only the languages specified
        if (!(lang.equalsIgnoreCase(locale))) {
          zipentry = zipinputstream.getNextEntry();
          continue;
        }

        try {
          String cp = crowdinProj + File.separator + proj;
          Properties currentProj = getProperties().get(proj + "/");
          // ignore projects that is not managed by the plugin
          if (currentProj == null) {
            zipentry = zipinputstream.getNextEntry();
            continue;
          }
          String key = zipentryName.substring(zipentryName.indexOf(cp) + cp.length() + 1);
          String value = currentProj.getProperty(key);
          if (value == null) {
            zipentry = zipinputstream.getNextEntry();
            continue;
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

          } else {
            fileName = name + "_" + lang + extension;
          }

          String parentDir = _destFolder + File.separator + proj + File.separator + value.substring(0, value.lastIndexOf(File.separatorChar) + 1);
          parentDir = parentDir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
          String entryName = parentDir + fileName;
          Type resourceBundleType = (key.indexOf("gadget") >= 0) ? Type.GADGET : Type.PORTLET;

          File newFile = new File(entryName.substring(0, entryName.lastIndexOf(File.separatorChar)));
          newFile.mkdirs();

          // Need improve, some portlets in CS use xml format for vi, ar locales
          boolean isXML = (entryName.indexOf(".xml") > 0);

          if (isXML) {
            // create the temporary properties file to be used for PropsToXML (use the file in Crowdin zip)
            entryName = entryName.replaceAll(".xml", ".properties");
            int n;
            FileOutputStream fileoutputstream;
            fileoutputstream = new FileOutputStream(entryName);
            while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
              fileoutputstream.write(buf, 0, n);
            }
            fileoutputstream.close();

            File propertiesFile = new File(entryName);
            PropsToXML.execShellCommand("native2ascii -encoding UTF8 " + propertiesFile.getPath() + " " + propertiesFile.getPath());
            PropsToXML.parse(propertiesFile.getPath(), resourceBundleType);
            propertiesFile.delete();
          } else {
            // identify the master properties file
            String masterFile = parentDir + name + extension;
            if (!new File(masterFile).exists()) masterFile = parentDir + name + "_en" + extension;
            if (!new File(masterFile).exists()) throw new FileNotFoundException("Cannot create or update " + entryName + " as the master file " + name + extension + " (or " + name + "_en" + extension + ")" + " does not exist!");

            // use the master file as a skeleton and fill in with translations from Crowdin
            PropertiesConfiguration config = new PropertiesConfiguration(masterFile);
            PropertiesConfiguration.setDefaultListDelimiter('=');
            config.setEncoding("UTF-8");

            Properties props = new Properties();
            props.load(zipinputstream);
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
              String propKey = (String) e.nextElement();
              config.setProperty(propKey, props.getProperty(propKey));
            }

            // if language is English, update master file and the English file if it exists (do not create new)
            if ("en".equals(lang)) {
              config.save(masterFile);
              // perform post-processing for the output file
              //use shell script
              //ShellScriptUtils.execShellscript("scripts/per-file-processing.sh", masterFile);
              //use java
              org.exoplatform.crowdin.utils.FileUtils.replaceCharactersInFile(masterFile, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");

              if (new File(entryName).exists()) {
                config.save(entryName);
                //use shell script
                //ShellScriptUtils.execShellscript("scripts/per-file-processing.sh", entryName);
                //use java
                org.exoplatform.crowdin.utils.FileUtils.replaceCharactersInFile(entryName, "config/special_character_processing.properties", "UpdateSourceSpecialCharacters");

              }
            } else {
              // always create new (or update) for other languages
              config.save(entryName);
              //use shell script
              //ShellScriptUtils.execShellscript("scripts/per-file-processing.sh", entryName);
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
