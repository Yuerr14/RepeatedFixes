package RQ2.Table6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;

public class BuggySnippetsOfOneGroupCloneDetection {
	String filteredRepeatedFixXmlFolderPath = null, buggySnippetsWithRepeatedFixFolderPath = null;
	String projectName = null, buggySnippetCloneInfoPath = null;
	String scriptChoice = null;
	int contextLineNum = 20;

	String pathSep = File.separator;
	String lineBreak = System.getProperty("line.separator");

	public BuggySnippetsOfOneGroupCloneDetection(String buggySnippetWithRepeatedFixFolderPath,
			String filteredRepeatedFixXmlFolderPath, String projectName, String buggySnippetCloneInfoPath) {
		this.buggySnippetsWithRepeatedFixFolderPath = buggySnippetWithRepeatedFixFolderPath;
		this.filteredRepeatedFixXmlFolderPath = filteredRepeatedFixXmlFolderPath;
		this.projectName = projectName;
		this.buggySnippetCloneInfoPath = buggySnippetCloneInfoPath;
		this.generateBuggySnippetCloneDetectionBat();
	}

	private void generateBuggySnippetCloneDetectionBat() {
		if (projectName.equals("EclipseJDTCore")) {
			scriptChoice = "java";
		} else if (projectName.equals("MozillaFirefox") || projectName.equals("LibreOffice")) {
			scriptChoice = "cpp";
		}
		File buggySnippetCloneInfoDir = new File(buggySnippetCloneInfoPath);
		if (!buggySnippetCloneInfoDir.exists() && !buggySnippetCloneInfoDir.isDirectory()) {
			buggySnippetCloneInfoDir.mkdir();
		}
		for (int i = 5; i <= 50; i += 5) {
			File batFile = new File(buggySnippetCloneInfoPath + "detectClonesForBuggySnippets-" + i + ".bat");
			if (!batFile.exists()) {
				try {
					batFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			getCloneBat(batFile, i);
		}
	}

	public void getCloneBat(File batFile, int token) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(batFile));
			bw.write("E:" + lineBreak);
			bw.write("cd " + buggySnippetCloneInfoPath + lineBreak);
			bw.write("md CloneInfo" + token + lineBreak);
			bw.write("cd CloneInfo" + token + lineBreak);
			bw.write("md ccfxSrc" + lineBreak);
			bw.write("md cloneInfoOut" + lineBreak);
			bw.write("md cloneInfoXml" + lineBreak);
			bw.write("md preProcessResult" + lineBreak);
			String currentCloneInfoPath = buggySnippetCloneInfoPath + "CloneInfo" + token + "\\";

			File xmlFileDir = new File(filteredRepeatedFixXmlFolderPath);

			for (File xmlfile : xmlFileDir.listFiles()) {
				// isXmlFile
				if (!xmlfile.getName().endsWith(".xml"))
					continue;
				String bugID = null, cloneID = null;
				// bugID
				String xmlFileName = xmlfile.getName();
				bugID = xmlFileName.substring(0, xmlFileName.indexOf("."));
				// Read XmlFile
				Scanner input = new Scanner(xmlfile);
				String str = null;
				HashSet<String> repeatedFixNameListForOneGroup = new HashSet<String>();

				while (input.hasNextLine()) {
					str = input.nextLine();
					if (str.contains("<clone id=")) {
						cloneID = str.substring(str.indexOf("\"") + 1, str.lastIndexOf("\""));
						repeatedFixNameListForOneGroup.clear();

					} else if (str.contains("<path>")) {
						String fixName = str.substring(str.lastIndexOf("\\") + 1, str.lastIndexOf("."));
						if (!repeatedFixNameListForOneGroup.contains(fixName))
							repeatedFixNameListForOneGroup.add(fixName);
					} else if (str.contains("</clone>")) {
						// bw.write("del /q ccfxSrc"+lineBreak);
						bw.write("xcopy ccfxSrc preProcessResult /y" + lineBreak);

						bw.write("del /q ccfxSrc" + lineBreak);
						writeCopyCommandForOneGroup(repeatedFixNameListForOneGroup, bw);
						bw.write("ccfx d " + scriptChoice + " -b " + token + " -d " + currentCloneInfoPath + "ccfxSrc"
								+ " -w w-" + lineBreak);
						bw.write("ccfx p a.ccfxd >cloneInfoOut\\" + bugID + "_" + cloneID + ".out" + lineBreak);
						bw.write("c:\\ccfx\\bin\\scripts\\post-prettyprint.pl " + "cloneInfoOut\\" + bugID + "_"
								+ cloneID + ".out " + "cloneInfoXml\\" + bugID + "_" + cloneID + ".xml" + lineBreak);
					}
				} // end of one xmlFile
			} // end of xmlFileList
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCopyCommandForOneGroup(HashSet<String> repeatedFixNameList, BufferedWriter bwForBat) {
		File allBuggySnippetWithRepeatedFixFile = new File(buggySnippetsWithRepeatedFixFolderPath);
		File[] buggySnippetList = allBuggySnippetWithRepeatedFixFile.listFiles();
		for (File buggySnippetFile : buggySnippetList) {
			String buggySnippetFileName = buggySnippetFile.getName();
			String fixNameForBuggySnippetFile = buggySnippetFileName.substring(0, buggySnippetFileName.indexOf("-"));
			if (repeatedFixNameList.contains(fixNameForBuggySnippetFile)) {
				try {
					bwForBat.write("copy " + buggySnippetFile.getAbsolutePath() + " ccfxSrc\\" + lineBreak);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
