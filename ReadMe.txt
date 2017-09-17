---------------------What does the code do?-----------------------------------------
The source code is used for our ICSME2017 paper titled as "A Characterization Study of Repeated Bug Fixes". It collects and analyzes repeated fixes.
---------------------How to run the code?-------------------------------------------
1.Install CCFinder on your machine (http://www.ccfinder.net/ccfinderxos.html). It works well on my machine(OS: windows7 64-bit).
2.Move "post-prettyprint.pl" to C:\ccfx\bin\scripts\ if you have installed CCFinder in the C:\ccfx\ folder. 
This script reads CCFinderX pretty print format, convert line numbers from intermediate CCFinderX token files to source code line numbers and saves the result in XML format.
3.Import the code into eclipse. 
4.Link the jar files in the lib folder of the zip to 
the project. 
5.Link the "misc", "model", "parser", "regression" and "suite" folder in the zip file to the project for cdt to use(Link source). 
6.Git Clone a study project's repo to your machine.
7.Use the following command to get the project's log.
git log  --pretty=format:"%H,%cd,%P,%s" --before="2016-01-01" --no-merges>commitInfoOneParentBefore20160101.txt
8.Sample the project's bugs and download the bug list from Bugzilla as a cvs file.
9.Run the main function in the "RepeatedFixAnalyzer" file. The input is the repo's path on your machine,its' log and bug list.
---------------------Explanations for code files---------------------
1.RepeatedFixAnalyzer
---RepeatedFixAnalyzer.java
   This file contains the main function. The main function includes the whole process of repeated fix collection and analysis.
2.FixCollection
---CommitInfoReverse.java
   This file converts the log file in chronological order to a new log file in reverse chronological order.
---CommitAnalyzer.java
   This file aims to find commits for fixing any sampled bug.
---PatchGenerator.java
   This file generates a bat file for generating fixing patches.
---PatchFilterOne.java
   This file filters less important hunks, such as hunks containing changes to documentations.
---MethodLabel
   This folder uses AST parsers to label each fix with the method containing the fix.
---FixFilterTwo.java
   This file removes fixes not applied to any method.
3.RepeatedFixDetection
---FixFormatter.java
   This file formats fixes by removing edit operation symbols (i.e. "+" and "-").
---RepeatDetectionBatGenerator.java
   This file generates a bat file which identifies clone regions among fixes.
---RepeatedFixCollector.java
   This file checks the edit operation sequences of clone regions and only collects clone regions whose edit operation sequences match.
---RepeatedFixFilter.java
   This file filters repeated fixes whose clone regions do not occupy the majority of their original fixes.
4.StudyProject
---StudyProjectComputer.java
   This file analyzes the basic infomation for a study project.
5.RQ1: This folder measures the results for RQ1.
6.RQ2: This folder computes the results for RQ1.