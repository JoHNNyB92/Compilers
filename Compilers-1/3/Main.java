import parser.*;
import lexer.*;
import node.*;
import java.io.*;

public class Main{

    public static void main(String[] args){
	try{
	    Parser p = new Parser(new Lexer(new PushbackReader(new InputStreamReader(System.in), 1024)));

	    p.parse();
	}
	catch(ParserException ex){
	    System.err.println(ex.getMessage());
	}
	catch(LexerException ex){
	    System.err.println(ex.getMessage());
	}
	catch(IOException ex){
	    System.err.println(ex.getMessage());
	}
	catch(Exception ex){
	    System.err.println(ex.getMessage());
	}
    }

}