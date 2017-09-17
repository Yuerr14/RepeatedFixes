package FixCollection.MethodLabel.CppParserAnltr;

import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.*;

import FixCollection.MethodLabel.CppParserAnltr.symboltable.*;

public class CppParser 
{
	public MTable table;
	public void init(String path)
	{
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner sc = new Scanner(fi);
		StringBuffer src = new StringBuffer();
		while(sc.hasNextLine())
		{
			String line = sc.nextLine();
			if(line.startsWith("namespace"))
			{
				if(line.endsWith("{"))
					src.append("namespace {\n");
				else
					src.append("namespace \n");
			}
			else
				src.append(line + "\n");
		}
		CharStream input = (CharStream)(new ANTLRInputStream(src.toString()));
		CPP14Lexer lexer = new CPP14Lexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		//tokenStream.consume();
		CPP14Parser parser = new CPP14Parser(tokenStream);
		CPP14Parser.TranslationunitContext tu = parser.translationunit();
		Listener listener = new Listener(tokenStream);
		ParseTreeWalker.DEFAULT.walk(listener, tu);
		table = listener.table;
		try {
			fi.close();
		} catch (IOException e) {
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
	
	private List<String> getMethodHashbyline(int line, int last)
	{
		
		List<String> _ret = new ArrayList<String>();
		MMethod method = null;
		for(MMethod t_method: table.methods)
		{
			boolean in = false;
			int bodyStartLine = t_method.startLine;
			try
			{
				bodyStartLine = t_method.body.get(0).getLine();
			}
			catch(Exception e){}
			if(bodyStartLine <= line && last <= t_method.endLine)
				in = true;
			if(in)
			{
				method = t_method;
				StringBuffer sHash = new StringBuffer();
				for(Token token:method.definition)
				{
					sHash.append(token.getText() + " ");
				}
				_ret.add(sHash.toString());
			}
		}
		return _ret;
	}
	public static void main(String args[])
	{
		CppParser cpp = new CppParser();
		cpp.init("hello.cpp");
		System.out.println(cpp.judge(29, 1));
	}
}
