/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.crowdin.mojo;

import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.exoplatform.crowdin.model.CrowdinFile;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * May 4, 2012  
 */

/**
 * @goal sync
 */
public class SyncSourcesMojo extends AbstractCrowdinMojo {

  /**
   * Entry point of the goal. AbstractMojo.execute() is actually overridden in
   * AbstractCrowdinMojo.
   */
  @Override
  public void executeMojo() throws MojoExecutionException, MojoFailureException {
    if (!isAllPropertyFilesExisted() && !isForce()) {
      getLog().info("\n\n\n");
      getLog().info("----------------------------------------------------------------------------------------\n\n"
          + "There are nonexistent properties files! Check again and update properties configuration files or run following command to "
          + "continue:\n mvn clean install -Psync -Dforce=true \n"
          + "Warning: All Crowdin files corresponding to nonexistent properties files will be deleted after execute above command.\n");
      getLog().info("----------------------------------------------------------------------------------------\n\n\n");
      return;
    }
    // Iterate on each project defined in crowdin.properties
    for (String proj : getProperties().keySet()) {
      getLog().info("Starting project " + proj);
      // Get the Properties of the current project, i.e. the content of
      // cs-2.2.x.properties
      Properties currentProj = getProperties().get(proj);
      String baseDir = currentProj.getProperty("baseDir");
      Set<Object> files = currentProj.keySet();
      // Iterate on each file of the current project
      for (Object file : files) {
        // Skip the property baseDir
        if (file.equals("baseDir"))
          continue;
        // Construct the full path to the file
        String filePath = getStartDir() + proj + currentProj.getProperty(file.toString());
        CrowdinFile master = getFactory().prepareCrowdinFile(filePath, file.toString(), baseDir);
        updateFile(master);
      }
      getLog().info("Finished project " + proj);
    }
  }

  /**
   * A function that initializes a File in Crowdin - creates parent folder(s) if
   * they don't exist - create the file if it doesn't exist - upload
   * translations for each file if they don't exist
   * 
   * @param _file the File to initialize in Crowdin
   */
  private void updateFile(CrowdinFile _file) {
    String fileN = _file.getFile().getName();
    if (getLog().isDebugEnabled())
      getLog().debug("*** Initializing: " + fileN);
    // Making sure the file is a master file and not a translation
    if (_file.getClass().equals(CrowdinFile.class)) {
      if (getLog().isDebugEnabled())
        getLog().debug("*** Init dir");
      initDir(_file.getCrowdinPath());
      try {
        if (_file.getFile().exists()) {
          if (!getHelper().elementExists(_file.getCrowdinPath())) {
            if (getLog().isDebugEnabled())
              getLog().debug("*** Add file: " + _file.getCrowdinPath());
            String result = getHelper().addFile(_file);
            if (result.contains("success"))
              getLog().info("File " + fileN + " created succesfully.");
            else
              getLog().warn("Cannot create file '" + _file.getFile().getPath() + "'. Reason:\n" + result);
          } else {
            if (getLog().isDebugEnabled()) {
              getLog().debug("*** Update file: " + _file.getCrowdinPath());
            }
            String result = getHelper().updateFile(_file);
            if (result.contains("success"))
              getLog().info("File " + fileN + " updated succesfully.");
            else
              getLog().warn("Cannot update file '" + _file.getFile().getPath() + "'. Reason:\n" + result);
          }
          if (_file.isShouldBeCleaned()) {
            _file.getFile().delete();
          }
        } else {
          if (getHelper().elementExists(_file.getCrowdinPath())) {
            if (getLog().isDebugEnabled())
              getLog().debug("*** Delete file: " + _file.getCrowdinPath());
            String result = getHelper().deleteFile(_file);
            if (result.contains("success"))
              getLog().info("File " + fileN + " deleted succesfully.");
            else
              getLog().warn("Cannot delete file '" + _file.getFile().getPath() + "'. Reason:\n" + result);
          }
        }
      } catch (MojoExecutionException e) {
        getLog().error("Error while updating file '" + _file.getFile().getPath() + "'. Exception:\n" + e.getMessage());
      }
    }
  }

}