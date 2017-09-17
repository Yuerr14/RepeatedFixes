package RepeatedFixDetection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RepeatDetectionBatGenerator {
	int token = 0;
	String repeatedFixFolderPath = null;
	String fixFolderPath = null;

	String scriptName = null;
	String lineBreak= System.getProperty("line.separator");

	public RepeatDetectionBatGenerator(String projectName, int token,  String formattedFixFolderPath, String repeatedFixFolderPath) {
		this.token = token;
		this.fixFolderPath=formattedFixFolderPath;
		this.repeatedFixFolderPath = repeatedFixFolderPath;		
		File repeatedFixFolder=new File(repeatedFixFolderPath);
		if(!repeatedFixFolder.exists())
			repeatedFixFolder.mkdirs();
	
		if (projectName.equals("EclipseJDTCore")) {
			scriptName = "java";
		} else if (projectName.equals("MozillaFirefox") || projectName.equals("LibreOffice")) {
			scriptName = "cpp";
		}
	}

	public void generateDetectionBat() {
		ArrayList<String> fixFileNameList = new ArrayList<String>();
		File dir = new File(fixFolderPath);
		File[] fixFiles = dir.listFiles();
		if (fixFiles == null) {
			System.err.println("No Fix Found!");
			return;
		}
		for (int i = 0; i < fixFiles.length; i++) {
			String fixFileName = fixFiles[i].getName();
			if (fixFileName.equals(".DS_Store"))
				continue;
			fixFileNameList.add(fixFileName);
		}
		Collections.sort(fixFileNameList, new SortFixFileNames());
		
		
		File detectionBatFile = new File(repeatedFixFolderPath + "cloneDetFormattedFixes" + token + ".bat");
		try {
			if (!detectionBatFile.exists())
				detectionBatFile.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(detectionBatFile));
			int preBugID = 0, curBugID = 0;
			bw.write("E:" + lineBreak);
			bw.write("cd " + repeatedFixFolderPath + lineBreak);

			bw.write("md CloneInfo" + token + lineBreak);
			bw.write("cd CloneInfo" + token + lineBreak);

			bw.write("md ccfxSrc" + lineBreak);
			bw.write("md cloneInfoOut" + lineBreak);
			bw.write("md cloneInfoXml" + lineBreak);
			bw.write("md preProcessResult" + lineBreak);
			String currentCloneInfoPath = repeatedFixFolderPath + "CloneInfo" + token + "\\";
			for (String fixFileName : fixFileNameList) {
				curBugID = Integer.valueOf(fixFileName.substring(0, fixFileName.indexOf("_")));
				if (curBugID != preBugID) {
					if (preBugID != 0) {
						bw.write("ccfx d " + scriptName + " -b " + token + " -d " + currentCloneInfoPath + "ccfxSrc"
								+ " -w w-" + lineBreak);
						bw.write("ccfx p a.ccfxd >cloneInfoOut\\" + preBugID + ".out" + lineBreak);
						bw.write("c:\\ccfx\\bin\\scripts\\post-prettyprint.pl " + "cloneInfoOut\\" + preBugID + ".out "
								+ "cloneInfoXml\\" + preBugID + ".xml" + lineBreak);
					}
					bw.write("xcopy ccfxSrc preProcessResult" + lineBreak);
					bw.write("del /q ccfxSrc" + lineBreak);
				}
				bw.write("copy " + fixFolderPath + fixFileName + " ccfxSrc\\" + lineBreak);
				preBugID = curBugID;
			}
			// process the last bug IDfdfd
			if (preBugID != 0) {
				bw.write("ccfx d " + scriptName + " -b " + token + " -d " + currentCloneInfoPath + "ccfxSrc" + " -w w-"
						+ lineBreak);
				bw.write("ccfx p a.ccfxd >cloneInfoOut\\" + preBugID + ".out" + lineBreak);
				bw.write("c:\\ccfx\\bin\\scripts\\post-prettyprint.pl " + "cloneInfoOut\\" + preBugID + ".out "
						+ "cloneInfoXml\\" + preBugID + ".xml" + lineBreak);

				bw.write("xcopy ccfxSrc preProcessResult" + lineBreak);
				bw.write("del /q ccfxSrc" + lineBreak);
			}

			bw.flush();
			bw.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	class SortFixFileNames implements Comparator {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			String[] tmp1 = s1.split("_");// bugID_bugOcuurence_diffCount_fixCount.suffix
			String[] tmp2 = s2.split("_");
			String tmpSuffixa3 = tmp1[3].substring(0, tmp1[3].indexOf("."));
			int bugID1 = Integer.valueOf(tmp1[0]);
			int suffixa1 = Integer.valueOf(tmp1[1]);
			int suffixa2 = Integer.valueOf(tmp1[2]);
			int suffixa3 = Integer.valueOf(tmpSuffixa3);

			String tmpSuffixb3 = tmp2[3].substring(0, tmp2[3].indexOf("."));
			int bugID2 = Integer.valueOf(tmp2[0]);
			int suffixb1 = Integer.valueOf(tmp2[1]);
			int suffixb2 = Integer.valueOf(tmp2[2]);
			int suffixb3 = Integer.valueOf(tmpSuffixb3);
			if (bugID1 > bugID2 || (bugID1 == bugID2 && suffixa1 > suffixb1)
					|| (bugID1 == bugID2 && suffixa1 == suffixb1 && suffixa2 > suffixb2)
					|| (bugID1 == bugID2 && suffixa1 == suffixb1 && suffixa2 == suffixb2 && suffixa3 > suffixb3))
				return 1;
			else
				return -1;
		}
	}

}
