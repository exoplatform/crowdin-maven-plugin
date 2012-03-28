
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

1. Initialization
-- mvn org.exoplatform.crowdin:crowdin-maven-plugin:${version}:init

This will execute the plugin with the goal 'init':
-- parse the startDir directory (default is the current directory)
-- find all properties (and soon xml) files
-- create folders on Crowdin if they don't exist
-- upload the master files on Crowdin if they don't exist (master file = file with no specific language code)
-- upload translations of each master file

2. Synchronization
-- mvn org.exoplatform.crowdin:crowdin-maven-plugin:${version}:sync

TODO

3. Updating
-- mvn org.exoplatform.crowdin:crowdin-maven-plugin:${version}:update

TODO

4. Command line options

-- startDir
   The directory from where the plugin will execute. Default is the current directory.
   Example: mvn ... -DstartDir=cs-2.1.x/
-- dryRun
   If true, no communication with Crowdin will be done; Default: false.
   Useful to see the evolution of the process. Combined with maven debug option -X, displays actual Rest calls and XmlPath queries.


Architecture:
*************

There are three main elements: the plugin, the communication with Crowdin API, the file model.

1. File model
The plugin defines and uses a class CrowdinFile that extends the java.io.File class.
It adds a few attributes of a file in Crowdin, such as:
- the type of the file (properties, xml)
- whether the file is a master (w/o language code) or a translation
-- if yes, the language
-- if yes, a reference to the master file
- a cleaned copy of the path of this file and its parent folder

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
Only one is created so far: InitCrowdinMojo (MOJO init). The two others must be completed.

AbstractCrowdinMojo defines few properties
-- startDir : cf command line options
-- dryRun   : cf command line options
-- helper   : a reference to the CrowdinAPIHelper class, to call functions that communicate with Crowdin


Testing and debugging:
**********************

1. Build

-- mvn clean install
-- This will create the plugin artifacts in your local repository.


Possible improvements:
**********************

-- map war names with real folder structure with a configuration file (settings.xml)
-- use two properties for the project key and API key (settings.xml or command line)
-- add a prefix in the folder structure to indicate the project name and version (aio, plf30, etc)
-- ensure how the type of the file is defined
-- use an abstract class and children classes for different file types (properties, xml-gadget, xml-exo)


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
