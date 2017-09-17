package FixCollection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class FixFilterTwo {
	int diffCount = 0;
	int fixCount = 0;
	String diffHeaderInMethodLabel = "diff --git";
	String hunkSuffix = null;

	String lineBreak = "\r\n";
	String pathBreak = "\\";

	public void performSecondFilter(String patchAfterFilter1FolderPath, String fixFolderPath,
			String methodLabelFolderPath) {
		File patchFileDir = new File(patchAfterFilter1FolderPath);
		File[] fileList = patchFileDir.listFiles();
		if (fileList == null) {
			System.out.println("No Files!");
			return;
		}
		// create fixFolder
		File fixDir = new File(fixFolderPath);
		if (!fixDir.exists())
			fixDir.mkdirs();

		for (File file : fileList) {
			if (file.getName().equals(".DS_Store"))
				continue;
			patchToFixes(file.getAbsolutePath(), fixFolderPath);
		}

		filterFixNotInMethods(fixFolderPath, methodLabelFolderPath);
	}

	private void patchToFixes(String patchfilePath, String fixFolderPath) {
		try {
			File file = new File(patchfilePath);
			Scanner input = new Scanner(file);

			diffCount = 0;// 当前patch文件的第几个diff
			fixCount = 0;// 当前diff的第几个continuous change

			Vector<String> hunkContent = new Vector<String>();
			boolean isBelowDiffAndAboveAt = false;
			String sourceFileSuffix = null;
			while (input.hasNext()) {
				String text = input.nextLine();
				// System.out.println(text);
				if (text.startsWith("diff")) {
					if (!hunkContent.isEmpty()) {// 上一个diff的最后一个@@
						processSingleHunk(hunkContent, patchfilePath, fixFolderPath);
						hunkContent.clear();
					}
					hunkSuffix = text.substring(text.lastIndexOf("."));// 处理完上一个hunk再更新hunkSuffix，因为上一个hunk的处理要根据上一个hunkSuffix来进行
					diffCount++;
					fixCount = 0;
					isBelowDiffAndAboveAt = true;

				} else if (text.startsWith("@@")) {
					if (!hunkContent.isEmpty()) {// 处理上一个@@
						processSingleHunk(hunkContent, patchfilePath, fixFolderPath);
						hunkContent.clear();
					}
					isBelowDiffAndAboveAt = false;
				} else if (!isBelowDiffAndAboveAt) {
					hunkContent.add(text);
				}
			}
			input.close();
			if (!hunkContent.isEmpty()) {// 最后一个diff的最后一个@@
				processSingleHunk(hunkContent, patchfilePath, fixFolderPath);
				hunkContent.clear();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void processSingleHunk(Vector<String> hunkContent, String patchfilePath, String fixFolderPath)
			throws IOException {
		FileWriter output = null;
		int firstIndex = -1, lastIndex = -1;// hunk中第1个editLine和最后一个editLine的下标
		// 计算hunk最上面的editLine所在下标
		for (int i = 0; i < hunkContent.size(); i++) {
			if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {
				firstIndex = i;
				break;
			}
		}
		// 计算hunk最下面的editLine所在下标
		for (int i = hunkContent.size() - 1; i >= 0; i--) {
			if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {
				lastIndex = i;
				break;
			}
		}
		//// 在firstIndex和lastIndex之间，将会是一个连续的edit,一个连续的上下文，一个连续的edit,一个连续的上下文……最后跟着一个连续的edit
		// 所以连续edit(即fix的数目)应该是连续上下文个数+1
		if (firstIndex >= 0 && lastIndex >= 0 && lastIndex >= firstIndex) {
			// 这是对应的第一个fix的fixCount和file
			fixCount++;
			File outputFile = new File(fixFolderPath
					+ patchfilePath.substring(patchfilePath.lastIndexOf(pathBreak), patchfilePath.lastIndexOf("."))
					+ "_" + diffCount + "_" + fixCount + hunkSuffix);
			output = new FileWriter(outputFile);

			boolean isFirstEditLineExist = false;
			boolean isFirstContextLineExist = false;
			for (int i = firstIndex; i <= lastIndex; i++) {
				// System.out.println(hunkContent.elementAt(i));
				if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {// 进入一个连续的edit
					if (!isFirstEditLineExist) {// 遇到了某个fix的第一个editLine
						output.write(hunkContent.elementAt(i));
						isFirstEditLineExist = true;
					} else {// 某个fix的editLine,但不是第一条
						output.write(lineBreak + hunkContent.elementAt(i));
					}
					isFirstContextLineExist = false;

				} else {// 进入一个连续的上下文
					if (!isFirstContextLineExist) {// 遇到了第一个连续context的第一行（如果不是第一行就不要创建新文件了，否则会多创建
						output.close();
						// 即将进入下一个连续的edit,因为firstIndex和lastIndex之间，处理完第一个fix之后，后面就是一个连续上下文、接着一个连续的edit
						fixCount++;
						outputFile = new File(fixFolderPath
								+ patchfilePath.substring(patchfilePath.lastIndexOf(pathBreak),
										patchfilePath.lastIndexOf("."))
								+ "_" + diffCount + "_" + fixCount + hunkSuffix);
						output = new FileWriter(outputFile);
						isFirstContextLineExist = true;
					}
					isFirstEditLineExist = false;
				}
			} // firstIndex和lastIndex之间遍历完成
		}

		if (output != null)
			output.close();

	}

	private void filterFixNotInMethods(String fixFolderPath, String methodLabelFolderPath) {
		File file = new File(methodLabelFolderPath);
		File[] fileList = file.listFiles();
		// Vector<String> betweenMethodName = new Vector<String> ();
		// //记录是method内部的hunk名，此处未包含后缀名，仅包括bugID_patchNum_diffNum_hunkNum
		HashMap<String, Integer> betweenMethodName = new HashMap<String, Integer>();
		for (File tmp : fileList) {
			if (tmp.getName().equals(".DS_Store"))
				continue;
			String patchName = tmp.getName().substring(0, tmp.getName().indexOf("."));
			int diffNum = 0, fixNum = 0;
			try {
				Scanner input = new Scanner(tmp);
				while (input.hasNextLine()) {
					String line = input.nextLine();
					if (line.startsWith(diffHeaderInMethodLabel)) {
						diffNum++;
						fixNum = 0;
					}
					if (line.startsWith("@@")) {
						fixNum++;
						// System.out.println(line+"===");
						if (!line.startsWith("@@ not in")) {
							// System.out.println(line+"===");
							String fixName = patchName + "_" + diffNum + "_" + fixNum;
							betweenMethodName.put(fixName, 1);
							// System.out.println(fixName);
						}
					}
				}
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		file = new File(fixFolderPath);
		fileList = file.listFiles();
		for (File tmp : fileList) {
			if (tmp.getName().equals(".DS_Store"))
				continue;
			// System.out.println(tmp.getName().substring(0,
			// tmp.getName().indexOf(".")));
			if (!betweenMethodName.containsKey(tmp.getName().substring(0, tmp.getName().indexOf(".")))) {
				tmp.delete();
			}
		}

	}
}
