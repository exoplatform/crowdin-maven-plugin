
Requirements:
*************

A tool to handle synchronization of the source code with translations made on Crowdin.
There are three main interactions with Crowdin:

1. Initial setup of Crowdin
-- create all the directory structure
-- upload all files with their translations

2. Synchronizing Crowdin with the changes in the source
-- update folders names, files names
-- create new folders, upload new files
-- delete old folders and files

3. Updating the source code from changes in Crowdin
-- export and download all translations
-- replace existing files by new ones
-- generate a patch


Why a maven plugin?
*******************

-- Easy to develop in Java
-- Easy to package and execute from the command line
-- Possibility to setup the plugin in each pom, with a special profile,
   to include Source <-> Crowdin synchronization in the release process (can be dangerous)
   

Usage:
******

Checkout the translation project (http://svn.exoplatform.org/exo-int/platform-tools/studies/trunk/PLF-2787/translations/)
Open a terminal in the checked out folder, and run the following commands:

1. Initialization
-- mvn clean install -Pinit

This will execute the plugin with the goal 'init':
-- load the properties of each project
-- browse them to identify master files and translations (master file = file with no specific language code)
-- create folders on Crowdin if they don't exist
-- upload the master files on Crowdin if they don't exist
-- upload translations of each master file

2. Synchronization
-- mvn clean install -Psync

TODO

3. Updating
-- mvn clean install -Pupdate

TODO

4. Command line options

-- dryRun
   If true, no communication with Crowdin will be done; Default: false.
   Useful to see the evolution of the process. Combined with maven debug option -X, displays actual Rest calls and XmlPath queries.


Architecture:
*************

There are three main elements: the plugin, the communication with Crowdin API, the file model.

1. File model
The model consists in 2 classes: CrowdinFile and CrowdinTranslation.
CrowdinFile represents a master file on Crowdin, with the following attributes:
- a pointer to the actual File
- the path and name on Crowdin
- the project (cs, ks etc) to use in the full file name
- the type of file (properties only at the moment)

CrowdinTranslation inherits from CrowdinFile, and adds the following attributes:
- a pointer to the master CrowdinFile
- the lang

A third class CrowdinFileFactory allows to easily retrieve objects of the two classes above, after performing some
common operation. One of the most important is the recognition of XML resource bundle files, to transform them automatically
into Properties.

2. Using the Crowdin API
The plugin interacts with Crowdin thanks to its API (http://crowdin.net/page/api/).
To separate the logic of the plugin (parse folders, etc) and the operations on Crowdin (upload, create folders, etc),
the plugin defines the class CrowdinAPIHelper. It contains functions that call the following methods from the API:
   API method name / helper function name and arguments
-- add-directory / addDirectory(String _dirName)
-- add-file / addFile(CrowdinFile _file)
-- update-file / updateFile(CrowdinFile _file)
-- upload-translation / uploadTranslation(CrowdinFile _file)
-- export then download / downloadTranslations()

Two convenience functions are provided to get details about the project, and to verify whether files or folder exist on Crowdin:
-- info / getProjectInfo()
-- N/A / elementExists(String _eltPath)

3. Plugin Maven
It contains 3 MOJOs, one for each interaction with Crowdin (init, sync, update)
It's structured around an abstract parent class (AbstractCrowdinMojo) and three children classes, one for each MOJO.
Only one is created so far: InitCrowdinMojo (MOJO init). UpdateSourcesMojo was started but not completed, the sync mojo was not started.

AbstractCrowdinMojo defines few attributes
-- startDir : cf command line options
-- dryRun   : cf command line options
-- helper   : a reference to the CrowdinAPIHelper class, to call functions that communicate with Crowdin
-- projectId and projectKey : credentials to authenticate ourselves (exo) on Crowdin. they are set in the main settings.xml file
-- propertiesFile : the path to the main configuration file, set in the translations' pom.xml file
-- mainProps : the properties from the main config file (project-name-version = project-config-file)
-- properties : a map with all referenced properties (project-name-version <-> poject-properties)


Testing and debugging:
**********************

1. Build

-- mvn clean install
-- This will create the plugin artifacts in your local repository.


Resources:
**********

-- http://code.google.com/p/rest-assured/wiki/Usage?ts=1317978378&updated=Usage#Example_1_-_JSON
-- http://blog.jayway.com/2011/10/09/simple-parsing-of-complex-json-and-xml-documents-in-java/
-- http://rest-assured.googlecode.com/svn/tags/1.6/apidocs/com/jayway/restassured/path/xml/XmlPath.html
-- http://groovy.codehaus.org/Updating+XML+with+XmlSlurper
-- http://maven.apache.org/developers/mojo-api-specification.html
-- http://maven.apache.org/plugin-developers/common-bugs.html
-- http://www.regexplanet.com/advanced/java/index.html
-- http://docs.oracle.com/javase/6/docs/api/index.html
