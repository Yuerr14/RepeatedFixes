package RQ1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class RQ1Invoker {
	String rq1FolderPath = null;
	String filteredRepeatedFixXmlFolderPath;
	String lineBreak= System.getProperty("line.separator");

	public RQ1Invoker(String rq1FolderPath, String filteredRepeatedFixXmlFolderPath) {
		this.rq1FolderPath = rq1FolderPath;
		this.filteredRepeatedFixXmlFolderPath = filteredRepeatedFixXmlFolderPath;
	}

	public void computeTable3() {
		Vector<String> repeatedFixList = new Vector<String>();
		Vector<String> patchWithRepeatedFixList = new Vector<String>();
		Vector<String> hunkWithRepeatedFixList = new Vector<String>();
		long bugWithRepeatedFixCount = 0;

		File file = new File(filteredRepeatedFixXmlFolderPath);
		File[] fileList = file.listFiles();
		try {
			for (File xmlFile : fileList) {
				bugWithRepeatedFixCount++;
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(xmlFile);
				NodeList repeatedFixPathList = document.getElementsByTagName("path");

				for (int i = 0; i < repeatedFixPathList.getLength(); i++) {
					String pathLine = repeatedFixPathList.item(i).getFirstChild().getNodeValue();

					String fixFileName = pathLine.substring(pathLine.lastIndexOf("\\") + 1);
					String[] tmp = fixFileName.split("_");
					String patchName = tmp[0] + "_" + tmp[1];
					String hunkName = tmp[0] + "_" + tmp[1] + "_" + tmp[2];

					if (!repeatedFixList.contains(fixFileName)) {
						repeatedFixList.addElement(fixFileName);
					}

					if (!patchWithRepeatedFixList.contains(patchName)) {
						patchWithRepeatedFixList.addElement(patchName);
					}
					if (!hunkWithRepeatedFixList.contains(hunkName)) {
						hunkWithRepeatedFixList.addElement(hunkName);
					}

				}
			}
			String resultFilePath = rq1FolderPath + "Table3.txt";
			File resultFile = new File(resultFilePath);
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			bw.write("# of repeated fixes:" + repeatedFixList.size() + lineBreak);
			bw.write("# of bug with repeated fixes:" + bugWithRepeatedFixCount + lineBreak);
			bw.write("# of patch with repeated fixes:" + patchWithRepeatedFixList.size() + lineBreak);
			bw.write("# of hunk with repeated fixes:" + hunkWithRepeatedFixList.size() + lineBreak);
			bw.write(lineBreak);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void computeFigure3And4() {
		long totalRepeatedFixGroupNum = 0;
		// key is the fixNum of a repeatedFixGroup，value the number of
		// repeatedFixGroups
		HashMap<Integer, Integer> fixNumDisForRepeatedFixGroups = new HashMap<Integer, Integer>();
		// key is the patchNum that covers all fixes of repeatedFixGroup，value
		// the number of repeatedFixGroups
		HashMap<Integer, Integer> patchNumDisForRepeatedFixGroups = new HashMap<Integer, Integer>();

		HashSet<String> fixNamesForOneRepeatedFixGroup = new HashSet<String>();
		HashSet<String> patchNamesForOneRepeatedFixGroup = new HashSet<String>();

		File dir = new File(filteredRepeatedFixXmlFolderPath);
		File[] xmlFiles = dir.listFiles();
		Scanner input;

		try {
			for (File xmlFile : xmlFiles) {
				input = new Scanner(xmlFile);
				while (input.hasNextLine()) {
					String tmp = input.nextLine();
					if (tmp.contains("<clone id=")) {
						// start of repeatedFixGroup
						totalRepeatedFixGroupNum++;
						fixNamesForOneRepeatedFixGroup.clear();
						patchNamesForOneRepeatedFixGroup.clear();
					} else if (tmp.contains("<path>")) {
						String fixName = tmp.substring(tmp.lastIndexOf("\\") + 1, tmp.lastIndexOf("."));
						String[] fixNameArray = fixName.split("_");
						String patchName = fixNameArray[0] + "_" + fixNameArray[1];
						// System.out.println("Fix: "+fixName+" Patch:
						// "+patchName);
						fixNamesForOneRepeatedFixGroup.add(fixName);
						patchNamesForOneRepeatedFixGroup.add(patchName);
					} else if (tmp.contains("</clone>")) {
						// end of repeatedFixGroup
						if (!fixNamesForOneRepeatedFixGroup.isEmpty()) {
							int fixNum = fixNamesForOneRepeatedFixGroup.size();
							if (!fixNumDisForRepeatedFixGroups.containsKey(fixNum)) {
								fixNumDisForRepeatedFixGroups.put(fixNum, 1);
							} else {
								int preGroupCnt = fixNumDisForRepeatedFixGroups.get(fixNum);
								fixNumDisForRepeatedFixGroups.put(fixNum, preGroupCnt + 1);
							}
						}

						if (!patchNamesForOneRepeatedFixGroup.isEmpty()) {
							int patchNum = patchNamesForOneRepeatedFixGroup.size();
							if (!patchNumDisForRepeatedFixGroups.containsKey(patchNum)) {
								patchNumDisForRepeatedFixGroups.put(patchNum, 1);
							} else {
								int preGroupCnt = patchNumDisForRepeatedFixGroups.get(patchNum);
								patchNumDisForRepeatedFixGroups.put(patchNum, preGroupCnt + 1);
							}
						}
						// System.out.println(xmlFile.getName()+"
						// "+fixNamesForOneRepeatedFixGroup.size()+"
						// "+patchNamesForOneRepeatedFixGroup.size());
					}
				} // end of reading for one xml
			} // end of all xml files

			File resultFile = new File(rq1FolderPath + "Figure3.txt");
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			bw.write("#RepeatedFixGroup:" + totalRepeatedFixGroupNum + lineBreak);
			bw.write("FixNumDisForRepeatedFixGroups:" + lineBreak);
			for (Integer fixCount : fixNumDisForRepeatedFixGroups.keySet()) {
				bw.write(fixCount + " " + fixNumDisForRepeatedFixGroups.get(fixCount) + " "
						+ (float) fixNumDisForRepeatedFixGroups.get(fixCount) / totalRepeatedFixGroupNum + lineBreak);
			}
			bw.write(lineBreak);
			bw.flush();
			bw.close();

			resultFile = new File(rq1FolderPath + "Figure4.txt");
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			bw = new BufferedWriter(new FileWriter(resultFile));
			bw.write("PatchNumDisForRepeatedFixGroups:" + lineBreak);
			for (Integer patchCount : patchNumDisForRepeatedFixGroups.keySet()) {
				bw.write(patchCount + " " + patchNumDisForRepeatedFixGroups.get(patchCount) + " "
						+ (float) patchNumDisForRepeatedFixGroups.get(patchCount) / totalRepeatedFixGroupNum
						+ lineBreak);
			}
			bw.write(lineBreak);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void computeTable4(int token, String projectName, String formattedFixFolderPath, String editSeqFolderPath,
			String fixFolderPath) {
		CrossBugDetector crossBugDetector = new CrossBugDetector(rq1FolderPath, filteredRepeatedFixXmlFolderPath, token,
				projectName, formattedFixFolderPath, editSeqFolderPath, fixFolderPath);
		File resultFile = new File(rq1FolderPath + "Table4.txt");
		try {
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		computeCrossBugTable(crossBugDetector.detectCrossBugRepeatedFixes(), resultFile);
	}

	private void computeCrossBugTable(String crossFilteredXmlFilePath, File resultFile) {
		// key is crossedBugNum,value is repeatedFixGroupNum that are across so many bugs
		HashMap<Integer, Integer> groupAcrossBugsDis = new HashMap<Integer, Integer>();
		File filteredCrossBugXml = new File(crossFilteredXmlFilePath);
		try {
			Scanner input = new Scanner(filteredCrossBugXml);
			String text = null;
			//key is repeatedFixGroup, value is bugSet that it is across
			HashMap<String, HashSet<String>> groupAcrossBugs = new HashMap<String, HashSet<String>>();
			ArrayList<String> repeatedFixGroupListForOneClone = new ArrayList<String>();
			HashSet<String> bugSetForOneClone = new HashSet<String>();
			while (input.hasNext()) {
				// System.out.println(groupAcrossBugs+" BeforeReadStart"+"
				// "+text);
				text = input.nextLine();
				if (text.contains("<clone id=")) {// enter a clone
					repeatedFixGroupListForOneClone.clear();
					bugSetForOneClone.clear();
				} else if (text.contains("<path>")) {
					String chosenfixFileName = text.substring(text.lastIndexOf("\\") + 1, text.indexOf("</"));
					if (!repeatedFixGroupListForOneClone.contains(chosenfixFileName))
						repeatedFixGroupListForOneClone.add(chosenfixFileName);
					String bugID = chosenfixFileName.substring(0, chosenfixFileName.indexOf("_"));
					if (!bugSetForOneClone.contains(bugID))
						bugSetForOneClone.add(bugID);
				} else if (text.contains("</clone>")) {
					for (int i = 0; i < repeatedFixGroupListForOneClone.size(); i++) {
						String repeatedFixGroup = repeatedFixGroupListForOneClone.get(i);
						if (!groupAcrossBugs.containsKey(repeatedFixGroup)) {
							groupAcrossBugs.put(repeatedFixGroup, (HashSet<String>) bugSetForOneClone.clone());
						} else {
							HashSet<String> preAcrossBugSet = (HashSet<String>) groupAcrossBugs.get(repeatedFixGroup)
									.clone();
							groupAcrossBugs.put(repeatedFixGroup,
									getCombinedHashSet(preAcrossBugSet, bugSetForOneClone));
						}
					}
				} // OnecloneEnd
			} // readLineEnd

			// compute groupArossBugsDis by using groupAcrossBugs
			for (String repeatedFixGroup : groupAcrossBugs.keySet()) {
				HashSet<String> acrossBugSetForOneRepeatedFixGroup = groupAcrossBugs.get(repeatedFixGroup);
				int acrossBugNumForOneRepeatedFixGroup = acrossBugSetForOneRepeatedFixGroup.size();
				if (!groupAcrossBugsDis.containsKey(acrossBugNumForOneRepeatedFixGroup)) {
					groupAcrossBugsDis.put(acrossBugNumForOneRepeatedFixGroup, 1);
				} else {
					int preGroupNum = groupAcrossBugsDis.get(acrossBugNumForOneRepeatedFixGroup);
					groupAcrossBugsDis.put(acrossBugNumForOneRepeatedFixGroup, preGroupNum + 1);
				}
			}
			// output result
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			bw.write("Bugs Repeated-fixGroupNumAcrossSuchBugs" + lineBreak);
			bw.write("AcrossBugNum RepeatedFixGroupNumAcrossSuchBugs" + lineBreak);
			for (int key : groupAcrossBugsDis.keySet()) {
				if (key == 1)
					continue;
				bw.write(key + " " + groupAcrossBugsDis.get(key) + lineBreak);
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private HashSet<String> getCombinedHashSet(HashSet<String> preSet, HashSet<String> curSet) {
		HashSet<String> combinedSet = new HashSet<String>();
		for (String v : preSet)
			if (!combinedSet.contains(v))
				combinedSet.add(v);
		for (String v : curSet)
			if (!combinedSet.contains(v))
				combinedSet.add(v);
		return combinedSet;
	}
}
