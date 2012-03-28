package org.exoplatform.crowdin.utils;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.xml.XmlPath.from;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.exoplatform.crowdin.model.CrowdinFile;
import org.exoplatform.crowdin.model.CrowdinTranslation;
import org.exoplatform.crowdin.mojo.AbstractCrowdinMojo;

import com.jayway.restassured.RestAssured;

public class CrowdinAPIHelper {

	private String projectId;
	private String projectKey;
	private AbstractCrowdinMojo currentMojo;

	public CrowdinAPIHelper(AbstractCrowdinMojo _mojo) {
		projectId = _mojo.getProjectId();
		projectKey = _mojo.getProjectKey();
		RestAssured.baseURI = "http://api.crowdin.net";
		RestAssured.port = 80;
		RestAssured.basePath = "/api/project/"+projectId;
		currentMojo = _mojo;
	}
	
	/**
	 * Calls the function http://crowdin.net/page/api/add-directory
	 * @param _dirName the name of the directory to create (with path if the directory is nested)
	 * @return true if the request is successful (directory created), false otherwise
	 * @throws MojoExecutionException
	 */
	public String addDirectory(String _dirName) throws MojoExecutionException {
		if (currentMojo.isDryRun()) {
			currentMojo.getLog().info("Adding directory '"+_dirName+"' in Dry Run mode...");
			if (currentMojo.getLog().isDebugEnabled())
				currentMojo.getLog().debug("*** Real mode would execute:\n" +
						"given()." +
						  "multiPart(\"name\", "+_dirName+")." +
						  "post(\"/add-directory?key="+projectKey+").andReturn().asString();");
			return "<dryRun success/>";
		}
		return given().
		    	multiPart("name", _dirName).
		        post("/add-directory?key="+projectKey).andReturn().asString();
	}
	
	/**
	 * Calls the function http://crowdin.net/page/api/add-file
	 * @param _filePath the full path + name of the file
	 * @param _type the type of the file (most likely properties)
	 * @return true if the request is successful (file added), false otherwise
	 * @throws MojoExecutionException
	 */
	public String addFile(CrowdinFile _file) throws MojoExecutionException {
		if (currentMojo.isDryRun()) {
			currentMojo.getLog().info("Adding file '"+_file.getFile().getName()+"' in Dry Run mode...");
			if (currentMojo.getLog().isDebugEnabled())
				currentMojo.getLog().debug("*** Real mode would execute:\n" +
						"given()." +
						  "multiPart(\"type\", "+_file.getType()+")." +
						  "multiPart(\"files[\""+_file.getCrowdinPath()+"\"]\", "+_file.getFile().getName()+")." +
						  "post(\"/add-file?key="+projectKey+").andReturn().asString();");
			return "<dryRun success/>";
		}
		return given().
				multiPart("type", _file.getType()).
		    	multiPart("files["+_file.getCrowdinPath()+"]", _file.getFile()).
		        post("/add-file?key="+projectKey).andReturn().asString();
	}
	
	/**
	 * 
	 * @param _eltPath the full path of the file/folder to check
	 * @return true if the element exists, false otherwise
	 * @throws MojoExecutionException
	 */
	public boolean elementExists(String _eltPath) throws MojoExecutionException {
		if (currentMojo.isDryRun()) return false;
		String infos = getProjectInfo();
		// an array in which each cell contains an element of the path, i.e.
		// path:  /path/to/element/element.ext
		// array: [path, to, element, element.ext]
		String[] pathElement = _eltPath.split("/");
		StringBuffer xmlPathStr = new StringBuffer("info");
		for (String elt : pathElement) {
			xmlPathStr.append(".files.item.find {it.name == '"+elt+"'}");
		}
		if (currentMojo.getLog().isDebugEnabled()) currentMojo.getLog().debug("*** XMLPath: "+xmlPathStr.toString());
		String fileName = from(infos).get(xmlPathStr.toString()).toString();
		return fileName.length() > 0;
	}
	
	/**
	 * Calls the function http://crowdin.net/page/api/update-file
	 * @param _filePath the full path + name of the file
	 * @return true if the request is successful (file updated), false otherwise
	 * @throws MojoExecutionException
	 */
	public String updateFile(CrowdinFile _file) throws MojoExecutionException {
		if (currentMojo.isDryRun()) {
			currentMojo.getLog().info("Updating file '"+_file.getFile().getName()+"' in Dry Run mode...");
			if (currentMojo.getLog().isDebugEnabled())
				currentMojo.getLog().debug("*** Real mode would execute:\n" +
						"given()." +
						  "multiPart(\"files[\""+_file.getCrowdinPath()+"\"]\", "+_file.getFile().getName()+")." +
						  "post(\"/update-file?key="+projectKey+").andReturn().asString();");
			return "<dryRun success/>";
		}
		return given().
		    	multiPart("files["+_file.getCrowdinPath()+"]", _file.getFile()).
		        post("/update-file?key="+projectKey).andReturn().asString();
	}
	
	/**
	 * Calls the function http://crowdin.net/page/api/upload-translation
	 * @param _filePath the full path + name of the translation file
	 * @param _language the language of the file ("fr", "it", etc)
	 * @return true if the request is successful (translation uploaded), false otherwise
	 * @throws MojoExecutionException
	 */
	public String uploadTranslation(CrowdinTranslation _file) throws MojoExecutionException {
		if (currentMojo.isDryRun()) {
			currentMojo.getLog().info("Uploading translation '"+_file.getFile().getName()+"' in Dry Run mode...");
			if (currentMojo.getLog().isDebugEnabled())
				currentMojo.getLog().debug("*** Real mode would execute:\n" +
						"given()." +
						  "multiPart(\"language\", "+_file.getLang()+")." +
						  "multiPart(\"files[\""+_file.getMaster().getCrowdinPath()+"\"]\", "+_file.getFile().getName()+")." +
						  "post(\"/upload-translation?key="+projectKey+").andReturn().asString();");
			return "<dryRun success/>";
		}
		return given().
				multiPart("language", _file.getLang()).
		    	multiPart("files["+_file.getMaster().getCrowdinPath()+"]", _file.getFile()).
		        post("/upload-translation?key="+projectKey).andReturn().asString();
	}
	
	/**
	 * Calls the function http://crowdin.net/page/api/info
	 * @return an XML string with all information about this project
	 * @throws MojoExecutionException
	 */
	public String getProjectInfo() throws MojoExecutionException {
		return given().
				post("/info?key="+projectKey).andReturn().asString();
	}
	
	/**
	 * 
	 * @return the File that contains all translations if the request is successful, null otherwise
	 * @throws MojoExecutionException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public File downloadTranslations() throws MojoExecutionException, FileNotFoundException, IOException {
		// we export the latest translations on the server
		// this is allowed only every 30 mins by Crowdin, TODO: could be handled here with a timer
		given().
				post("/export?key="+projectKey).andReturn().asString();
		// create the file in which all translations will be downloaded
		File translations = new File("target/all.zip");
		try {
			FileOutputStream fos = new FileOutputStream(translations);
			// write the translations (as a byte array) in the File
			fos.write(given().
					post("/download/all.zip?key="+projectKey).andReturn().asByteArray());
			fos.close();
		} catch (FileNotFoundException e) {
			translations = null;
			throw e;
		} catch (IOException e) {
			translations = null;
			throw e;
		}
		return translations;
	}
}
