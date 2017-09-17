package RQ1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import RepeatedFixDetection.RepeatedFixCollector;
import RepeatedFixDetection.RepeatedFixFilter;

public class CrossBugDetector {
	String crossBugPath;
	String filteredRepeatedFixXmlFolderPath;
	
	//For BatFile
	int token;
	String scriptName;
	String formattedFixFolderPath;
	//For CollectRepeatedFix
	String editSeqFolderPath;
	//For filterRepeatedFix
	String fixFolderPath;
	

	File batFile;	
	
	String pathSep = File.separator;
	String lineBreak= System.getProperty("line.separator");

	public CrossBugDetector(String rq1FolderPath, String filteredRepeatedFixXmlFolderPath, int token,
			String projectName, String formattedFixFolderPath,String editSeqFolderPath, String fixFolderPath) {
		crossBugPath = rq1FolderPath + "CrossBugs" + pathSep;
		File crossBugDir = new File(crossBugPath);
		if (!crossBugDir.exists())
			crossBugDir.mkdirs();

		this.filteredRepeatedFixXmlFolderPath = filteredRepeatedFixXmlFolderPath;
		this.token = token;

		if (projectName.equals("EclipseJDTCore")) {
			scriptName = "java";
		} else if (projectName.equals("MozillaFirefox") || projectName.equals("LibreOffice")) {
			scriptName = "cpp";
		}
		
		this.formattedFixFolderPath=formattedFixFolderPath;
		this.editSeqFolderPath=editSeqFolderPath;
		this.fixFolderPath=fixFolderPath;

		batFile = new File(crossBugPath + "CrossBugDetection"+token+".bat");
		try {
			if (!batFile.exists())
				batFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String detectCrossBugRepeatedFixes() {
		getDetectionBat();
		runDetectionBat();
		
		String crossCloneInfoFolderPath=crossBugPath + "cloneInfo"+token+pathSep;
		String crossCloneXmlFolderPath=crossCloneInfoFolderPath+"cloneInfoXml"+pathSep;
		
		String crossRepeatedFixXmlFolderPath=crossBugPath+"RepeatedFixXmls"+pathSep;
		String crossFilteredRepeatedFixXmlFolderPath=crossBugPath+"FilteredRepeatedFixXmls"+pathSep;
		RepeatedFixCollector collector=new RepeatedFixCollector(crossCloneInfoFolderPath, crossCloneXmlFolderPath,editSeqFolderPath,
				crossRepeatedFixXmlFolderPath);
		collector.getRepeatedFixesBasedOnEditSeqs();
	
		RepeatedFixFilter filter=new RepeatedFixFilter();
		filter.getFilterdRepeatedFixes(crossRepeatedFixXmlFolderPath, crossFilteredRepeatedFixXmlFolderPath,fixFolderPath);
		
		return crossFilteredRepeatedFixXmlFolderPath+"a.xml";
	
	}
	
	private void getDetectionBat() {
		File xmlFileDir = new File(filteredRepeatedFixXmlFolderPath);
		File[] xmlFileList = xmlFileDir.listFiles();
		if (xmlFileList == null) {
			System.out.println("No Xml Files!");
			return;
		}

		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(batFile));
			bw.write("E:" + lineBreak);
			bw.write("cd " + crossBugPath + lineBreak);
			
			bw.write("md CloneInfo" + token + lineBreak);
			bw.write("cd CloneInfo" + token + lineBreak);
	
			bw.write("md ccfxSrc" + lineBreak);
			bw.write("md cloneInfoOut" + lineBreak);
			bw.write("md cloneInfoXml" + lineBreak);

			for (File file : xmlFileList) {
				if (file.getName().equals(".DS_Store"))
					continue;
				Scanner input = new Scanner(file);
				boolean isFirstFixInGroup = true;
				while (input.hasNext()) {
					String text = input.nextLine();
					if (text.contains("<clone id")) {
						isFirstFixInGroup = true;
					} else if (text.contains("<path>")) {
						if (isFirstFixInGroup) {
							String fixFullName = text.substring(text.lastIndexOf("\\") + 1, text.lastIndexOf("</"));
							String desAbsPath = crossBugPath + "cloneInfo"+token+pathSep+"ccfxSrc\\" + fixFullName;
							File desFixFile = new File(desAbsPath);
							if (!desFixFile.exists()) {
								bw.write("copy " + formattedFixFolderPath + fixFullName + " ccfxSrc\\" + lineBreak);
							}
						}
						isFirstFixInGroup = false;
					}
				}//end of one xml file

			} // End of all xml files
			bw.write("ccfx d " + scriptName + " -b " + token + " -d " + "ccfxSrc" + " -w w-" + lineBreak);
			bw.write("ccfx p a.ccfxd >cloneInfoOut\\a.out" + lineBreak);
			bw.write("c:\\ccfx\\bin\\scripts\\post-prettyprint.pl " + "cloneInfoOut\\a.out "
					+ "cloneInfoXml\\a.xml");
			bw.flush();
			bw.close();	
			
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
	
	private void runDetectionBat(){
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec("cmd /c " + batFile.getAbsolutePath());

			final InputStream is1 = p.getErrorStream();
			new Thread(new Runnable() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						while (br.readLine() != null)
							;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
