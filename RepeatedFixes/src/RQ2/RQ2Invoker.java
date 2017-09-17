package RQ2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RQ2Invoker {
	String rq2FolderPath = null,filteredRepeatedFixXmlFolderPath=null,methodLabelFolderPath=null;
	
	String pathSep = File.separator;
	String lineBreak= System.getProperty("line.separator");

	public RQ2Invoker(String rq2FolderPath, String filteredRepeatedFixXmlFolderPath,String methodLabelFolderPath) {
		this.rq2FolderPath = rq2FolderPath;
		this.filteredRepeatedFixXmlFolderPath=filteredRepeatedFixXmlFolderPath;
		this.methodLabelFolderPath=methodLabelFolderPath;
	}
	
	public void computeTable5(){
		int totalRepeatedFixGroupCnt = 0;	
		int groupCntWithAllFixesInSamePackage = 0;
		int groupCntWithAllFixesInSameFile = 0;
		int groupCntWithAllFixesInSameMethod = 0;

		File dir = new File(filteredRepeatedFixXmlFolderPath);
		File[] xmlFiles = dir.listFiles();
		Scanner input = null;
		if (xmlFiles == null) {
			System.err.println("No Xml Files Found!");
			return;
		}
		ArrayList<String> instanceInfoList = new ArrayList<String>();
		for (File xmlFile : xmlFiles) {
			try {
				input = new Scanner(xmlFile);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (input.hasNextLine()) {
				String tmp = input.nextLine();
				if (tmp.contains("<clone id=")) {//enter one repeatedFixGroup
					totalRepeatedFixGroupCnt++;
					instanceInfoList.clear();

				} else if (tmp.contains("<path>")) {
					String fixName = tmp.substring(tmp.lastIndexOf(pathSep) + 1, tmp.lastIndexOf("."));
					String instanceInfo = getInstanceInfo(fixName);
					instanceInfoList.add(instanceInfo);

				} else if (tmp.contains("</clone>")) {
					boolean flagGroupCntWithAllFixesInSamePackage = true;
					boolean flagGroupCntWithAllFixesInSameFile = true;
					boolean flagGroupCntWithAllFixesInSameMethod = true;
				

					for (int i = 0; i < instanceInfoList.size() - 1; i++)
						for (int j = i + 1; j < instanceInfoList.size(); j++) {
							String instanceInfo1 = instanceInfoList.get(i);
							String[] tmpInfo1 = instanceInfo1.split("##");
							String packageName1 = null;
							try {
								packageName1 = tmpInfo1[0].substring(tmpInfo1[0].indexOf("/") + 1,
										tmpInfo1[0].lastIndexOf("/"));
							} catch (Exception e) {
								System.out.println(e.getMessage());
							}

							String fileName1 = tmpInfo1[0].substring(tmpInfo1[0].lastIndexOf("/") + 1);
							String methodName1 = null;
							if(tmpInfo1.length>2)
								methodName1=tmpInfo1[2];
							
							String instanceInfo2 = instanceInfoList.get(j);
							String[] tmpInfo2 = instanceInfo2.split("##");
							String packageName2 = null;
							try {
								packageName2 = tmpInfo2[0].substring(tmpInfo2[0].indexOf("/") + 1,
										tmpInfo2[0].lastIndexOf("/"));
							} catch (Exception e) {
								System.out.println(e.getMessage());
							}

							String fileName2 = tmpInfo2[0].substring(tmpInfo2[0].lastIndexOf("/") + 1);
							String methodName2=null;
							if(tmpInfo2.length>2)
								methodName2=tmpInfo2[2];
						

							if (packageName1.equals(packageName2)) {//In Same Package
								if (fileName1.equals(fileName2)) {//In Same File
								//System.out.println(methodName1+" "+methodName2);
									if(methodName1 != null && methodName2 != null)
									{//In Method
										if (!methodName1.equals(methodName2)) {// Not in Same Method
											flagGroupCntWithAllFixesInSameMethod = false;
										}
									}
								
								} else {//Not In Same File
									flagGroupCntWithAllFixesInSameFile = false;	
									flagGroupCntWithAllFixesInSameMethod =false;
								}
							} else {//Not In Same Package
								flagGroupCntWithAllFixesInSamePackage = false;
								flagGroupCntWithAllFixesInSameFile = false;	
								flagGroupCntWithAllFixesInSameMethod =false;
							}												
						}
									
					if(flagGroupCntWithAllFixesInSamePackage)
						groupCntWithAllFixesInSamePackage++;
					if(flagGroupCntWithAllFixesInSameFile)
						groupCntWithAllFixesInSameFile++;
					if(flagGroupCntWithAllFixesInSameMethod)
						groupCntWithAllFixesInSameMethod++;		
				}
			} 	
		} 
		
		try{
			File resultFile = new File(rq2FolderPath + "Table5.txt");
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
			
	        bw.write("TotalRepeatedFixGroup:"+totalRepeatedFixGroupCnt+lineBreak);
	        bw.write("GroupCntWithAllFixesInSamePackage:"+groupCntWithAllFixesInSamePackage+lineBreak);
	        bw.write("GroupCntWithAllFixesInSameFile:"+groupCntWithAllFixesInSameFile+lineBreak);
	        bw.write("GroupCntWithAllFixesInSameMethod:"+groupCntWithAllFixesInSameMethod+lineBreak);
	        bw.write(lineBreak);
	        bw.flush();
	        bw.close();
			
		}catch(IOException e){
			e.printStackTrace();
			
		}	
	}
	private String getInstanceInfo(String fixName) {

		String[] tmp = fixName.split("_");
		String methodLabelFileName = tmp[0] + "_" + tmp[1] + ".patch.out";
		int diffindex = Integer.parseInt(tmp[2]);
		int hunkindex = Integer.parseInt(tmp[3]);

		File methodLabelFile = new File(methodLabelFolderPath + methodLabelFileName);
		Scanner scan = null;
		try {
			scan = new Scanner(new FileInputStream(methodLabelFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer labelFileContent = new StringBuffer();
		while (scan.hasNextLine())
			labelFileContent.append(scan.nextLine() + "\n");

		String[] diffLabels = labelFileContent.toString().split("diff --git ");
		if (diffLabels.length <= diffindex)
			return null;
		String currentDiffLabel = diffLabels[diffindex];
		String[] hunkLabels = currentDiffLabel.split("@@ ");
		String sourcefilenameAndSuperClass = hunkLabels[0];
		String sourcefilename = sourcefilenameAndSuperClass;
		if (sourcefilenameAndSuperClass.contains("##")) {
			sourcefilename = sourcefilenameAndSuperClass.substring(0, sourcefilenameAndSuperClass.indexOf("##"));
		}

		if (hunkLabels.length <= hunkindex)
			return null;
		String currentHunkLabel = hunkLabels[hunkindex];
		if (currentHunkLabel.contains("not in a method") || currentHunkLabel.contains("in many methods")
				|| currentHunkLabel.contains("exception occur") || currentHunkLabel.contains("no a java file")) {
			currentHunkLabel ="";
		}
		//System.out.println("CurrentHunLabel:"+currentHunkLabel);
		return sourcefilename + "##" + currentHunkLabel;
	}


}
