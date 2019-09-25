import java.io.IOException;
import java.io.InputStream;

public class MyParser {
	private int lookaheadToken;
	private InputStream in;

	// ////////////////////////////////////////////////////////////////////////////////////
	public MyParser(InputStream inputS) throws IOException {
		this.in = inputS;
		lookaheadToken = in.read();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	private void consume(int symbol) throws IOException, ParserError {
		if (lookaheadToken != symbol) {
			throw new ParserError();
		}
		lookaheadToken = in.read();
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public String expr() throws ParserError, IOException {
		String returnString = term();
		String finalReturnString = expr2(returnString);
		return finalReturnString;
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public String expr2(String prevLT) throws ParserError, IOException {
		if (lookaheadToken == '+' || lookaheadToken == '-') {
			String operator = Character.toString((char) lookaheadToken); // operator
																			// string
			consume(lookaheadToken);
			String returnString = term();
			prevLT = "(" + operator + " "+prevLT+" " + returnString + ")"; // Parenthesis+op+soFarString+termString+Parenthesis
			prevLT = expr2(prevLT);

		} else if (lookaheadToken == '\n') { // Case newline.
			return prevLT;
		} else { // Case not consume.
			return prevLT;
		}
		return prevLT;

	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public String term() throws ParserError, IOException {

		if ((lookaheadToken < '0' || lookaheadToken > '9')
				&& lookaheadToken != '(') {
			throw new ParserError();
		} else if (lookaheadToken != '(') {
			String argLT = Character.toString((char) this.lookaheadToken); // keep
																			// current
																			// lookaheadtoken.
			consume(this.lookaheadToken);
			String rt = term2(argLT); // pass it to the function.
			return rt;
		} else { // Case of a parenthesis spotted.call factor.
			String returnString = factor();
			String term2ReturnString = term2(returnString);
			return term2ReturnString;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public String term2(String prevLT) throws ParserError, IOException {
		if (lookaheadToken == '*' || lookaheadToken == '/') {
			String operator = Character.toString((char) this.lookaheadToken);
			consume(this.lookaheadToken);
			if ((lookaheadToken < '0' || lookaheadToken > '9')
					&& lookaheadToken != '(') {
				throw new ParserError();
			} else if (lookaheadToken != '(') {
				String temp = Character.toString((char) this.lookaheadToken);
				consume(lookaheadToken);
				String passLT = "(" + operator +" "+prevLT+" "+ temp + ")"; //
				prevLT = term2(passLT);
			} else if (lookaheadToken == '(') {
				String returnString = factor();
				returnString = operator + " "+prevLT+" "+ returnString;
				String returnString2 = term2(returnString);
				//System.out.println(returnString2);
				return "(" + returnString2 + ")";
			}

		} else if (lookaheadToken == '\n') {
			return prevLT;
		}
		return prevLT;
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public String factor() throws ParserError, IOException {

		if (lookaheadToken == '(') {
			consume(this.lookaheadToken);
			String returnString = expr();
			consume(')');
			return returnString;
		} else if (lookaheadToken != '(') {
			throw new ParserError();
		}
		return "";

	}

	// //////////////////////////////////////////////////////////////////////////////////////

	public void parse() throws IOException, ParserError {
		String prefixNotation = expr();

		if (lookaheadToken != '\n' && lookaheadToken != -1
				&& lookaheadToken != 13) {
			throw new ParserError();
		}
		System.out.println(prefixNotation);
	}
}
