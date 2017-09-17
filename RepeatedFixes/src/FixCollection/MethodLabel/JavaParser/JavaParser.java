package FixCollection.MethodLabel.JavaParser;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.zip.CRC32;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import FixCollection.MethodLabel.JavaParser.symboltable.MTable;
import FixCollection.MethodLabel.JavaParser.symboltable.MMethod;
import FixCollection.MethodLabel.JavaParser.symboltable.MParameter;

public class JavaParser 
{
	private String lineBreak="\n";
	private ASTParser parser;
	private CompilationUnit cu;
	public BuildSymbolTableVisitor build;
	private String javafile;
	public void release()
	{
		if(cu != null)
			cu.delete();
	}
	public JavaParser()
	{
		parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
				
		Map<String, String> compilerOptions = JavaCore.getOptions();
		compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7); 
		compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		parser.setCompilerOptions(compilerOptions); 
	}
	public void init(String absolutePath)
	{
		if(cu != null)
			cu.delete();
		char[] src = inputfile(absolutePath);
		parser.setSource(src);
		cu = (CompilationUnit)parser.createAST(null);
		build = new BuildSymbolTableVisitor();
		cu.accept(build);
	}
	private char[] inputfile(String filename) 
	{
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
		StringBuffer sb = new StringBuffer();
		String line;  
        try {
			while ((line = reader.readLine()) != null) 
			{  
				sb.append(line + lineBreak); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    javafile = sb.toString();
	    //System.out.println(javafile);
	    try {
			fs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return javafile.toCharArray();
	}
	public String judge(int line, int length)
	{
		if(length == 0)
			return "no modify";
		List<String> methods = getMethodHashbyline(line, line + length - 1);
		if(methods.size() == 0)
			return "not in a method";
		if(methods.size() == 1)
			return methods.get(0);
		return "in many methods";
	}

	public List<String> getMethodHashbyline(int line, int last)
	{
		//Transfer line and last(line of source file) to the start token index, because jdt/cdt's position is computed in token
		int position = 0;
		int endposition = 0;
		String javaTemp = javafile;
		while(last > 0)
		{
			line--;
			last--;
			if(javaTemp.indexOf(lineBreak) == -1)
				break;
			String aline = javaTemp.substring(0,javaTemp.indexOf(lineBreak));
			if(line > 0)
				position += aline.length() + 1;
			endposition += aline.length() + 1;
			javaTemp = javaTemp.substring(javaTemp.indexOf(lineBreak) + 1);
		}
		
		List<String> _ret = new ArrayList<String>();
		MTable table=build.table;
		MMethod method = null;
			for(MMethod t_method: table.methods)
			{
				boolean in = false;
				if(t_method.bodystartPosition < position && endposition < t_method.endPosition)
					in = true;
				if(in)
				{
					method = t_method;
					StringBuffer sHash = new StringBuffer();
					for(String t:method.modifiers)
						sHash.append(t + " ");
					sHash.append(" " + method.return_value + " ");
					sHash.append(" " + method.name + " (");
					for(MParameter t:method.parameters)
					{
						sHash.append(" " + t.type + " ");
						sHash.append(t.name + ",");
					}
					sHash.append(")");
					_ret.add(sHash.toString());
				}
			}
	
		//String methodbody = javafile.substring(max, min);
		//System.out.println(methodbody);
		return _ret;
	}
}