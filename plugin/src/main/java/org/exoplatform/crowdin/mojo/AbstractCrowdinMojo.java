package org.exoplatform.crowdin.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;


import com.jayway.restassured.RestAssured;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinFileFactory;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.utils.CrowdinAPIHelper;

/**
 * @author Philippe Aristote
 */
public abstract class AbstractCrowdinMojo extends AbstractMojo {

  /**
   * The directory to start parsing from
   * @parameter expression="${startDir}" default-value="."
   */
  private String startDir;

  /**
   * If true, no communication with Crowdin will be done; useful to test
   * @parameter expression="${dryRun}" default-value="false"
   */
  private boolean dryRun;

  /**
   * If true, continue initialize or synchronize source code to Crowdin if there
   * are nonexistent property files. If false, stop process
   * 
   * @parameter expression="${force}" default-value="false"
   */
  private boolean force;

  /**
   * Languages of the translations to be processed, or "all" to process all languages
   * @parameter expression="${langs}" default-value="all"
   */
  private String langs;
  
  /**
   * Option to get only the approved translations or not
   * @parameter expression="${apply_approved_only}" default-value="true"
   */
  private String apply_approved_only;
  

  private CrowdinFileFactory factory;
  private CrowdinAPIHelper helper;
  /**
   * @required
   * @parameter expression="${exo.crowdin.project.id}"
   */
  private String projectId;
  /**
   * @required
   * @parameter expression="${exo.crowdin.project.key}"
   */
  private String projectKey;
  /**
   * @required
   * @parameter expression="${exo.crowdin.properties}"
   */
  private String propertiesFile;
  /**
   * The main properties file, that contains names of other properties
   */
  private Properties mainProps;
  /**
   * The list of properties files that contain pointers to each file to manage with Crowdin <br/>
   * Format:  project-name-version <=> path/to/file.properties <br/>
   * Example: cs-2.2.x <=> cs-2.2.x.properties
   */
  private HashMap<String, Properties> properties;

  /**
   * @parameter expression="${exo.crowdin.ignore}"
   */
  private String ignore;
  /**
   * The list of ignored files which  are not processed by plugin
   */
  private Properties ignoredFiles;

  /**
   * The base directory of the project being tested. This can be obtained in your integration test via
   * System.getProperty("basedir").
   */
  @Parameter( defaultValue = "${basedir}" )
  private File basedir;

  public void execute() throws MojoExecutionException, MojoFailureException {
    // Initialization of the CrowdinFileFactory and CrowdinAPIHelper
    factory = new CrowdinFileFactory(this);
    helper = new CrowdinAPIHelper(this);
    // Options to show in debug mode
    if (getLog().isDebugEnabled()) {
      getLog().debug("*** RestAssured Base URI: "+ RestAssured.baseURI);
      getLog().debug("*** RestAssured Port: "+ RestAssured.port);
      getLog().debug("*** RestAssured Base Path: "+ RestAssured.basePath);
      getLog().debug("*** RestAssured Request URI: "+ RestAssured.baseURI+":"+RestAssured.port+RestAssured.basePath);
      getLog().debug("*** Current Working Directory: "+startDir);
    }
    // Initialization of the properties
    mainProps = new Properties();
    properties = new HashMap<String, Properties>();
    try {
      if (getLog().isDebugEnabled()) getLog().debug("*** Loading the main properties file ("+propertiesFile+")...");
      mainProps = loadProperties(propertiesFile);
      Set<Object> keys = mainProps.keySet();
      for (Object key : keys) {
        if (getLog().isDebugEnabled()) getLog().debug("*** Loading the properties file ("+mainProps.getProperty(key.toString())+")...");
        properties.put(key.toString(), loadProperties(new File(basedir,mainProps.getProperty(key.toString())).getAbsolutePath()));
      }
      keys = null;

      if (ignore != null) {
        if (getLog().isDebugEnabled()) {
          getLog().debug("*** Loading the ignored files list (" + ignore + ")...");
        }
        ignoredFiles = loadProperties(ignore);
      }

    } catch (IOException e) {
      getLog().error("Could not load the properties. Exception: "+e.getMessage());
      if (getLog().isDebugEnabled()) {
        for (StackTraceElement elt : e.getStackTrace()) {
          getLog().debug("*** "+elt.toString());
        }
      }
      throw new MojoExecutionException("Could not load the properties. Exception: "+e.getMessage());
    }
    // Create the target/ directory
    File target = new File("target");
    if (!target.exists()) target.mkdir();
    
    // Create the report/ directory
    File report = new File("report");
    if (!report.exists()) report.mkdir();
    
    // Call to the abstract method, that must be overriden in each concrete mojo
    executeMojo();
  }

  /**
   * A convenience method to load properties file
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
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  public abstract void executeMojo() throws MojoExecutionException, MojoFailureException;

  /*
   * Getters
   */

  public String getStartDir() {
    return startDir;
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


  public Properties getMainProperties() {
    return mainProps;
  }

  public String getLangs() {
    return langs;
  }
  
  public String getApplyApprovedOnlyOption(){
	  if ("true".equals(apply_approved_only)) {return "1";}
	  else return "0";
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
        String filePath = getStartDir() + proj + currentProj.getProperty(file.toString());
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
   * A function that initializes translations of the master file given in parameter.
   * @param _master The master file of which translations will be detected and uploaded.
   */
  protected void initTranslations(CrowdinFile _master) {
    File dir = _master.getFile().getParentFile();
    String masterFileName = _master.getFile().getName();
    if (_master.isShouldBeCleaned()) {
      _master.getFile().delete();
    }
    File[] files = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        if (dir.getPath().contains("gadget") && !dir.getPath().contains("GadgetPortlet")) {
          return true;
        }
        // There are both format *.properties and *.xml for this files, so must ignore *.xml files
        if (dir.getPath().contains("workflow") && name.indexOf(".xml") > 0) {
          return false;
        }
        if (dir.getPath().contains("web/portal")) {
          if (name.equals("expression_en.xml") || name.equals("expression_it.xml") || name.equals("services_en.xml")
              || name.equals("services_it.xml"))
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
      if (!tName.equalsIgnoreCase(mName)
          && (transName.indexOf(masterName) == 0 && transName.indexOf(masterName + "-") < 0 || file.getPath().contains("gadget"))) {
        if (getLog().isDebugEnabled()) getLog().debug("*** Initializing: "+transName);
        try {
          if (getLog().isDebugEnabled()) getLog().debug("*** Upload translation: "+transName+"\n\t***** for master: "+_master.getName());
          CrowdinTranslation cTran = getFactory().prepareCrowdinTranslation(_master, file);
          if (getLog().isDebugEnabled()) {
            getLog().debug("=============================================================================");
            getLog().debug(printFileContent(cTran.getFile()));
            getLog().debug("=============================================================================");
          }
          String result = getHelper().uploadTranslation(cTran);
          if (result.contains("success")) getLog().info("Translation '"+transName+"' added succesfully.");
          else getLog().warn("Cannot upload translation '"+file.getPath()+" with lang '"+cTran.getLang()+"'. Reason:\n"+result);
          if (cTran.isShouldBeCleaned()) {
            cTran.getFile().delete();
          }
        } catch (MojoExecutionException e) {
          getLog().error("Error while adding translation '"+file.getPath()+"'. Exception:\n"+e.getMessage());
        }
      }
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

}
