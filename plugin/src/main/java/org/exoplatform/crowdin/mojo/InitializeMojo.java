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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.exoplatform.crowdin.model.SourcesRepository;

/**
 * Initialize local repositories to work
 */
@Mojo(name = "initialize")
public class InitializeMojo extends AbstractCrowdinMojo {

  @Override
  public void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Preparing working environment ...");
    for (SourcesRepository repository : getSourcesRepositories()) {
      File bareRepository = prepareRepositoryCache(repository);
      prepareWorkingRepository(repository, bareRepository);
    }
    getLog().info("Environment ready.");
  }

  private void prepareWorkingRepository(SourcesRepository repository, File bareRepository) throws MojoExecutionException, MojoFailureException {
    // Create a copy for the given version
    File localVersionRepository = new File(getWorkingDir(), repository.getLocalDirectory());
    if (localVersionRepository.exists()) {
      getLog().info("Reset repository " + repository.getLocalDirectory() + "...");
      execGit(localVersionRepository, "remote set-url origin " + repository.getUri());
      execGit(localVersionRepository, "remote update --prune");
      execGit(localVersionRepository, "checkout --force " + repository.getBranch());
      execGit(localVersionRepository, "reset --hard origin/" + repository.getBranch());
    } else {
      getLog().info("Creating working repository " + localVersionRepository + " ...");
      execGit(getWorkingDir(), "clone --no-checkout --reference " + bareRepository.getAbsolutePath() + " " + repository.getUri() + " " + repository.getLocalDirectory());
      execGit(localVersionRepository, "checkout --force " + repository.getBranch());
      execGit(localVersionRepository, "config user.name \"CrowdIn\"");
      execGit(localVersionRepository, "config user.email \"noreply+crowdin@exoplatform.com\"");
    }
    getLog().info("Done.");
  }

  private File prepareRepositoryCache(SourcesRepository repository) throws MojoExecutionException, MojoFailureException {
    // Create or update the reference repository
    File bareRepository = new File(getCacheDir(), repository.getName() + ".git");
    if (bareRepository.exists()) {
      getLog().info("Fetching repository " + repository.getName() + " ...");
      execGit(bareRepository, "remote set-url origin " + repository.getUri());
      execGit(bareRepository, "remote update --prune");
      getLog().info("Done.");
    } else {
      getLog().info("Cloning repository " + repository.getName() + " ...");
      execGit(getCacheDir(), "clone --bare " + repository.getUri() + " " + repository.getName() + ".git");
      getLog().info("Done.");
    }
    return bareRepository;
  }

}
