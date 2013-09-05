jazzautomation_source
==================
###############################
Installation Instructions:

1. Download jazz automation at https://github.com/jazzautomation_source;

2. Unzip to a working directory;

3. Make sure you have JAVA_HOME in your environment. Java7 is required.

4. Make sure gradlew can start, by executing:
	> gradlew

you should see:
	:help

	Welcome to Gradle 1.6.

	To run a build, run gradlew <task> ...

	To see a list of available tasks, run gradlew tasks

	To see a list of command-line options, run gradlew --help

	BUILD SUCCESSFUL




###############################
Run an example,

1.
	> gradlew runJazz -Pjazz_configs=examples/bestBuyExample

This will run bestBuy example.



2. Sample Configuration. We have two examples under "examples" directory: bestBuyExample and columbiaSportsExample. To run a particular example, execute one of the following:

	> gradlew runJazz -Pjazz_configs=examples/columbiaSportsExample

	> gradlew runJazz -Pjazz_configs=examples/bestBuyExample
	
	You also can run the bestBuyExample against suaceLabs remotely:
	> gradlew runJazz -Pjazz_configs=examples/bestBuyExample -Premote=true
	(please login to https://saucelabs.com/home with jazzautomation/jazztest to view the run and replay video)

(You can also change it at jazz_configs property of gradle.properties)


3. Test configuration. You can create your own configuration for a site by follow the structure of either sample projects (columbiaSportsExample or bestBuyExample). The following folder structure is required:
	{configFolderName}
		settings.properties
		project.properties
		> pages (folder)
		> features (folder)
		

4. Browser configuration. You can change the browser by using the below property

 	> gradlew run -Pbrowsers=firefox

    or

	> gradlew run -Pbrowsers=chrome

   (in the future we may support multiple browsers for one flow)


5. Remote run. You may run against a selenium farm such as SauceLabs. If this is desired, simply change the "remoteWebDriverUrl" property in the project.properties for the test configuration. Then run

	gradlew run -Premote=true
	

6. Reports. By default, the system generates testing reports at "reports" folder. Make sure the directory exists if you don't do one of the following. You can customize the report location by

	gradlew run -Pjazz_reports=a_directory_path

   or, you can specify it at gradle.properties (still make sure the directory exists).



	
##########################
How to configure a page

Please read JazzAutomation_PageConfiguration.pdf located in the docs directory

##########################
How to configure a feature

Please read JazzAutomation_FeatureConfiguration.pdf located in the docs directory

		
