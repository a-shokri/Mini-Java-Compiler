package parser;

public class ParseException extends Exception {

    public ParseException(int e, int f, String message) {
        super("line: " + e + ", Column: " + f + " : Parser Error: " + message);
    }

}
