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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Downloads all translations from CrowdIn
 */
@Mojo(name = "download-translations")
public class DownloadTranslationsMojo extends AbstractCrowdinMojo {

  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    if (!crowdInArchive.exists() || !isDryRun()) {
      try {
        getHelper().setApprovedOnlyOption();
        getLog().info("Downloading Crowdin translation zip...");
        getHelper().downloadTranslations(crowdInArchive);
        getLog().info("Downloading done!");
      } catch (Exception e) {
        throw new MojoExecutionException("Error downloading the translations from Crowdin." + e);
      }
    }
  }
}
