import java.io.IOException;

public class Main {
	public static void main(String[] args) throws ParserError {
		try {
			MyParser parser = new MyParser(System.in);
			parser.parse();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
