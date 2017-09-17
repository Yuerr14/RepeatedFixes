package FixCollection.MethodLabel.CppParser;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;

import FixCollection.MethodLabel.CppParser.symboltable.MMethod;
import FixCollection.MethodLabel.CppParser.symboltable.MTable;

public class CPPBuildSymbolTableVisitor extends ASTGenericVisitor
{
	public MTable table;
	public CPPBuildSymbolTableVisitor(boolean visitNodes) {
		super(visitNodes);
		// TODO Auto-generated constructor stub
		table = new MTable();
	}
	
	public int visit(IASTDeclaration declaration) 
	{
		if(declaration instanceof CPPASTFunctionDefinition)
		{
			try 
			{
				CPPASTFunctionDefinition functiondecl = (CPPASTFunctionDefinition)declaration;
				IASTDeclSpecifier specifier = functiondecl.getDeclSpecifier();
				CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator) functiondecl.getDeclarator();
				CPPASTCompoundStatement statement = (CPPASTCompoundStatement) functiondecl.getBody();	
				
				ArrayList<String> decl = new ArrayList<String>();
				IToken token = null;
				if(specifier != null)
				{
					try
					{
						token = specifier.getSyntax();
						while(token != null)
						{
							decl.add(token.getImage());
							token = token.getNext();
						}
					}
					catch(Exception e)
					{
					}
				}
				token = functionDeclarator.getSyntax();
				while(token != null)
				{
					decl.add(token.getImage());
					token = token.getNext();
				}
				
				ArrayList<String> body = new ArrayList<String>();
				token = statement.getSyntax();
				while(token != null)
				{
					body.add(token.getImage());
					token = token.getNext();
				}
				
				int start = functiondecl.getOffset();
				int length = functiondecl.getLength();
				int bodyStart = statement.getOffset();
				
				MMethod method = new MMethod(start, length, bodyStart, decl, body);
				table.methods.add(method);
				//System.out.println(decl);
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return PROCESS_CONTINUE;
	}
}
