package org.exoplatform.crowdin.mojo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinTranslation;

/**
 * @goal init
 * @author Philippe Aristote
 */
public class InitCrowdinMojo extends AbstractCrowdinMojo {
   
    /**
     * Entry point of the goal. AbstractMojo.execute() is actually overridden in AbstractCrowdinMojo.
     */	
	@Override
	public void executeMojo() throws MojoExecutionException, MojoFailureException {
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
			getLog().info("Starting project "+proj);
			// Get the Properties of the current project, i.e. the content of cs-2.2.x.properties
			Properties currentProj = getProperties().get(proj);
			String baseDir = currentProj.getProperty("baseDir");
			Set<Object> files = currentProj.keySet();
			// Iterate on each file of the current project
			for (Object file : files) {
				// Skip the property baseDir
				if (file.equals("baseDir")) continue;
				// Construct the full path to the file
				String filePath = getStartDir()+proj+currentProj.getProperty(file.toString());
				CrowdinFile master = getFactory().prepareCrowdinFile(filePath, file.toString(), baseDir);
        if (master.getFile().exists()) {
          initFile(master);
          initTranslations(master);
        }
			}
			getLog().info("Finished project "+proj);
		}
	}
	
	/**
	 * A function that initializes a File in Crowdin
	 *  - creates parent folder(s) if they don't exist
	 *  - create the file if it doesn't exist
	 *  - upload translations for each file if they don't exist
	 * @param _file the File to initialize in Crowdin
	 */
	private void initFile(CrowdinFile _file) {
		String fileN = _file.getFile().getName();
		if (getLog().isDebugEnabled()) getLog().debug("*** Initializing: "+fileN);
		// Making sure the file is a master file and not a translation
		if (_file.getClass().equals(CrowdinFile.class)) {
			if (getLog().isDebugEnabled()) getLog().debug("*** Init dir");
			initDir(_file.getCrowdinPath());
			try {
				if (getLog().isDebugEnabled()) getLog().debug("*** Checking whether file: "+_file.getCrowdinPath()+" exists.");
				if (!getHelper().elementExists(_file.getCrowdinPath())) {
					if (getLog().isDebugEnabled()) getLog().debug("*** Add file: "+_file.getCrowdinPath());
					String result = getHelper().addFile(_file);
					if (result.contains("success")) getLog().info("File "+fileN+" created succesfully.");
					else getLog().warn("Cannot create file '"+_file.getFile().getPath()+"'. Reason:\n"+result);
				}
			} catch (MojoExecutionException e) {
				getLog().error("Error while creating file '"+_file.getFile().getPath()+"'. Exception:\n"+e.getMessage());
			}
		}
	}
	/**
	 * A function that initializes translations of the master file given in parameter.
	 * @param _master The master file of which translations will be detected and uploaded.
	 */
	private void initTranslations(CrowdinFile _master) {
		File dir = _master.getFile().getParentFile();
    if (_master.isShouldBeCleaned()) {
      _master.getFile().delete();
    }
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return getFactory().isTranslation(name);
			}
		});
		for (File file : files) {
			String transName = file.getName();
			String masterName = _master.getFile().getName().substring(0, _master.getFile().getName().lastIndexOf('.'));
			if (transName.contains(masterName)) {
				if (getLog().isDebugEnabled()) getLog().debug("*** Initializing: "+transName);
				try {
					if (getLog().isDebugEnabled()) getLog().debug("*** Upload translation: "+transName+"\n\t***** for master: "+_master.getName());
					CrowdinTranslation cTran = getFactory().prepareCrowdinTranslation(_master, file);
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
}
