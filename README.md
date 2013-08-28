crowdin-maven-plugin
====================
A command line tool to handle synchronization of the source code with translations made on Crowdin.

Maven plugin for crowdin (http://crowdin.net/)


Requirements:
-------------

* Git is installed and configured properly.
* Have a valid Crowdin Project ID and its key.
* Maven is installed 

Configuration:
--------------

Add the Following properties in the maven settings.xml (contact with admin of crowdin exo-platform-35 project to get project key and id):


    <exo.crowdin.project.id>crowdin-plf40</exo.crowdin.project.id>
    <exo.crowdin.project.key>{projectKey}</exo.crowdin.project.key>



Download:
---------

Get the latest version of the code:

    git clone https://github.com/exoplatform/crowdin-maven-plugin.git

This will create a folder and copy the necessary files into it.

Usage:
------

- From master branch, switch to branch feature/4.0.x/jgit
- Run maven command relates on profiles: **download-translation**, **update-sources**, **update-crowdin**, **init-crowdin**, **upload-translation**


**1\. Initialization a crowdin project: init-crowdin**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,init-crowdin -pl translations`**

This will execute the plugin with the profile 'init-crowdin':

-- load the properties of each project

-- browse them to identify master files and translations

-- create folders on Crowdin if they don't exist

-- upload the master files and translations of each master file on Crowdin if they don't exist


**2\. Download translations from crowdin: download-translations**


**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,download-translations -pl translations`**


- Download archive file from crowdin to translation/target/ then name to "translation.zip"

**3\. Activate new language**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,activate-language -pl translations`**

Execute 'activate-language' with download-translation included :

- Modify the pom file with only new activated language: **`<languages><language>ru</language></languages>`** for example
- Clone all projects to ~/.eXoProjectCached/
- Download archive file from crowdin to translation/target/ then name to "translation.zip" ( download-translation step)
- Create patches files, commit and apply to branch /feature/4.0.x-translation

with **`-DdryRun=true`**

- DryRun will not download the all.zip if it exists in /target/ also, it doesn't push to github

**4\. Update sources from crowdin (injection)**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,update-sources -pl translations`**

This will execute the plugin with the goal 'update-sources' with step download-translation included:

- Clone all projects to ~/.eXoProjectCached/
- Download archive file from crowdin to translation/target/ then name to "translation.zip" ( download-translation step)
- Create patches files,  commit and apply to branch /feature/4.0.x-translation

with **`-DdryRun=true`**

- DryRun will not download the all.zip if it exists in /target/ also, it doesn't push to github


**5\. Update to crowdin (synchronization): update-crowdin**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,update-crowdin -pl translations`**

This will execute the plugin with the goal 'update-crowdin':

-- load the properties of each project
-- browse them to identify master files and translations
-- create new folders, upload new master files and translations (new entries in the properties of each project)
-- update master files content on Crowdin (add new keys, rename existing keys, delete keys)
-- delete old folders and files (old entries in the properties of each project and not exist in file system)

with **`-DdryRun=true`**

- DryRun will not update properties files to crowdin

**6\. Upload Translation**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,upload-translation -pl translations`**

This is used to update changes in projects' translation files to Crowdin. It uses the information provided in the properties files (upload-translation.properties and <exo-project>.properties files) under upload-translation folder to determine the projects and their translation files need to be updated (in the same convention as the plugin's crowdin.properties and <exo-project>.properties files) 

Steps:

* Identify the list of projects need to be changed, put them into upload-translation.properties. Each entry is a key/value pair with key = <project name>-<version> and value = <path to project's description file>
* For each project create the project description file named <project>.properties. In this file provide the path to the project in the 'baseDir' property and list all the translation files need to be updated in the form of <path in Crowdin>=<path in source code>
* Run 'mvn clean install -Pupload-translation'

**7\. Restore translation**

**`mvn clean install -pl plugin -am; mvn clean install -Pcrowdin-plf40,plf40,restore-translation -pl translations`**

This restores a Crowdin project's directory structure and translations from its zip file. This zip file should be built with 'Export Only Approved' and 'Don't Export Untranslated' options unchecked so it will backup the untranslated and all suggested translations (not only the approved ones) as they will need to be restored also. Since this zip contains the project's directory structure and all of its translations, it can be considered as a project's backup and should be rebuilt (with 'Build Fresh Package' under Crowdin's 'Downloads' tab) and kept safe before doing any activity that may mess up the project.

This can also be used to clone a Crowdin project from its zip file (by changing the project's API key to point to another project). 

Steps:

* Rename the project's zip file as 'translation.zip' and put it under the plugin's crowdin-zip folder
* If the master files do not exist (e.g in case you want to do a clone, or you had deleted them all using Crowdin's 'File Manager' to rebuild from scratch - this is recommended), you must first re-create the project's structure with '-Daction=createProject' option, the plugin will extract the zip and walk through its directories and files to create the same structure on Crowdin.
* When having the master files ready, run 'mvn clean install -Prestore-translation', the plugin will (by default) upload translations of every languages it finds in the zip.

If you want to decide what to be uploaded to Crowdin, you can: first run with 'Dprepare=true' option to get the plugin stops after preparing the extracted folder for you to modify, e.g to remove some languages or projects you don't want to upload. When you're done, let the plugin continues by running with '-Dcontinue=true' option, it will upload your modified folder instead of the original zip. 

**8\. Command line options**

-- **dryRun**
   If true, no communication with Crowdin will be done; Default: false.
   Useful to see the evolution of the process. Combined with maven debug option -X, displays actual Rest calls and XmlPath queries.

Contributing:
-------------
    1. Fork it
    2. Create your feature branch (git checkout -b my-new-feature)
    3. Commit your changes (git commit -am 'Added some feature')
    4. Push to the branch (git push origin my-new-feature)
    5. Create new Pull Request

Resources:
----------

-- http://code.google.com/p/rest-assured/wiki/Usage?ts=1317978378&updated=Usage#Example_1_-_JSON

-- http://blog.jayway.com/2011/10/09/simple-parsing-of-complex-json-and-xml-documents-in-java/

-- http://rest-assured.googlecode.com/svn/tags/1.6/apidocs/com/jayway/restassured/path/xml/XmlPath.html

-- http://groovy.codehaus.org/Updating+XML+with+XmlSlurper

-- http://maven.apache.org/developers/mojo-api-specification.html

-- http://maven.apache.org/plugin-developers/common-bugs.html

-- http://www.regexplanet.com/advanced/java/index.html

-- http://docs.oracle.com/javase/6/docs/api/index.html

-- http://int.exoplatform.org/portal/intranet/wiki/group/spaces/platform_team/Crowdin_Maven_Plugin_Developer_guide

-- http://int.exoplatform.org/portal/intranet/wiki/group/spaces/platform_team/Crowdin_Maven_Plugin_User_Guide

