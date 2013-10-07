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

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.utils.FileUtils;

@Mojo(name = "upload-translation")
public class UploadTranslationMojo extends AbstractCrowdinMojo {

  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    if (isAllPropertyFilesExisted()) {
      // Iterate on each project defined in upload-translation.properties
      for (String proj : getProperties().keySet()) {
        getLog().info("Starting project " + proj);
        // Get the Properties of the current project, i.e. the content of
        // cs-2.2.x.properties
        Properties currentProj = getProperties().get(proj);
        String baseDir = currentProj.getProperty("baseDir");
        Set<Object> keys = currentProj.keySet();
        // Iterate on each file of the current project
        for (Object key : keys) {
          // Skip the property baseDir
          if (key.equals("baseDir"))
            continue;
          // Construct the full path to the file
          String filePath = getWorkingDir() + File.separator + proj + File.separator
              + currentProj.getProperty(key.toString());

          CrowdinFile master = getFactory().prepareCrowdinFile(filePath, key.toString(), baseDir);
          uploadTranslation(master);
        }
        getLog().info("Finished project " + proj);
      }
    }

  }

  private void uploadTranslation(CrowdinFile master) {
    
    if (getLog().isDebugEnabled())
      getLog().debug("*** Init dir");
    initDir(master.getCrowdinPath());

    try {          
      // escape special character before sync
      FileUtils.replaceCharactersInFile(master.getFile().getPath(),
                                        "config/special_character_processing.properties",
                                        "EscapeSpecialCharactersBeforeSyncFromCodeToCrowdin");
      
      String result;
      // check if master file already existed on Crowdin, if not then add new one 
      if (!getHelper().elementExists(master.getCrowdinPath())) {
        if (getLog().isDebugEnabled())
          getLog().debug("*** Add file: " + master.getCrowdinPath());
        result = getHelper().addFile(master);
      }else {
        if (getLog().isDebugEnabled()) {
          getLog().debug("*** Update file: " + master.getCrowdinPath());
        }
        result = getHelper().updateFile(master);
      }
      // Initialize translations for the given master file
      if (result.contains("success")) {
        getLog().info("File " + master.getFile().getName() + " created succesfully.");
        initTranslations(master);
      } else {
        getLog().warn("Cannot upload file '" + master.getFile().getPath() + "'. Reason:\n"
            + result);
      }
      
      if (master.isShouldBeCleaned()) {
        master.getFile().delete();
      } else {
        // remove escape special character after sync
        FileUtils.replaceCharactersInFile(master.getFile().getPath(),
                                          "config/special_character_processing.properties",
                                          "EscapeSpecialCharactersAfterSyncFromCodeToCrowdin");
      }
    
    } catch (MojoExecutionException e) {
      getLog().error("Error while updating file '" + master.getFile().getPath()
          + "'. Exception:\n" + e.getMessage());
    }
    
  }

}
