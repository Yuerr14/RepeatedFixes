package FixCollection.MethodLabel.CppParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2TestBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;

import FixCollection.MethodLabel.CppParser.symboltable.*;

public class CPPParser 
{
	public IASTTranslationUnit tu;
	public MTable table;
	public String code;
	public void init(String path, ParserLanguage lang)
	{
		AST2TestBase ast = new AST2TestBase();
		try {
			StringBuffer codeBuffer = new StringBuffer();
			FileInputStream cfile = new FileInputStream(new File(path));
			Scanner sc = new Scanner(cfile);
			while(sc.hasNextLine())
			{
				codeBuffer.append(sc.nextLine() + "\n");
			}
			cfile.close();
			code = codeBuffer.toString();
			IASTTranslationUnit tu = ast.parse(code, lang, false, false);
			CPPBuildSymbolTableVisitor build = new CPPBuildSymbolTableVisitor(true);
			tu.accept(build);
			table = build.table;
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init(String path, ASTVisitor visitor)
	{
		AST2TestBase ast = new AST2TestBase();
		try {
			StringBuffer codeBuffer = new StringBuffer();
			FileInputStream cfile = new FileInputStream(new File(path));
			Scanner sc = new Scanner(cfile);
			while(sc.hasNextLine())
			{
				String line = sc.nextLine() + "\n";
				line = line.replaceAll("MOZ_FINAL", "         ");
				line = line.replaceAll("MOZ_STACK_CLASS", "               ");
				codeBuffer.append(line);
			}
			cfile.close();
			code = codeBuffer.toString();
			IASTTranslationUnit tu = ast.parse(code, ParserLanguage.CPP, false, false);
			tu.accept(visitor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		int position = 0;
		int endposition = 0;
		String codeTemp = code;
		while(last > 0)
		{
			line--;
			last--;
			if(codeTemp.indexOf("\n") == -1)
				break;
			String aline = codeTemp.substring(0,codeTemp.indexOf("\n"));
			if(line > 0)
				position += aline.length() + 1;
			endposition += aline.length() + 1;
			codeTemp = codeTemp.substring(codeTemp.indexOf("\n") + 1);
		}
		
		List<String> _ret = new ArrayList<String>();
		MMethod method = null;
		for(MMethod t_method: table.methods)
		{
			boolean in = false;
			if(t_method.bodyStart < position && endposition < t_method.endOffset)
				in = true;
			if(in)
			{
				method = t_method;
				StringBuffer sHash = new StringBuffer();
				for(String t:method.declarator)
					sHash.append(t + " ");
				_ret.add(sHash.toString());
			}
		}
		return _ret;
	}
	

}
