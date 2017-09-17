package RQ2.Table6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Table6Computer {
	String filteredXmlOfRepeatedFixPath = null, buggySnippetInfoPath = null, rq2FolderPath = null;

	String lineBreak = System.getProperty("line.separator");

	public Table6Computer(String filteredXmlOfRepeatedFixPath, String buggySnippetInfoPath, String rq2FolderPath) {
		this.filteredXmlOfRepeatedFixPath = filteredXmlOfRepeatedFixPath;
		this.buggySnippetInfoPath = buggySnippetInfoPath;
		this.rq2FolderPath = rq2FolderPath;
	}

	public void computeTable6() {
		try {
			File resultFile = new File(rq2FolderPath + "Table6.txt");
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedWriter resultWriter;

			resultWriter = new BufferedWriter(new FileWriter(resultFile));
			resultWriter.write("The results of repeated fixes in code clones" + lineBreak);
			for (int i = 5; i <= 50; i = i + 5) {
				String xmlOfBuggySnippetPath = buggySnippetInfoPath + "BuggySnippetCloneInfo\\CloneInfo" + i + "\\cloneInfoXml\\";
				computeForOneTokenSetting(resultWriter, i, xmlOfBuggySnippetPath);
				resultWriter.write(lineBreak);
			}
			resultWriter.flush();
			resultWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void computeForOneTokenSetting(BufferedWriter resultWriter, int tokenOfBuggySnippet,
			String xmlOfBuggySnippetPath) {
		int totalRepeatedFixGroupCnt = 0;
		int groupCntWithAllFixesAppliedInClones = 0;
		int groupCntWithFixesAppliedInClonesExist = 0;
		int groupCntWithNoFixesAppliedInClones = 0;

		File dir = new File(filteredXmlOfRepeatedFixPath);
		File[] xmlFiles = dir.listFiles();
		Scanner input;
		if (xmlFiles == null) {
			System.err.println("No Xml Files Of RepeatedFixes Found!");
			return;
		}
		try {
			String bugID = null, cloneID = null;
			CloneGroup cloneGroupOfRepeatedFix = null;
			ArrayList<CloneGroup> cloneGroupOfBuggySnippetList = new ArrayList<CloneGroup>();
			for (File xmlFile : xmlFiles) {
				String xmlFileName = xmlFile.getName();
				bugID = xmlFileName.substring(0, xmlFileName.indexOf("."));
				BufferedReader br = new BufferedReader(new FileReader(xmlFile));
				String str = null;
				while ((str = br.readLine()) != null) {
					if (str.contains("<clone")) {
						cloneGroupOfRepeatedFix = new CloneGroup();
						cloneID = str.substring(str.indexOf("\"") + 1, str.lastIndexOf("\""));
						cloneGroupOfRepeatedFix.cloneid = cloneID;
						totalRepeatedFixGroupCnt++;
					} else if (str.contains("<file>")) {
						str = br.readLine();
						String fixName = str.substring(str.lastIndexOf('\\') + 1, str.lastIndexOf('.'));
						str = br.readLine();
						int startLine = Integer.parseInt(str.substring(str.indexOf('>') + 1, str.lastIndexOf('<')));
						str = br.readLine();
						int endLine = Integer.parseInt(str.substring(str.indexOf('>') + 1, str.lastIndexOf('<')));
						CloneInstance cloneInstance = new CloneInstance();
						cloneInstance.fixName = fixName;
						cloneInstance.startLine = startLine;
						cloneInstance.endLine = endLine;
						cloneGroupOfRepeatedFix.cloneInstanceList.add(cloneInstance);

					} else if (str.contains("</clone>")) {
						cloneGroupOfBuggySnippetList = getCloneGroupOfBuggySnippetForOneRepeatedFixGroup(bugID, cloneID,
								xmlOfBuggySnippetPath);
						ArrayList<Boolean> boolResult = judgeForOneGroup(cloneGroupOfRepeatedFix,
								cloneGroupOfBuggySnippetList);
						// System.out.println(boolResult);
						if (boolResult.get(0)) {
							groupCntWithAllFixesAppliedInClones++;
						}
						if (boolResult.get(1)) {
							groupCntWithFixesAppliedInClonesExist++;
						}
						if (boolResult.get(2)) {
							groupCntWithNoFixesAppliedInClones++;
						}
					} // cloneEnd
				} // xmlReaderEnd
			} // allXmlEnd
			if (tokenOfBuggySnippet == 5)
				resultWriter.write("Total Count of RepeatedFixGroup:" + totalRepeatedFixGroupCnt + lineBreak+lineBreak);
			resultWriter.write("Token of BuggySnippet Clone Detection:" +  tokenOfBuggySnippet+ lineBreak);
			resultWriter.write("Count of RepeatedFixGroup with all fixes in clones:"
					+ groupCntWithAllFixesAppliedInClones + lineBreak);
			resultWriter.write("Count of RepeatedFixGroup with fixes existing in clones:"
					+ groupCntWithFixesAppliedInClonesExist + lineBreak);
			resultWriter.write("Count of RepeatedFixGroup with no fixes existing in clones:"
					+ groupCntWithNoFixesAppliedInClones + lineBreak);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<CloneGroup> getCloneGroupOfBuggySnippetForOneRepeatedFixGroup(String bugID, String cloneID,
			String xmlOfBuggySnippetPath) {
		ArrayList<CloneGroup> cloneGroupOfBuggySnippetList = new ArrayList<CloneGroup>();
		CloneGroup cloneGroupOfBuggySnippet = null;
		String mappedBuggySnippetXmlPath = xmlOfBuggySnippetPath + bugID + "_" + cloneID + ".xml";
		File xmlFileOfBuggySnippet = new File(mappedBuggySnippetXmlPath);
		BufferedReader br2;
		try {
			br2 = new BufferedReader(new FileReader(xmlFileOfBuggySnippet));

			String str2 = null, cloneIDOfBuggySnippet = null;
			while ((str2 = br2.readLine()) != null) {
				if (str2.contains("<clone")) {
					cloneGroupOfBuggySnippet = new CloneGroup();
					cloneIDOfBuggySnippet = str2.substring(str2.indexOf("\"") + 1, str2.lastIndexOf("\""));
					cloneGroupOfBuggySnippet.cloneid = cloneIDOfBuggySnippet;
					str2 = br2.readLine();
					while (str2.contains("<file>")) {
						str2 = br2.readLine();
						String fixName = str2.substring(str2.lastIndexOf('\\') + 1, str2.indexOf('-'));
						String tmpSuffix = str2.substring(str2.indexOf('-') + 1, str2.lastIndexOf("."));
						String[] tmp = tmpSuffix.split("-");
						int countUp = Integer.parseInt(tmp[0]);
						int countDown = Integer.parseInt(tmp[1]);
						int addLineCount = Integer.parseInt(tmp[2]);
						// int deleteLineCount=Integer.parseInt(tmp[3]);

						str2 = br2.readLine();
						int startLine = Integer.parseInt(str2.substring(str2.indexOf('>') + 1, str2.lastIndexOf('<')));
						str2 = br2.readLine();
						int endLine = Integer.parseInt(str2.substring(str2.indexOf('>') + 1, str2.lastIndexOf('<')));
						br2.readLine();
						str2 = br2.readLine();
						CloneInstance cloneInstance = new CloneInstance();
						cloneInstance.addLineCount = addLineCount;
						// cloneInstance.deleteLineCount=deleteLineCount;
						cloneInstance.countUp = countUp;
						cloneInstance.countDown = countDown;
						cloneInstance.fixName = fixName;
						cloneInstance.startLine = startLine;
						cloneInstance.endLine = endLine;
						cloneGroupOfBuggySnippet.cloneInstanceList.add(cloneInstance);
					} // cloneInstance
					cloneGroupOfBuggySnippetList.add(cloneGroupOfBuggySnippet);
				} // cloneGroup
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cloneGroupOfBuggySnippetList;
	}

	public ArrayList<Boolean> judgeForOneGroup(CloneGroup cloneGroupOfRepeatedFix,
			ArrayList<CloneGroup> cloneGroupOfBuggySnippetList) {
		ArrayList<Boolean> judgeResult = new ArrayList<Boolean>();
		boolean flagGroupCntWithAllFixesAppliedInClones = false;
		boolean flagGroupCntWithFixesAppliedInClonesExist = false;
		boolean flagGroupCntWithNoFixesAppliedInClones = true;

		for (int i = 0; i < cloneGroupOfBuggySnippetList.size(); i++) {
			List<String> mappingList = buildMappingList(cloneGroupOfRepeatedFix, cloneGroupOfBuggySnippetList.get(i));
			if (mappingList.size() > 1) {
				flagGroupCntWithFixesAppliedInClonesExist = true;
				flagGroupCntWithNoFixesAppliedInClones = false;
			}
			if (mappingList.size() == cloneGroupOfRepeatedFix.cloneInstanceList.size()) {
				flagGroupCntWithAllFixesAppliedInClones = true;
			}
		}
		judgeResult.add(flagGroupCntWithAllFixesAppliedInClones);
		judgeResult.add(flagGroupCntWithFixesAppliedInClonesExist);
		judgeResult.add(flagGroupCntWithNoFixesAppliedInClones);
		return judgeResult;
	}

	public List<String> buildMappingList(CloneGroup cloneGroupOfRepeatedFix, CloneGroup cloneGroupOfBuggySnippet) {
		List<CloneInstance> fixCloneInstanceList = cloneGroupOfRepeatedFix.cloneInstanceList;
		List<CloneInstance> buggySnippetCloneInstanceList = cloneGroupOfBuggySnippet.cloneInstanceList;

		List<String> mappingList = new ArrayList<String>();
		for (int i = 0; i < fixCloneInstanceList.size(); i++) {
			CloneInstance fixCloneInstance = fixCloneInstanceList.get(i);
			for (int j = 0; j < buggySnippetCloneInstanceList.size(); j++) {
				CloneInstance buggySnippetCloneInstance = buggySnippetCloneInstanceList.get(j);
				if (fixCloneInstance.fixName.equals(buggySnippetCloneInstance.fixName)) {
					int aRangeStart = fixCloneInstance.startLine + buggySnippetCloneInstance.countUp;
					int aRangeEnd = fixCloneInstance.endLine + buggySnippetCloneInstance.countUp
							- buggySnippetCloneInstance.addLineCount;

					if (isRangeMatch(aRangeStart, aRangeEnd, buggySnippetCloneInstance.startLine,
							buggySnippetCloneInstance.endLine)) {
						mappingList.add(cloneGroupOfRepeatedFix.cloneid + ":" + fixCloneInstance.fixName + " AppliedTo"
								+ cloneGroupOfBuggySnippet.cloneid + ":" + buggySnippetCloneInstance.fixName);
					}
				}
			} // buggySnippetCloneInstanceList end
		} // fixCloneInstanceList end

		return mappingList;
	}

	private static boolean isRangeMatch(int aRangeStart, int aRangeEnd, int buggySnippetCloneInstanceStart,
			int buggySnippetCloneInstanceEnd) {
		if (aRangeStart >= buggySnippetCloneInstanceStart && aRangeEnd <= buggySnippetCloneInstanceEnd)// repeatedFix��������buggySnippet��
			return true;
		return false;
	}
}

class CloneInstance {
	String fixName;
	int startLine;
	int endLine;

	int countUp;
	int countDown;
	int addLineCount;
	int deleteLineCount;
}

class CloneGroup {
	String cloneid;
	List<CloneInstance> cloneInstanceList;

	CloneGroup() {
		cloneInstanceList = new ArrayList<CloneInstance>();
	}
}
