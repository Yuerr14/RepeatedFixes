package FixCollection.MethodLabel.CppParserAnltr;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import FixCollection.MethodLabel.CppParserAnltr.symboltable.*;


public class Listener extends CPP14BaseListener
{
	public MTable table;
	public TokenStream tokens;
	public Listener(TokenStream _tokens)
	{
		tokens = _tokens;
		table = new MTable();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterLiteral(CPP14Parser.LiteralContext ctx) 
	{	
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterFunctiondefinition(CPP14Parser.FunctiondefinitionContext ctx) 
	{ 
		Token start = ctx.start;
		Token stop = ctx.stop;
		Token body_start = ctx.functionbody().start;
		
		int start_index = start.getTokenIndex();
		int stop_index = stop.getTokenIndex();
		int body_index = body_start.getTokenIndex();
		
		MMethod method = new MMethod();
		method.startLine = start.getLine();
		method.endLine = stop.getLine();
		
		for(int i = start_index; i <= stop_index; i++)
		{
			if(i < body_index)
				method.definition.add(tokens.get(i));
			else
				method.body.add(tokens.get(i));
		}
		
		table.methods.add(method);
	}
}
