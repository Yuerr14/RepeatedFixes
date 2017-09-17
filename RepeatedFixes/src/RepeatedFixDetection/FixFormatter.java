package RepeatedFixDetection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jdt.internal.core.util.LineNumberAttribute;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;
/**
 * FormattedFix:
 * 1. remove add/delete sign for added/deleted line
 * 2. add {} for deleted range and added range, insure the line numbers don't change at the same time
 * 
 * EditSeq:
 * 1. recode * for added/deleted comment/blank line
 * 2. record + for meaningful code lines (non-comment/blank lines)starting with plus sign
 * 3. record - for meaningful code lines (non-comment/blank lines)starting with minus sign
 */

public class FixFormatter {
	String lineBreak= System.getProperty("line.separator");
	String plusSign = "+", minusSign = "-",blankCommentSign="*";
	
	public void getFormattedFixes(String fixFolderPath, String formattedFixFolderPath, String editSeqFolderPath) {
		makeFolder(formattedFixFolderPath);
		makeFolder(editSeqFolderPath);
		
		File fixDir = new File(fixFolderPath);
		File[] fixFileList = fixDir.listFiles();
		if (fixFileList == null) {
			System.out.println("No Files!");
			return;
		}
		for (File fixFile : fixFileList) {
			String fixFileName = fixFile.getName();
			File formattedFixFile = makeNewFile(formattedFixFolderPath + fixFileName);
			File editSeqFile = makeNewFile(editSeqFolderPath + fixFileName);
			formatSingleFix(fixFile, formattedFixFile, editSeqFile);
		}
	}

	private void makeFolder(String folderPath) {
		File dir = new File(folderPath);
		if (!dir.exists())
			dir.mkdirs();
	}

	private File makeNewFile(String filePath) {
		File file = new File(filePath);
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private void formatSingleFix(File fixFile, File formattedFixFile, File editSeqFile) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(fixFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(formattedFixFile));
			BufferedWriter bwSeq = new BufferedWriter(new FileWriter(editSeqFile));
			String str = null;
			boolean alreadyMeetFirstAddLine = false;
			str = br.readLine();
			bw.write(getSign(str));
			bw.write("{" + removePlusOrMinusSign(str));
			if (str.startsWith(plusSign))
				alreadyMeetFirstAddLine = true;
			while ((str = br.readLine()) != null) {
				bw.write(lineBreak+getSign(str));
				if (str.startsWith(plusSign) && !alreadyMeetFirstAddLine) {
					alreadyMeetFirstAddLine = true;
					bw.write("}" + lineBreak + "{" + removePlusOrMinusSign(str));
					continue;
				}
				bw.write(lineBreak+removePlusOrMinusSign(str));
			}
			bw.write("}");			
			bw.flush();
			bw.close();
			bwSeq.flush();
			bwSeq.close();
		}catch(IOException e){
			e.printStackTrace();
		}		

	}
	
	private String removePlusOrMinusSign(String str) {
		String removedStr = null;
		if (str.startsWith(plusSign))
			removedStr = str.substring(str.indexOf(plusSign) + 1);
		else if (str.startsWith(minusSign))
			removedStr = str.substring(str.indexOf(minusSign) + 1);
		return removedStr;
	}
	
	private String getSign(String editedStr){
		String resultSign=null;
		String codeStr = null;
		if (editedStr.startsWith(plusSign)) {
			resultSign = plusSign;
			codeStr = editedStr.substring(editedStr.indexOf(plusSign) + 1);
		} else if (editedStr.startsWith(minusSign)) {
			resultSign = minusSign;
			codeStr = editedStr.substring(editedStr.indexOf(minusSign) + 1);
		}
		
		if(isBlankOrCommentLine(codeStr)){
			resultSign=blankCommentSign;
		}
		return resultSign;	
	}
	private boolean isBlankOrCommentLine(String codeLine){	
		char firstNotEmptyChar = ' ';
		char theCharAfterFirstNotEmptyChar = ' ';
		for (int i = 0; i < codeLine.length(); i++) {
			//set theCharAfterFirstNotEmptyChar
			if (firstNotEmptyChar != ' ') {
				theCharAfterFirstNotEmptyChar = codeLine.charAt(i);
				break;
			}
			//set firstNotEmptyChar
			if (!(codeLine.charAt(i) == ' ') && !(codeLine.charAt(i) == '\t')) {
				if (firstNotEmptyChar == ' ') {
					firstNotEmptyChar = codeLine.charAt(i);
				}
			}
		}
		if (firstNotEmptyChar == ' ')
			return true;//blank line
		if (firstNotEmptyChar == '*')
			return true;//comment line
		if (firstNotEmptyChar == '/' && theCharAfterFirstNotEmptyChar == '/')
			return true;//comment line
		if (firstNotEmptyChar == '/' && theCharAfterFirstNotEmptyChar == '*')
			return true;//comment line
		return false;		
	}

}
