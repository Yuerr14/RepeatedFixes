package RepeatedFixDetection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class RepeatedFixCollector {
	String cloneInfoFolderPath = null;
	String cloneXmlFolderPath = null;
	String editSeqFolderPath = null;
	String repeatedFixXmlFolderPath = null;

	String blankCommentSign = "*";
	String splitSignForFixes = ",";
	String splitSignForFixAndLineRange = "-";
	String lineBreak= System.getProperty("line.separator");

	public RepeatedFixCollector(String cloneInfoFolderPath, String cloneXmlFolderPath, String editSeqFolderPath,
			String repeatedFixXmlFolderPath) {
		this.cloneInfoFolderPath = cloneInfoFolderPath;
		this.cloneXmlFolderPath = cloneXmlFolderPath;
		this.editSeqFolderPath = editSeqFolderPath;
		this.repeatedFixXmlFolderPath = repeatedFixXmlFolderPath;
	}

	public void getRepeatedFixesBasedOnEditSeqs() {
		File repeatedFixXmlDir = new File(repeatedFixXmlFolderPath);
		if (!repeatedFixXmlDir.exists()) {
			repeatedFixXmlDir.mkdir();
		}
		File dir = new File(cloneXmlFolderPath);
		File[] cloneXmlFiles = dir.listFiles();
		if (cloneXmlFiles == null) {
			System.err.println("No CloneXmlFiles Found!");
			return;
		}
		for (File cloneXmlFile : cloneXmlFiles) {
			processSingleCloneXml(cloneXmlFile);
		}
	}

	private void processSingleCloneXml(File cloneXmlFile) {
		String xmlFileName = cloneXmlFile.getName();
		File repeatedFixXmlFile = new File(repeatedFixXmlFolderPath + xmlFileName);
		try {
			if (!repeatedFixXmlFile.exists()) {
				repeatedFixXmlFile.createNewFile();
			}
			BufferedReader br = new BufferedReader(new FileReader(cloneXmlFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(repeatedFixXmlFile));

			// Key is editSeq, value is the fixes who have the
			// editSeq(represented in "fix1,fix2,fix……“
			HashMap<String, String> editSeqAndFixForOneClone = new HashMap<String, String>();
			String str = null;
			String cloneID = null;
			bw.write("<repository>" + lineBreak);
			while ((str = br.readLine()) != null) {
				if (str.contains("<clone")) {
					cloneID = str.substring(str.indexOf("\"") + 1, str.lastIndexOf("\""));
					editSeqAndFixForOneClone.clear();
				} else if (str.contains("<path>")) {
					String fixFileName = str.substring(str.lastIndexOf("\\") + 1, str.lastIndexOf("</path>"));
					str = br.readLine(); // <startline>
					int startLine = Integer.parseInt(str.substring(str.indexOf('>') + 1, str.lastIndexOf('<')));
					str = br.readLine(); // <endlineline>
					int endLine = Integer.parseInt(str.substring(str.indexOf('>') + 1, str.lastIndexOf('<')));
					String editSeq = getEditSeq(fixFileName, startLine, endLine);

					String oneFix = fixFileName + splitSignForFixAndLineRange + startLine + splitSignForFixAndLineRange
							+ endLine;
					if (editSeqAndFixForOneClone.containsKey(editSeq)) {
						String preFixList = editSeqAndFixForOneClone.get(editSeq);
						// System.out.println(xmlFileName+" "+cloneID+"
						// "+fixFileName+" PreFixList:"+preFixList);
						editSeqAndFixForOneClone.put(editSeq, preFixList + splitSignForFixes + oneFix);
					} else {
						editSeqAndFixForOneClone.put(editSeq, oneFix);
					}
				} else if (str.contains("</clone>")) {
					String newCloneListInfo = getNewCloneListInfo(editSeqAndFixForOneClone, cloneID);
					bw.write(newCloneListInfo);
				}

			}
			bw.write("</repository>");
			br.close();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getEditSeq(String fixFileName, int startLine, int endLine) {
		String editOperationSeq = "";
		int lineNum = 0;
		File editSeqFile = new File(editSeqFolderPath + fixFileName);
		try {
			BufferedReader br = new BufferedReader(new FileReader(editSeqFile));
			String str = null;
			while ((str = br.readLine()) != null) {
				lineNum++;
				// Only non-blank/comment signs are reserved
				if (lineNum >= startLine && lineNum <= endLine && !str.equals(blankCommentSign))
					editOperationSeq += str;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * System.out.println("TestForEditSeq" + " " + "FixName:" + fixFileName
		 * + " " + startLine + "-" + endLine + " EditSeq:" + editOperationSeq);
		 */
		return editOperationSeq;
	}

	private String getNewCloneListInfo(HashMap<String, String> editSeqAndFixForOneClone, String cloneID) {
		int uniqueEditSeqNumForOneClone = 0;
		String newCloneListInfo = "", newCloneID = null;
		for (String editSeq : editSeqAndFixForOneClone.keySet()) {
			String fixListForOneEditSeq = editSeqAndFixForOneClone.get(editSeq);
			if (containsMoreThanOneUniqueFixes(fixListForOneEditSeq)) {
				uniqueEditSeqNumForOneClone++;
				if (uniqueEditSeqNumForOneClone == 1)
					newCloneID = cloneID;
				else
					newCloneID = cloneID + "-" + uniqueEditSeqNumForOneClone;
				newCloneListInfo += getOneNewCloneInfo(fixListForOneEditSeq, newCloneID);
			}
		}
		return newCloneListInfo;
	}

	private boolean containsMoreThanOneUniqueFixes(String fixList) {
		if (!fixList.contains(splitSignForFixes))
			return false;
		String[] tmp = fixList.split(splitSignForFixes);
		HashSet<String> fixNameList = new HashSet<String>();
		for (int i = 0; i < tmp.length; i++) {
			String fixName = tmp[i].substring(0, tmp[i].indexOf(splitSignForFixAndLineRange));
			if (!fixNameList.contains(fixName))
				fixNameList.add(fixName);
		}
		if (fixNameList.size() > 1)
			return true;
		else
			return false;
	}

	private String getOneNewCloneInfo(String fixListForOneEditSeq, String newCloneID) {
		String oneNewCloneInfo = "";
		oneNewCloneInfo += "  <clone id=\"" + newCloneID + "\">" + lineBreak;
		String[] fixList = fixListForOneEditSeq.split(splitSignForFixes);
		for (int i = 0; i < fixList.length; i++) {
			String oneFix = fixList[i];
			// 0 is fixFileName, 1 is startLine, 2 is endLine
			String[] tmp = oneFix.split(splitSignForFixAndLineRange);
			oneNewCloneInfo += "    <file>" + lineBreak;
			oneNewCloneInfo += "      <path>" + cloneInfoFolderPath + "ccfxSrc\\" + tmp[0] + "</path>" + lineBreak;
			oneNewCloneInfo += "      <startline>" + tmp[1] + "</startline>" + lineBreak;
			oneNewCloneInfo += "      <endlineline>" + tmp[2] + "</endlineline>" + lineBreak;
			oneNewCloneInfo += "    </file>" + lineBreak;
		}
		oneNewCloneInfo += "  </clone>" + lineBreak;
		return oneNewCloneInfo;
	}
}
