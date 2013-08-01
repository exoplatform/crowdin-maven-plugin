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

import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.utils.FileUtils;

/**
 * @author Philippe Aristote
 */
@Mojo(name = "init")
public class InitCrowdinMojo extends AbstractCrowdinMojo {

  /**
   * Entry point of the goal. AbstractMojo.execute() is actually overridden in AbstractCrowdinMojo.
   */
  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    if (!isAllPropertyFilesExisted() && !isForce()) {
      getLog().info("\n\n\n");
      getLog().info("----------------------------------------------------------------------------------------\n\n"
                        + "There are nonexistent properties files! Check again and update properties configuration files or run following command to "
                        + "continue:\n mvn clean install -Pinit -Dforce=true\n");
      getLog().info("----------------------------------------------------------------------------------------\n\n\n");
      return;
    }
    // Iterate on each project defined in crowdin.properties
    for (String proj : getProperties().keySet()) {
      getLog().info("Starting project " + proj);
      // Get the Properties of the current project, i.e. the content of cs-2.2.x.properties
      Properties currentProj = getProperties().get(proj);
      String baseDir = currentProj.getProperty("baseDir");
      Set<Object> files = currentProj.keySet();
      // Iterate on each file of the current project
      for (Object file : files) {
        // Skip the property baseDir
        if (file.equals("baseDir")) continue;
        // Construct the full path to the file
        String filePath = getWorkingDir() + proj + currentProj.getProperty(file.toString());
        CrowdinFile master = getFactory().prepareCrowdinFile(filePath, file.toString(), baseDir);
        if (master.getFile().exists()) {
          boolean initialized = initFile(master);
          if (initialized) {
            initTranslations(master);
          }
        }
      }
      getLog().info("Finished project " + proj);
    }
  }

  /**
   * A function that initializes a File in Crowdin
   * - creates parent folder(s) if they don't exist
   * - create the file if it doesn't exist
   * - upload translations for each file if they don't exist
   *
   * @param _file the File to initialize in Crowdin
   * @return true if file is created, false if file is existed on Crowdin
   */
  private boolean initFile(CrowdinFile _file) {
    String fileN = _file.getFile().getName();
    if (getLog().isDebugEnabled()) getLog().debug("*** Initializing: " + fileN);
    // Making sure the file is a master file and not a translation
    if (_file.getClass().equals(CrowdinFile.class)) {
      if (getLog().isDebugEnabled()) getLog().debug("*** Init dir");
      initDir(_file.getCrowdinPath());
      try {
        if (getLog().isDebugEnabled()) getLog().debug("*** Checking whether file: " + _file.getCrowdinPath() + " exists.");
        if (!getHelper().elementExists(_file.getCrowdinPath())) {

          //escape special character before init
          FileUtils.replaceCharactersInFile(_file.getFile().getPath(), "config/special_character_processing.properties", "EscapeSpecialCharactersBeforeSyncFromCodeToCrowdin");

          if (getLog().isDebugEnabled()) getLog().debug("*** Add file: " + _file.getCrowdinPath());
          String result = getHelper().addFile(_file);
          if (result.contains("success")) {
            getLog().info("File " + fileN + " created succesfully.");

            //remove escape special character before init
            FileUtils.replaceCharactersInFile(_file.getFile().getPath(), "config/special_character_processing.properties", "EscapeSpecialCharactersAfterSyncFromCodeToCrowdin");

            return true;
          } else {
            getLog().warn("Cannot create file '" + _file.getFile().getPath() + "'. Reason:\n" + result);
          }

          //remove escape special character before init
          FileUtils.replaceCharactersInFile(_file.getFile().getPath(), "config/special_character_processing.properties", "EscapeSpecialCharactersAfterSyncFromCodeToCrowdin");
        }
      } catch (MojoExecutionException e) {
        getLog().error("Error while creating file '" + _file.getFile().getPath() + "'. Exception:\n" + e.getMessage());
      }
    }
    return false;
  }

}
