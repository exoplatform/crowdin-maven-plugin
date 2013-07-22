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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;

@Mojo(name = "restore-translation")
public class RestoreTranslationMojo extends AbstractCrowdinMojo {
  private static String ZIP_DIR;
  private static String ZIP_FILE;

  @Parameter(property="prepare",defaultValue="false")
  private boolean isPrepare;

  @Parameter(property="continue",defaultValue="false")
  private boolean isContinue;

  @Parameter(property="action",defaultValue="uploadTranslations")
  private String action;

  @Override
  public void executeMojo() throws MojoExecutionException, MojoFailureException {
    try {
      File zip = new File("crowdin-zip/all.zip");
      if (!zip.exists()) {
        getLog().warn("Crowdin translation zip (../crowdin-zip/all.zip) not found. Nothing to do");
        return;
      }

      ZIP_FILE = zip.getAbsolutePath();
      ZIP_DIR = ZIP_FILE.substring(0, ZIP_FILE.lastIndexOf(".zip")) + "/";

      if (!isContinue) {
        deleteDir(new File(ZIP_DIR));
        extractZip();
        getLog().info("Crowdin translation folder (../crowdin-zip/all/) is ready.");
        if (isPrepare) {
          getLog().info("Please make the necessary modification then continue by running with '-Dcontinue=true'");
        }
      } else {
        if (!new File(ZIP_DIR).exists()) {
          getLog().warn("Crowdin translation folder (../crowdin-zip/all/) not found. Nothing to do");
          return;
        }
      }

      if (!isPrepare) {
        if ("createProject".equals(action)) {
          getLog().info("*** Creating Crowdin project's directory structure...");
          dir2crowdin(ZIP_DIR + "en/");
          return;
        }

        getLog().info("*** Uploading translations...");
        File[] list = new File(ZIP_DIR).listFiles();
        for (File file : list) {
          if (file.isDirectory()) {
            String lang = file.getAbsolutePath().replace(ZIP_DIR, "");
            getLog().info("*** Uploading " + lang + "...");
            trans2crowdin(ZIP_DIR + "/" + lang + "/", lang);
          }
        }
      }

    } catch (Exception e) {
      getLog().error("Exception when running restore-translation: " + e.getMessage(), e);
    }
  }

  private void dir2crowdin(String dirPath) throws Exception {
    File root = new File(dirPath);
    File[] list = root.listFiles();

    for (File file : list) {
      String crowdinPath = file.getAbsolutePath().replace(ZIP_DIR + "en/", "");
      if (file.isDirectory()) {
        String ret = getHelper().addDirectory(crowdinPath);
        if (ret.contains("success")) {
          getLog().info("Creating folder: " + crowdinPath + " [successed]");
        } else {
          getLog().warn("Creating folder: " + crowdinPath + " [FAILED]");
          if (getLog().isDebugEnabled()) {
            getLog().debug(ret);
          }
        }
        dir2crowdin(file.getAbsolutePath());
      } else {
        CrowdinFile cf = getFactory().prepareCrowdinFile(file.getAbsolutePath(), crowdinPath.substring(crowdinPath.lastIndexOf(File.separatorChar)), dirPath.replace(ZIP_DIR + "en/", ""));
        String ret = getHelper().addFile(cf);
        if (ret.contains("success")) {
          getLog().info("Adding file:   " + crowdinPath + " [successed]");
        } else {
          getLog().warn("Adding file:   " + crowdinPath + " [FAILED]");
          if (getLog().isDebugEnabled()) {
            getLog().debug(ret);
          }
        }
      }
    }
  }

  private void trans2crowdin(String dirPath, String lang) throws Exception {
    File root = new File(dirPath);
    File[] list = root.listFiles();

    for (File file : list) {
      String crowdinPath = file.getAbsolutePath().replace(ZIP_DIR + lang + "/", "");
      if (file.isDirectory()) {
        trans2crowdin(file.getAbsolutePath(), lang);
      } else {
        CrowdinFile master = getFactory().prepareCrowdinFile(file.getAbsolutePath(), crowdinPath.substring(crowdinPath.lastIndexOf(File.separatorChar)), dirPath.replace(ZIP_DIR + lang + "/", ""));
        CrowdinTranslation cTran = new CrowdinTranslation(file, crowdinPath.substring(crowdinPath.lastIndexOf(File.separatorChar)), master.getType(), master.getProject(), lang, master, false);
        String ret = getHelper().uploadTranslation(cTran);
        if (ret.contains("success")) {
          getLog().info("Uploading translation: " + crowdinPath + " [successed]");
        } else {
          getLog().warn("Uploading translation: " + crowdinPath + " [FAILED]");
          if (getLog().isDebugEnabled()) {
            getLog().debug(ret);
          }
        }
      }
    }
  }


  private void extractZip() throws Exception {
    byte[] buf = new byte[1024];
    ZipInputStream zipinputstream = null;
    ZipEntry zipentry;
    zipinputstream = new ZipInputStream(new FileInputStream(ZIP_FILE));

    zipentry = zipinputstream.getNextEntry();
    while (zipentry != null) {
      // for each entry to be extracted
      if (zipentry.isDirectory()) {
        zipentry = zipinputstream.getNextEntry();
        continue;
      }

      String tmp = zipentry.getName();
      tmp = CrowdinFileFactory.encodeMinusCharacterInPath(tmp, false);
      tmp = tmp.replace('/', File.separatorChar);
      tmp = tmp.replace('\\', File.separatorChar);
      String[] path = tmp.split(File.separator);
      Properties currentProj = getProperties().get(path[2] + "/");
      // ignore projects that is not managed by the plugin
      if (currentProj == null) {
        zipentry = zipinputstream.getNextEntry();
        continue;
      }

      String zipentryName = ZIP_DIR + zipentry.getName();

      int n;
      FileOutputStream fileoutputstream;
      File newFile = new File(zipentryName.substring(0, zipentryName.lastIndexOf(File.separatorChar)));
      newFile.mkdirs();

      fileoutputstream = new FileOutputStream(zipentryName);

      while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
        fileoutputstream.write(buf, 0, n);
      }

      fileoutputstream.close();

      deleteFirstLine(zipentryName);

      zipinputstream.closeEntry();
      zipentry = zipinputstream.getNextEntry();
    }// while

    zipinputstream.close();
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

  private void deleteFirstLine(String filePath) throws Exception {
    StringBuilder sb = new StringBuilder();
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
    // Ignore first line generated by Crowdin: #X-Generator: crowdin.net
    in.readLine();
    String temp;
    while ((temp = in.readLine()) != null) {
      sb.append(temp).append("\n");
    }
    in.close();
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), "UTF-8"));
    out.write(sb.toString());
    out.close();
  }

} 
