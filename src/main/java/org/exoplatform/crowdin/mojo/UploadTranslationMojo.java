package org.exoplatform.crowdin.mojo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinTranslation;

/**
 * @goal upload-translation
 */
public class UploadTranslationMojo extends AbstractCrowdinMojo {
  /**
   * @required
   * @parameter expression="${exo.crowdin.upload-translation.properties}"
   */
  private String uploadTransPropsFile;
  private Properties uploadTransMainProps;
  private HashMap<String, Properties> uploadTransProperties;
    
  @Override
  public void executeMojo() throws MojoExecutionException, MojoFailureException {
    // Initialization of the properties
    uploadTransMainProps = new Properties();
    uploadTransProperties = new HashMap<String, Properties>();
    try {
      if (getLog().isDebugEnabled()) getLog().debug("*** Loading the main properties file ("+uploadTransPropsFile+")...");
      uploadTransMainProps = loadProperties(uploadTransPropsFile);
      Set<Object> keys = uploadTransMainProps.keySet();
      for (Object key : keys) {
        if (getLog().isDebugEnabled()) getLog().debug("*** Loading the properties file ("+uploadTransMainProps.getProperty(key.toString())+")...");
        uploadTransProperties.put(key.toString(), loadProperties(uploadTransMainProps.getProperty(key.toString())));
      }
      keys = null;

    } catch (IOException e) {
      getLog().error("Could not load the properties. Exception: "+e.getMessage());
      if (getLog().isDebugEnabled()) {
        for (StackTraceElement elt : e.getStackTrace()) {
          getLog().debug("*** "+elt.toString());
        }
      }
      throw new MojoExecutionException("Could not load the properties. Exception: "+e.getMessage());
    }
        
    // Iterate on each project defined in upload-translation.properties
    for (String proj : uploadTransProperties.keySet()) {
      getLog().info("Starting project "+proj);
      // Get the Properties of the current project, i.e. the content of cs-2.2.x.properties
      Properties currentProj = uploadTransProperties.get(proj);
      String baseDir = currentProj.getProperty("baseDir");
      Set<Object> keys = currentProj.keySet();
      // Iterate on each file of the current project
      for (Object key : keys) {
        // Skip the property baseDir
        if (key.equals("baseDir")) continue;
        // Construct the full path to the file
        String filePath = getStartDir()+proj+currentProj.getProperty(key.toString());
        
        Pattern p = Pattern.compile("^([a-zA-Z_0-9-/]*)_([a-z]{2})(_[A-Z]{2})?.([a-z]*)$");
        Matcher m = p.matcher(key.toString());
        String crowdinKey = m.matches() ? m.group(1).concat(".properties") : key.toString();
        
        CrowdinFile master = getFactory().prepareCrowdinFile(filePath, crowdinKey, baseDir);
        uploadTranslation(master, filePath);
      }
      getLog().info("Finished project "+proj);
    }    
  }
  
  private void uploadTranslation(CrowdinFile master, String transFilePath) {
    String transName = transFilePath.substring(transFilePath.lastIndexOf("/")+1);    
    if (getLog().isDebugEnabled()) getLog().debug("*** Initializing: "+transName);
    try {
      if (getLog().isDebugEnabled()) getLog().debug("*** Upload translation: "+transName+"\n\t***** for master: "+master.getName());
      CrowdinTranslation cTran = getFactory().prepareCrowdinTranslation(master, new File(transFilePath));
      if (getLog().isDebugEnabled()) {
        getLog().debug("=============================================================================");
        getLog().debug(printFileContent(cTran.getFile()));
        getLog().debug("=============================================================================");
      }
      String result = getHelper().uploadTranslation(cTran);
      if (result.contains("success")) getLog().info("Translation '"+transName+"' added succesfully.");
      else getLog().warn("Cannot upload translation '"+transFilePath+" with lang '"+cTran.getLang()+"'. Reason:\n"+result);
      if (cTran.isShouldBeCleaned()) {
        cTran.getFile().delete();
      }
    } catch (MojoExecutionException e) {
      getLog().error("Error while adding translation '"+transFilePath+"'. Exception:\n"+e.getMessage());
    }
  }
  
}