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

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import com.jayway.restassured.RestAssured;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.model.SourcesRepository;
import org.exoplatform.crowdin.utils.CrowdinAPIHelper;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * @author Philippe Aristote
 */
public abstract class AbstractCrowdinMojo extends AbstractMojo {

  public static final String DOWNLOAD_DATE_PROPERTY = "downloadDate";
  public static final String SRC_CONFIG = "src/config/";
  protected File crowdInArchive;
  protected File crowdInArchiveProperties;
  protected File translationStatusReport;
  /**
   * The directory to start parsing from
   */
  @Parameter(property = "workingDir", defaultValue = "${project.build.directory}/eXoProjects")
  private File workingDir;

  /**
   * The directory to cache repositories
   */
  @Parameter(property = "cacheDir", defaultValue = "${user.home}/.eXoProjectsCache")
  private File cacheDir;

  /**
   * If true, no communication with Crowdin will be done; useful to test
   */
  @Parameter(property = "dryRun", defaultValue = "false")
  private boolean dryRun;

  /**
   * If true, continue initialize or synchronize source code to Crowdin if there
   * are nonexistent property files. If false, stop process
   */
  @Parameter(property = "force", defaultValue = "false")
  private boolean force;

  /**
   * Languages of the translations to be processed, or "all" to process all languages
   */
  //  @Parameter(property = "langs", defaultValue = "all")
  @Parameter(property = "langs")
  private List<String> languages;

  /**
   * Option to get only the approved translations or not
   */
  @Parameter(property = "apply_approved_only", defaultValue = "true")
  private String apply_approved_only;

  @Parameter(property = "exo.crowdin.project.id", required = true)
  private String projectId;

  @Parameter(property = "exo.crowdin.project.key", required = true)
  private String projectKey;

  @Parameter(property = "exo.crowdin.ignore")
  private String ignore;
  
  /**
   * Boolean to enable/disable new language or new properties file
   */  
  @Parameter
  private boolean isActivate;

  @Parameter
  private List<SourcesRepository> sourcesRepositories = new ArrayList<SourcesRepository>();

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession mavenSession;

  @Component
  private BuildPluginManager pluginManager;

  /**
   * The list of properties files that contain pointers to each file to manage with Crowdin <br/>
   * Format:  project-name-version <=> path/to/file.properties <br/>
   * Example: cs-2.2.x <=> cs-2.2.x.properties
   */
  private HashMap<String, Properties> properties;

  /**
   * The list of ignored files which  are not processed by plugin
   */
  private Properties ignoredFiles;

  private CrowdinFileFactory factory;
  private CrowdinAPIHelper helper;
  private String downloadDate = null;

  public void execute() throws MojoExecutionException, MojoFailureException {
    // Initialization of the CrowdinFileFactory and CrowdinAPIHelper
    factory = new CrowdinFileFactory(this);
    helper = new CrowdinAPIHelper(this);
    // Options to show in debug mode
    if (getLog().isDebugEnabled()) {
      getLog().debug("*** RestAssured Base URI: " + RestAssured.baseURI);
      getLog().debug("*** RestAssured Port: " + RestAssured.port);
      getLog().debug("*** RestAssured Base Path: " + RestAssured.basePath);
      getLog().debug("*** RestAssured Request URI: " + RestAssured.baseURI + ":" + RestAssured.port + RestAssured.basePath);
      getLog().debug("*** Current Working Directory: " + workingDir);
    }
    // Initialization of the properties
    properties = new HashMap<String, Properties>();
    try {
      for (SourcesRepository repository : getSourcesRepositories()) {
        if (getLog().isDebugEnabled())
          getLog().debug("*** Loading the properties file (" + SRC_CONFIG + repository.getLocalDirectory() + ".properties)...");
        properties.put(repository.getLocalDirectory(), loadProperties(new File(getProject().getBasedir(), SRC_CONFIG + repository.getLocalDirectory() + ".properties").getAbsolutePath()));
      }

      if (ignore != null) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("*** Loading the ignored files list (" + ignore + ")...");
        }
        ignoredFiles = loadProperties(ignore);
      }

    } catch (IOException e) {
      getLog().error("Could not load the properties. Exception: " + e.getMessage());
      if (getLog().isDebugEnabled()) {
        for (StackTraceElement elt : e.getStackTrace()) {
          getLog().debug("*** " + elt.toString());
        }
      }
      throw new MojoExecutionException("Could not load the properties. Exception: " + e.getMessage());
    }
    File buildDir = new File(getProject().getBuild().getDirectory());
    buildDir.mkdirs();
    crowdInArchive = new File(buildDir, "translations.zip");
    crowdInArchiveProperties = new File(buildDir, "translations.properties");
    translationStatusReport = new File(buildDir, "translations_status.xml");

    // Call to the abstract method, that must be overriden in each concrete mojo
    crowdInMojoExecute();
  }

  /**
   * A convenience method to load properties file
   *
   * @param _propertiesFile the name/path of the file to load
   * @return the Properties file
   * @throws IOException
   */
  protected Properties loadProperties(String _propertiesFile) throws IOException {
    Properties res = new Properties();
    InputStream in = new FileInputStream(_propertiesFile);
    res.load(in);
    in.close();
    return res;
  }

  /**
   * The core method of the Mojo. Has to be overriden in each concrete Mojo.
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  public abstract void crowdInMojoExecute() throws MojoExecutionException, MojoFailureException;

  /*
   * Getters
   */

  public File getWorkingDir() {
    if (!workingDir.exists())
      workingDir.mkdirs();
    return workingDir;
  }

  public File getCacheDir() {
    if (!cacheDir.exists())
      cacheDir.mkdirs();
    return cacheDir;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public boolean isForce() {
    return force;
  }

  public CrowdinAPIHelper getHelper() {
    return helper;
  }

  public CrowdinFileFactory getFactory() {
    return factory;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public List<SourcesRepository> getSourcesRepositories() {
    return sourcesRepositories;
  }
  
  public boolean isActivate() {
    return isActivate;
  }


  public String getApplyApprovedOnlyOption() {
    if ("true".equals(apply_approved_only)) {
      return "1";
    } else
      return "0";
  }

  /**
   * The Maven Project Object
   */
  public MavenProject getProject() {
    return project;
  }

  public MavenSession getMavenSession() {
    return mavenSession;
  }

  public BuildPluginManager getPluginManager() {
    return pluginManager;
  }

  /**
   * @return The list of properties files that contain pointers to each file to manage with Crowdin <br/>
   * Format:  project-name-version <=> path/to/file.properties <br/>
   * Example: cs-2.2.x <=> cs-2.2.x.properties
   */
  public HashMap<String, Properties> getProperties() {
    return properties;
  }

  /**
   * Create parent directories of a file
   *
   * @param _filePath the full path of the parent of that file
   */
  protected void initDir(String _filePath) {
    // remove the file name
    _filePath = _filePath.substring(0, _filePath.lastIndexOf('/'));
    // add each element of the path in the cell of an array
    String[] path = _filePath.split("/");
    // reconstruct the path from the beginning, one element after each other
    // if the folder under this path doesn't exist yet, it is created
    StringBuffer pathFromBeginning = new StringBuffer();
    for (String string : path) {
      pathFromBeginning.append(string);
      try {
        if (!getHelper().elementExists(pathFromBeginning.toString())) {
          if (getLog().isDebugEnabled())
            getLog().debug("*** Create directory: " + _filePath);
          String result = getHelper().addDirectory(pathFromBeginning.toString());
          if (result.contains("success"))
            getLog().info("Directory '" + pathFromBeginning.toString() + "' created succesfully.");
          else
            getLog().warn("Cannot create directory '" + _filePath + "'. Reason:\n" + result);
        }
      } catch (MojoExecutionException e) {
        getLog().error("Error while creating directory '" + _filePath + "'. Exception:\n" + e.getMessage());
      }
      pathFromBeginning.append("/");
    }
  }

  protected boolean isAllPropertyFilesExisted() {
    boolean existed = true;
    getLog().info("Checking property files... ");
    // Iterate on each project defined in crowdin.properties
    for (String proj : getProperties().keySet()) {
      // Get the Properties of the current project, i.e. the content of
      // cs-2.2.x.properties
      Properties currentProj = getProperties().get(proj);
      Set<Object> files = currentProj.keySet();
      // Iterate on each file of the current project
      for (Object file : files) {
        // Skip the property baseDir
        if (file.equals("baseDir")) {
          continue;
        }
        // Construct the full path to the file
        String filePath = getWorkingDir() + File.separator + proj + File.separator + currentProj.getProperty(file.toString());
        File f = new File(filePath);
        if (!f.exists()) {
          existed = false;
          getLog().warn("File not found: " + filePath);
        }
      }
    }
    getLog().info("Checking done.");
    return existed;
  }

  /**
   * A function that initializes translations of the master file given in parameter 
   * then upload these translations into Crowdin accordingly
   *
   * @param _master The master file of which translations will be detected and uploaded.
   */
  protected void initTranslations(CrowdinFile _master) {
    File dir = _master.getFile().getParentFile();
    String masterFileName = _master.getFile().getName();
    if (_master.isShouldBeCleaned()) {
      _master.getFile().delete();
    }
 
    List<File> files = new ArrayList<File>();
    List<String> languagesToProcess = getLanguages();    
    // processing for Android or iOs
    if ((dir.getPath().contains("android")) || ((dir.getPath().contains("ios")))) {
      
      
      // remove "en" language default in the list, just send translations files      
      if (languagesToProcess.contains("en")) {
        languagesToProcess.remove("en");
      }      
      
      for (int i = 0; i < languagesToProcess.size(); i++) {        
          String replaceLanguagePathName ="";
          
          if (dir.getPath().contains("android")){
          //replace "values" to "values-language"
            String localizable = CrowdinTranslation.encodeAndroidLocale(languagesToProcess.get(i));
          replaceLanguagePathName = dir.getPath().replaceAll("values","values-" + localizable);
          }
          else if (dir.getPath().contains("ios")) {
            //replace "en.lproj" to "language.lproj"
            //transform to iOS convention localizable "es-ES" > "es_ES"
            String localizable = CrowdinTranslation.encodeIOSLocale(languagesToProcess.get(i));
          replaceLanguagePathName = dir.getPath().replaceAll("en.lproj",localizable+".lproj");
          }          
          // add translation files in list
          File fileToAdd = new File(replaceLanguagePathName + File.separator + masterFileName);
          if (fileToAdd.exists()) {
            files.add(fileToAdd);
          }
                  
      }
    }
    // processing for other projects
    else{
      File[] filesArray = dir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (dir.getPath().contains("gadget") && !dir.getPath().contains("GadgetPortlet")) {
            return true;
          }
          // There are both format *.properties and *.xml for this files, so must
          // ignore *.xml files
          if (dir.getPath().contains("workflow") && name.indexOf(".xml") > 0) {
            return false;
          }
          if (dir.getPath().contains("web/portal")) {
            if (name.equals("expression_en.xml") || name.equals("expression_it.xml")
                || name.equals("services_en.xml") || name.equals("services_it.xml"))
              return false;
          }
          if (ignoredFiles != null) {
            String filePath = dir.getPath() + "/" + name;
            for (Object key : ignoredFiles.keySet()) {
              if (filePath.indexOf((String) key) >= 0) {
                return false;
              }
            }
          }
          return getFactory().isTranslation(name);
        }
      });
      files = Arrays.asList(filesArray);
    }  
    
    
    for (File file : files) {
      String transName = file.getName();
      String masterName;
      Matcher matcher = getFactory().matchTranslation(masterFileName);
      if (matcher.matches()) {
        masterName = matcher.group(1);
      } else {
        masterName = masterFileName.substring(0, masterFileName.lastIndexOf('.'));
      }
      String tName = transName.substring(0, transName.lastIndexOf('.'));
      String mName = masterFileName.substring(0, masterFileName.lastIndexOf('.'));
      if (!tName.equalsIgnoreCase(mName) && (transName.indexOf(masterName) == 0 && transName.indexOf(masterName + "-") < 0 || file.getPath().contains("gadget"))) {
        if (getLog().isDebugEnabled())
          getLog().debug("*** Initializing: " + transName);
        prepareAndUploadTranslation(transName, _master, file);
      }
      //process for Android or iOS
      else if ((dir.getPath().contains("android")) || ((dir.getPath().contains("ios")))) {
        prepareAndUploadTranslation(transName, _master, file);
      }
    }
  }
  
  /**
* prepareCrowdinTranslation and uploadTranslation
* @param transName
* @param _master
* @param file
*/
  private void prepareAndUploadTranslation(String transName, CrowdinFile _master, File file) {
    try {
      if (getLog().isDebugEnabled())
        getLog().debug("*** Upload translation: " + transName + "\n\t***** for master: "
            + _master.getName());
      CrowdinTranslation cTran = getFactory().prepareCrowdinTranslation(_master, file);
      if (getLog().isDebugEnabled()) {
        getLog().debug("=============================================================================");
        getLog().debug(printFileContent(cTran.getFile()));
        getLog().debug("=============================================================================");
      }
      if(!getLanguages().contains(cTran.getLang())){
        getLog().warn("Language "+cTran.getLang()+" is not configured to be processed");
        return;
      }
      
      String result = getHelper().uploadTranslation(cTran);
      getLog().info("*** Upload translation: " + transName + "\n\t***** for master: "
          + _master.getName());
      
      if (result.contains("success"))
        getLog().info("Translation '" + transName + "' added succesfully.");
      else
        getLog().warn("Cannot upload translation '" + file.getPath() + " with lang '"
            + cTran.getLang() + "'. Reason:\n" + result);
      if (cTran.isShouldBeCleaned()) {
        cTran.getFile().delete();
      }
    } catch (MojoExecutionException e) {
      getLog().error("Error while adding translation '" + file.getPath() + "'. Exception:\n"
          + e.getMessage());
    }
  }
    

  protected String printFileContent(File file) {
    try {
      Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8");
      char[] characters = new char[(int) file.length()];
      r.read(characters);
      return new String(characters);
    } catch (Exception e) {
      getLog().warn("Unable to print file content", e);
    }
    return null;
  }

  protected void execGit(File workingDirectory, String params) throws MojoExecutionException, MojoFailureException {
    execGit(workingDirectory, params, element("successCode", "0"));
  }

  protected void execGit(File workingDirectory, String params, MojoExecutor.Element... successCodes) throws MojoExecutionException, MojoFailureException {
    getLog().info("Running : git " + params);
    executeMojo(plugin(groupId("org.codehaus.mojo"), artifactId("exec-maven-plugin"), version("1.2.1")), goal("exec"), configuration(element(name("executable"), "/bin/sh"), element(name("commandlineArgs"), "-c \"(cd " + workingDirectory.getAbsolutePath() + " && exec git " + params + ")\""), element(name("workingDirectory"), workingDirectory.getAbsolutePath()), element(name("successCodes"), successCodes)), executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
  }

  protected String getCrowdinDownloadDate() throws IOException {
    if (downloadDate == null) {
      Properties downloadProperties = new Properties();
      downloadProperties.load(new FileInputStream(crowdInArchiveProperties));
      downloadDate = downloadProperties.getProperty(DOWNLOAD_DATE_PROPERTY);
    }
    return downloadDate;
  }
}
