package FixCollection.MethodLabel.CppParserAnltr.symboltable;

import java.util.Vector;

import org.antlr.v4.runtime.Token;


public class MMethod 
{
	public int startLine;
	public int endLine;
	public Vector<Token> definition = new Vector<Token>();
	public Vector<Token> body = new Vector<Token>();
}
