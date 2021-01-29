package lexer;
/*

    Filename: CommonConstants.java
    Version: 1
    ID: 1.1

 */

/*

    This file provides mapping for the entire glossary that is going
    to be used in the eMiniJava language.

    @author Ali Shokri
    @author Manan Joshi

 */

import java.util.HashMap;

public class CommonConstants {

    public enum SymbolName {

        MAIN,
        DIV, TIMES,
        ID,
        INTLIT,
        LPAREN,
        RPAREN,
        LBRACE,
        RBRACE,
        LBRACKET,
        RBRACKET,
        SEMICOLON,
        COMMA,
        DOT,
        EQ,
        GT,
        LT,
        NOT,
        QUESTION,
        PLUS,
        MINUS,
        STRING_LITERAL,
        IF,
        EQEQ,
        GTEQ,
        LTEQ,
        ANDAND,
        OROR,
        NEW,
        INT,
        ELSE,
        BOOLEAN_LITERAL,
        THIS,
        VOID,
        CLASS,
        SIDEF, STRING,
        WHILE,
        STATIC,
        PUBLIC,
        RETURN,
        BOOLEAN,
        EXTENDS,
        PRINTLN, TRUE, FALSE, LENGTH,
        EOF, BAD, PStart

    }

    public static enum AstNodeType{
        PROGRAM, MAIN_CLASS, CLASS_DECLARATION, IDENTIFIER, VAR_DECLARATION, TYPE, METHOD_DECLARATION, EXPRESSION, T, A,
        B, C, D, J, F, G, H, Q, STATEMENT, TRUE, INT_LITERAL, STRING_LITERAL, FALSE, INT_ARRAY, NEWQ;
    }

    public static HashMap<SymbolName, Integer> SymbolMapping = new HashMap<>();

    /*
        Static block that contains the mapping.
     */
    static {

        SymbolMapping.put(SymbolName.BAD, 1);
        SymbolMapping.put(SymbolName.DIV, 3);
        SymbolMapping.put(SymbolName.TIMES, 4);
        SymbolMapping.put(SymbolName.ID, 5);
        SymbolMapping.put(SymbolName.INTLIT, 6);
        SymbolMapping.put(SymbolName.LPAREN, 8);
        SymbolMapping.put(SymbolName.RPAREN, 9);
        SymbolMapping.put(SymbolName.LBRACE, 10);
        SymbolMapping.put(SymbolName.RBRACE, 11);
        SymbolMapping.put(SymbolName.LBRACKET, 12);
        SymbolMapping.put(SymbolName.RBRACKET, 13);
        SymbolMapping.put(SymbolName.SEMICOLON, 14);
        SymbolMapping.put(SymbolName.COMMA, 15);
        SymbolMapping.put(SymbolName.DOT, 7);
        SymbolMapping.put(SymbolName.EQ, 16);
        SymbolMapping.put(SymbolName.GT, 17);
        SymbolMapping.put(SymbolName.LT, 18);
        SymbolMapping.put(SymbolName.NOT, 19);
        SymbolMapping.put(SymbolName.QUESTION, 20);
        SymbolMapping.put(SymbolName.PLUS, 21);
        SymbolMapping.put(SymbolName.MINUS, 22);
        SymbolMapping.put(SymbolName.STRING_LITERAL, 26);
        SymbolMapping.put(SymbolName.BOOLEAN_LITERAL, 66);
        SymbolMapping.put(SymbolName.IF, 27);
        SymbolMapping.put(SymbolName.EQEQ, 28);
        SymbolMapping.put(SymbolName.GTEQ, 29);
        SymbolMapping.put(SymbolName.LTEQ, 30);
        SymbolMapping.put(SymbolName.ANDAND, 31);
        SymbolMapping.put(SymbolName.OROR, 32);
        SymbolMapping.put(SymbolName.NEW, 42);
        SymbolMapping.put(SymbolName.INT, 43);
        SymbolMapping.put(SymbolName.ELSE, 44);
        SymbolMapping.put(SymbolName.TRUE, 45);
        SymbolMapping.put(SymbolName.FALSE, 50);
        SymbolMapping.put(SymbolName.THIS, 46);
        SymbolMapping.put(SymbolName.MAIN, 47);
        SymbolMapping.put(SymbolName.VOID, 48);
        SymbolMapping.put(SymbolName.SIDEF, 49);
        SymbolMapping.put(SymbolName.CLASS, 51);
        SymbolMapping.put(SymbolName.WHILE, 52);
        SymbolMapping.put(SymbolName.STATIC, 53);
        SymbolMapping.put(SymbolName.STRING, 54);
        SymbolMapping.put(SymbolName.RETURN, 55);
        SymbolMapping.put(SymbolName.PUBLIC, 56);
        SymbolMapping.put(SymbolName.BOOLEAN, 57);
        SymbolMapping.put(SymbolName.EXTENDS, 58);
        SymbolMapping.put(SymbolName.PRINTLN, 60);
        SymbolMapping.put(SymbolName.LENGTH, 72);
        SymbolMapping.put(SymbolName.EOF, 0);
        SymbolMapping.put(SymbolName.PStart, -1);


    }

    /*
        This method returns the name of the Symbol based on the
        token.

        @param token
     */

    public static String getSymbolName(int token) {

        for (SymbolName symbolName : SymbolMapping.keySet())
            if (SymbolMapping.get(symbolName) == token)
                return symbolName.name();

        return SymbolName.BAD.name();

    }
}

