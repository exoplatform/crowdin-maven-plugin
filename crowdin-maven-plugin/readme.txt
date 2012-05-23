
Requirements:
*************

A tool to handle synchronization of the source code with translations made on Crowdin.
There are three main interactions with Crowdin:

1. Initial setup of Crowdin
-- create all the directory structure
-- upload all files with their translations

2. Synchronizing Crowdin with the changes in the source
-- create new folders, upload new files
-- delete old folders and files
-- update folders names, files names
-- update master files content on Crowdin (master file = file with no specific language code)

3. Updating the source code from changes in Crowdin
-- export and download all translations
-- create "Crowdin" branches in source code from last tags
-- replace existing files in "Crowdin" branches by new ones
-- commit changes to "Crowdin" branches
-- merge "Crowdin" branches to working branches (do manually to fix conflicts)


Why a maven plugin?
*******************

-- Easy to develop in Java
-- Easy to package and execute from the command line
-- Possibility to setup the plugin in each pom, with a special profile,
   to include Source <-> Crowdin synchronization in the release process (can be dangerous)
   

Usage:
******

Clone the translation project (https://github.com/exoplatform/plf-studies/tree/master/PLF-2787/translations)
Open a terminal in the cloned folder, and run the following commands:

1. Initialization
-- mvn clean install -Pinit

This will execute the plugin with the goal 'init':
-- load the properties of each project
-- browse them to identify master files and translations
-- create folders on Crowdin if they don't exist
-- upload the master files and translations of each master file on Crowdin if they don't exist

Note: If there are nonexistent master files in file system, there will be a warning like
----------------------------------------------------------------------------------------
There are nonexistent properties files! Check again and update properties configuration files or run following command to continue:
  mvn clean install -Pinit -Dforce=true
----------------------------------------------------------------------------------------

2. Synchronization
-- mvn clean install -Psync

This will execute the plugin with the goal 'sync':
-- load the properties of each project
-- browse them to identify master files and translations
-- create new folders, upload new master files and translations (new entries in the properties of each project)
-- update master files content on Crowdin (old entries in the properties of each project and exist in file system)
-- delete old folders and files (old entries in the properties of each project and not exist in file system)

Note: If there are nonexistent master files in file system, there will be a warning like
----------------------------------------------------------------------------------------
There are nonexistent properties files! Check again and update properties configuration files or run following command to continue:
  mvn clean install -Psync -Dforce=true
Warning: All Crowdin files corresponding to nonexistent properties files will be deleted after execute above command
----------------------------------------------------------------------------------------

==> We can rely on above message to know if there are some master files renamed in source code. In this case, we need update manually these
master files by content and translations from Crowdin before do a synchronization again with -Dforce=true

3. Updating
-- mvn clean install -Pupdate

This will execute the plugin with the goal 'update':
-- export and download all translations
-- create "Crowdin" branches in source code from last tags
-- replace existing files in "Crowdin" branches by new ones
-- commit changes to "Crowdin" branches

Note: We need merge manually "Crowdin" branches to working branches to fix conflicts and commit after that



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
- information indicate this file whether a temporary file or not (temporary files are generated when convert xml files to properties files)

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
-- delete-file / deleteFile(CrowdinFile _file)
-- upload-translation / uploadTranslation(CrowdinFile _file)
-- export then download / downloadTranslations()

Two convenience functions are provided to get details about the project, and to verify whether files or folder exist on Crowdin:
-- info / getProjectInfo()
-- N/A / elementExists(String _eltPath)

3. Plugin Maven
It contains 3 MOJOs, one for each interaction with Crowdin (init, sync, update)
It's structured around an abstract parent class (AbstractCrowdinMojo) and three children classes, one for each MOJO.
InitCrowdinMojo (MOJO init), SyncSourcesMojo (MOJO synchronize), UpdateSourcesMojo (MOJO update)

Note: Plugin still doesn't support for gadget properties files. Also in update phase, the plugin produce translation files with the same
extension name with master file (Some time in source code, master file and it's translation files are not same extension name).
Also there is a case where there are duplicated master files with different extensions: properties and xml ( Bonita and Jbpm workflow resource
bundles ), plugin only take files with properties extension in this case.

AbstractCrowdinMojo defines few attributes
-- startDir : cf command line options, the working directory for the plugin
-- dryRun   : cf command line options, run in dryRun mode or not
-- helper   : a reference to the CrowdinAPIHelper class, to call functions that communicate with Crowdin
-- projectId and projectKey : credentials to authenticate ourselves (exo) on Crowdin. they are set in the main settings.xml file
-- propertiesFile : the path to the main configuration file, set in the translations' pom.xml file
-- mainProps : the properties from the main config file (project-name-version = project-config-file)
-- properties : a map with all referenced properties (project-name-version <-> project-properties)


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

