package RepeatedFixDetection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RepeatedFixFilter {
	double ratioThreshold = 0.6;
	String pathSep = File.separator;
	String lineBreak= System.getProperty("line.separator");

	public void getFilterdRepeatedFixes(String repeatedFixXmlFolderPath, String filteredRepeatedFixXmlFolderPath,
			String fixFolderPath) {
		File desDir = new File(filteredRepeatedFixXmlFolderPath);
		if (!desDir.exists()) {
			desDir.mkdir();
		}
		File repeatedFixXmlFolder = new File(repeatedFixXmlFolderPath);
		File[] xmlFileList = repeatedFixXmlFolder.listFiles();
		for (File xmlFile : xmlFileList) {
			if (xmlFile.getName().equals(".DS_Store"))
				continue;
			File filterdXmlFile = new File(filteredRepeatedFixXmlFolderPath + xmlFile.getName());
			try {
				if (!filterdXmlFile.exists()) {
					filterdXmlFile.createNewFile();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			createFilteredXmlByRatio(xmlFile, filterdXmlFile, fixFolderPath);
		}
	}

	private void createFilteredXmlByRatio(File xmlFile, File filterdXmlFile, String fixFolderPath) {
		try {
			HashMap<String, Long> repeatedfixList = computeTotalLineForFixes(xmlFile, fixFolderPath);
			// key is fixes in this xml, value is the line count of a fix
			Scanner input = new Scanner(xmlFile);
			FileWriter output = new FileWriter(filterdXmlFile);

			Vector<String> oneCloneContent = new Vector<String>();
			Vector<String> fixNameForOneClone = new Vector<String>();
			boolean isCloneExistInNewFile = false;
			while (input.hasNextLine()) {
				String str = input.nextLine();
				if (str.contains("<repository>")) {
					output.write(str + lineBreak);
				} else if (str.contains("<clone id=")) {
					oneCloneContent.add(str);
					fixNameForOneClone.clear();
				} else if (str.contains("<file>")) {// <file>
					String pathStr = input.nextLine();
					String startLineStr = input.nextLine();
					String endLineStr = input.nextLine();
					String instanceEndStr = input.nextLine();

					String fixName = pathStr.substring(pathStr.lastIndexOf("\\") + 1, pathStr.lastIndexOf("<"));
					long start = Long.parseLong(
							startLineStr.substring(startLineStr.indexOf(">") + 1, startLineStr.lastIndexOf("<")));
					long end = Long
							.parseLong(endLineStr.substring(endLineStr.indexOf(">") + 1, endLineStr.lastIndexOf("<")));
					long len = repeatedfixList.get(fixName);

					double ratio = (double) (end - start + 1) / len;
					if (ratio >= ratioThreshold) {
						oneCloneContent.add(str);
						oneCloneContent.add(pathStr);
						oneCloneContent.add(startLineStr);
						oneCloneContent.add(endLineStr);
						oneCloneContent.add(instanceEndStr);

						if (!fixNameForOneClone.contains(fixName))
							fixNameForOneClone.add(fixName);
					}
				} else if (str.contains("</clone>")) {
					oneCloneContent.add(str);
					if (fixNameForOneClone.size() > 1) {
						for (int i = 0; i < oneCloneContent.size(); i++) {
							output.write(oneCloneContent.elementAt(i) + lineBreak);
						}
						isCloneExistInNewFile = true;
					}
					oneCloneContent.clear();
				} else if (str.contains("</repository>")) {
					output.write(str);
				}
			}
			input.close();
			output.close();

			if (!isCloneExistInNewFile) {
				filterdXmlFile.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, Long> computeTotalLineForFixes(File xmlFile, String fixFolderPath) {
		HashMap<String, Long> repeatedfixList = new HashMap<String, Long>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(xmlFile);
			NodeList pathList = document.getElementsByTagName("path");
			if (pathList.getLength() == 0) {
				return null;
			}

			for (int j = 0; j < pathList.getLength(); j++) {
				String tmp = pathList.item(j).getFirstChild().getNodeValue();
				String fixFileName = tmp.substring(tmp.lastIndexOf(pathSep) + 1);
				if (!repeatedfixList.containsKey(fixFileName)) {
					int lineCount = 0;
					String fixFilePath = fixFolderPath + fixFileName;
					File fixFile = new File(fixFilePath);
					Scanner input = new Scanner(fixFile);
					while (input.hasNextLine()) {
						input.nextLine();
						lineCount++;
					}
					input.close();
					repeatedfixList.put(fixFileName, (long) lineCount);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return repeatedfixList;
	}

}
