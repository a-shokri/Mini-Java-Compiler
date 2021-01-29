package parser;

import ast.*;
import lexer.CommonConstants;
import lexer.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private void advance() {

        try {
            lexer.next_token();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            System.out.println( e.getMessage() );

        }

    }

    private void eat(CommonConstants.SymbolName symbolName) throws ParseException {

        int symbolInt = CommonConstants.SymbolMapping.get(symbolName);

        if (lexer.getCurrent().sym == symbolInt) {
            advance();
        } else {
            error(symbolName);
        }

    }

    public Tree parseTree() throws ParseException {

        try {

            lexer.next_token();
            return parseProgram();

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new ParseException(lexer.getCurrent().getLine(), lexer.getCurrent().getColumn(), "UNKNOWN");

    }

    private Program parseProgram() throws ParseException, IOException {

        MainClass mainClass = parseMainClass();
        List<ClassDeclaration> classDeclarations = new ArrayList<>();

        while (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.CLASS.toString()))
            classDeclarations.add(parseClassDeclaration());

        if (!CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.EOF.toString()))
            error(CommonConstants.SymbolName.EOF);
        Program p = new Program(mainClass, classDeclarations);
        p.setLine(mainClass.getLine());
        return p;

    }

    private MainClass parseMainClass() throws ParseException, IOException {

        eat(CommonConstants.SymbolName.CLASS);

        Identifier classIdentifier = parseIdentifier();

        eat(CommonConstants.SymbolName.ID);
        eat(CommonConstants.SymbolName.LBRACE);
        eat(CommonConstants.SymbolName.PUBLIC);
        eat(CommonConstants.SymbolName.STATIC);
        eat(CommonConstants.SymbolName.VOID);
        eat(CommonConstants.SymbolName.MAIN);
        eat(CommonConstants.SymbolName.LPAREN);
        eat(CommonConstants.SymbolName.STRING);
        eat(CommonConstants.SymbolName.LBRACKET);
        eat(CommonConstants.SymbolName.RBRACKET);

        Identifier methodIdentifier = parseIdentifier();

        eat(CommonConstants.SymbolName.ID);
        eat(CommonConstants.SymbolName.RPAREN);
        eat(CommonConstants.SymbolName.LBRACE);

        ArrayList<Statement> statement = new ArrayList<>();
        while (!(lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.RBRACE)))
            statement.add(parseStatement(null));

        eat(CommonConstants.SymbolName.RBRACE);
        eat(CommonConstants.SymbolName.RBRACE);


        return new MainClass(classIdentifier, methodIdentifier, statement);

    }

    private ClassDeclaration parseClassDeclaration() throws ParseException, IOException {

        eat(CommonConstants.SymbolName.CLASS);

        Identifier classIdentifier = parseIdentifier();

        eat(CommonConstants.SymbolName.ID);

        Identifier extendsIdentifier = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EXTENDS)) {
            eat(CommonConstants.SymbolName.EXTENDS);
            extendsIdentifier = parseIdentifier();
            eat(CommonConstants.SymbolName.ID);
        }

        eat(CommonConstants.SymbolName.LBRACE);

        List<VarDeclaration> varDeclarations = new ArrayList<>();
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();

        while (lexer.getCurrent().sym != CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.RBRACE)) {

            while (lexer.getCurrent().sym != CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.PUBLIC)) {
                varDeclarations.add(parseVarDeclaration());
            }
            while (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.PUBLIC)) {
                methodDeclarations.add(parseMethodDeclaration());
            }

        }

        eat(CommonConstants.SymbolName.RBRACE);

        return new ClassDeclaration(classIdentifier, extendsIdentifier, varDeclarations, methodDeclarations);

    }

    private Identifier parseIdentifier() throws ParseException {

        Identifier identifier = null;

        if (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.ID.toString())) {
            identifier = new Identifier(lexer.getCurrent().value.toString());
            identifier.setLine(lexer.getCurrent().getLine());
            identifier.setColumn(lexer.getCurrent().getColumn());
        } else
            error(CommonConstants.SymbolName.ID);

        //lexer.next_token();

        return identifier;

    }

    private Statement parseStatement(Identifier paramIdentifier) throws ParseException, IOException {

        if (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.LBRACE.toString())) {

            eat(CommonConstants.SymbolName.LBRACE);

            List<Statement> statements = new ArrayList<>();

            while (!(CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.RBRACE.toString()))) {

                Statement statement = parseStatement(null);
                statements.add(statement);

            }
            eat(CommonConstants.SymbolName.RBRACE);
            return new Block(statements);

        } else if (CommonConstants.getSymbolName(lexer.getCurrent().sym) == CommonConstants.SymbolName.IF.toString()) {

            eat(CommonConstants.SymbolName.IF);
            eat(CommonConstants.SymbolName.LPAREN);

            Expression expression = parseExpression();

            eat(CommonConstants.SymbolName.RPAREN);

            Statement ifStatement = parseStatement(null);

            Skip skip = new Skip();

            Statement elseStatement = null;

            if (CommonConstants.getSymbolName(lexer.getCurrent().sym) == CommonConstants.SymbolName.ELSE.toString()) {
                eat(CommonConstants.SymbolName.ELSE);
                elseStatement = parseStatement(null);
            }

            return new Block(new IfThenElse(expression, ifStatement, elseStatement));

        } else if (CommonConstants.getSymbolName(lexer.getCurrent().sym) == CommonConstants.SymbolName.WHILE.toString()) {

            eat(CommonConstants.SymbolName.WHILE);
            eat(CommonConstants.SymbolName.LPAREN);

            Expression whileExpression = parseExpression();

            eat(CommonConstants.SymbolName.RPAREN);

            Statement whileStatements = parseStatement(null);

            return new Block(new While(whileExpression, whileStatements));

        } else if (CommonConstants.getSymbolName(lexer.getCurrent().sym) == CommonConstants.SymbolName.PRINTLN.toString()) {

            eat(CommonConstants.SymbolName.PRINTLN);
            eat(CommonConstants.SymbolName.LPAREN);

            Expression printExpression = parseExpression();

            eat(CommonConstants.SymbolName.RPAREN);
            eat(CommonConstants.SymbolName.SEMICOLON);

            return new Block(new Print(printExpression));

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID) ||
                lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EQ)) {

            Identifier identifier;

            if (paramIdentifier == null) {

                identifier = new Identifier(lexer.getCurrent().value.toString());
                identifier.setLine(lexer.getCurrent().getLine());
                identifier.setColumn(lexer.getCurrent().getColumn());
                eat(CommonConstants.SymbolName.ID);

                if (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.LBRACKET.toString())) {

                    eat(CommonConstants.SymbolName.LBRACKET);
                    Expression indexExpression = parseExpression();
                    eat(CommonConstants.SymbolName.RBRACKET);
                    eat(CommonConstants.SymbolName.EQ);
                    Expression arrayExpression = parseExpression();
                    eat(CommonConstants.SymbolName.SEMICOLON);

                    return new Block(new ArrayAssign(identifier, indexExpression, arrayExpression));
                }

            } else {
                identifier = paramIdentifier;
            }

            //eat(CommonConstants.SymbolName.ID);

            eat(CommonConstants.SymbolName.EQ);
            Expression expression = parseExpression();
            eat(CommonConstants.SymbolName.SEMICOLON);

            return new Block(new Assign(identifier, expression));

        } else if (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.SIDEF.toString())) {

            eat(CommonConstants.SymbolName.SIDEF);
            eat(CommonConstants.SymbolName.LPAREN);

            Expression expression = parseExpression();

            eat(CommonConstants.SymbolName.RPAREN);
            eat(CommonConstants.SymbolName.SEMICOLON);

            return new Block(new Sidef(expression));

        }

        throw new ParseException(lexer.getCurrent().getLine(), lexer.getCurrent().getColumn(), "Cannot assign value to " + CommonConstants.getSymbolName(lexer.getCurrent().sym));

    }

    private VarDeclaration parseVarDeclaration() throws IOException, ParseException {

        Type type = parseType();

        if (type.typeName.name().equals("IDENTIFIER"))
            eat(CommonConstants.SymbolName.ID);

        Identifier identifier = parseIdentifier();
        eat(CommonConstants.SymbolName.ID);
        eat(CommonConstants.SymbolName.SEMICOLON);

        return new VarDeclaration(type, identifier);

    }

    private MethodDeclaration parseMethodDeclaration() throws ParseException, IOException {

        eat(CommonConstants.SymbolName.PUBLIC);
        Type methodType = parseType();
        if (methodType.typeName.name().equals("IDENTIFIER"))
            eat(CommonConstants.SymbolName.ID);
        Identifier methodName = parseIdentifier();
        eat(CommonConstants.SymbolName.ID);
        eat(CommonConstants.SymbolName.LPAREN);

        List<Map<Type, Identifier>> typeIdentifiers = new ArrayList<>();

        if (lexer.getCurrent().sym != CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.RPAREN)) {

            Type argumentType = parseType();
            if (argumentType.typeName.name().equals("IDENTIFIER"))
                eat(CommonConstants.SymbolName.ID);
            Identifier argument = parseIdentifier();
            eat(CommonConstants.SymbolName.ID);
            Map<Type, Identifier> typeIdentifier = new HashMap<>();
            typeIdentifier.put(argumentType, argument);
            typeIdentifiers.add(typeIdentifier);

            while (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.COMMA.toString())) {

                eat(CommonConstants.SymbolName.COMMA);
                argumentType = parseType();
                if (argumentType.typeName.name().equals("IDENTIFIER"))
                    eat(CommonConstants.SymbolName.ID);
                argument = parseIdentifier();
                eat(CommonConstants.SymbolName.ID);
                typeIdentifier = new HashMap<>();
                typeIdentifier.put(argumentType, argument);
                typeIdentifiers.add(typeIdentifier);

            }

        }

        eat(CommonConstants.SymbolName.RPAREN);
        eat(CommonConstants.SymbolName.LBRACE);

        List<VarDeclaration> varDeclarations = new ArrayList<>();
        List<Statement> statements = new ArrayList<>();

        while (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.INT) ||
                lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.BOOLEAN) ||
                lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.STRING))
            varDeclarations.add(parseVarDeclaration());

        while (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID)) {

            Type type = parseType();
            if (type.typeName.name().equals("IDENTIFIER"))
                eat(CommonConstants.SymbolName.ID);

            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EQ)) {
                statements.add(parseStatement(type.identifier));
                continue;
            }

            Identifier identifier1 = new Identifier(lexer.getCurrent().value.toString());
            varDeclarations.add(new VarDeclaration(type, identifier1));
            identifier1.setLine(lexer.getCurrent().getLine());
            identifier1.setColumn(lexer.getCurrent().getColumn());
            eat(CommonConstants.SymbolName.ID);
            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.SEMICOLON))
                eat(CommonConstants.SymbolName.SEMICOLON);

            while (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.INT) ||
                    lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.BOOLEAN) ||
                    lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.STRING))
                varDeclarations.add(parseVarDeclaration());

            Identifier identifier;

            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID)) {

                identifier = new Identifier(lexer.getCurrent().value.toString());
                eat(CommonConstants.SymbolName.ID);

                if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EQ)) {
                    statements.add(parseStatement(identifier));
                    continue;
                }

                eat(CommonConstants.SymbolName.SEMICOLON);

                varDeclarations.add(new VarDeclaration(type, identifier));

                while (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.INT) ||
                        lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.BOOLEAN) ||
                        lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.STRING))
                    varDeclarations.add(parseVarDeclaration());

            } else {

                Statement statement = parseStatement(identifier1);
                statements.add(statement);

                while (!CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.RETURN.toString()))
                    statements.add(parseStatement(null));

            }

        }


        while (!CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.RETURN.toString()))
            statements.add(parseStatement(null));


        eat(CommonConstants.SymbolName.RETURN);

        Expression returnExpression = parseExpression();

        eat(CommonConstants.SymbolName.SEMICOLON);

        eat(CommonConstants.SymbolName.RBRACE);

        return new MethodDeclaration(methodType, methodName, typeIdentifiers, varDeclarations, statements, returnExpression);

    }

    private Type parseType() throws IOException, ParseException {

        Type type;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.INT)) {

            lexer.next_token();

            if (CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.LBRACKET.toString())) {

                lexer.next_token();
                if (!CommonConstants.getSymbolName(lexer.getCurrent().sym).equals(CommonConstants.SymbolName.RBRACKET.toString()))
                    error(CommonConstants.SymbolName.RBRACKET);
                lexer.next_token();

                type = new Type(Type.TypeName.INT_ARRAY);
                return type;

            }

            type = new Type(Type.TypeName.INT);
            return type;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.BOOLEAN)) {

            type = new Type(Type.TypeName.BOOLEAN);
            lexer.next_token();
            return type;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.STRING)) {

            type = new Type(Type.TypeName.STRING);
            lexer.next_token();
            return type;

        }

        type = new Type(parseIdentifier());
        //eat(CommonConstants.SymbolName.ID);
        return type;

    }

    private Expression parseExpression() throws IOException, ParseException {

        T t = parseT();
        A a = parseA();

        return new Expression(t, a);

    }

    private A parseA() throws IOException, ParseException {

        A a = null;
        T t = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.OROR)) {

            lexer.next_token();
            t = parseT();
            a = parseA();
            return new A(t, a);

        }

        return null;

    }

    private T parseT() throws IOException, ParseException {

        F f = parseF();
        B b = parseB();
        return new T(f, b);

    }

    private B parseB() throws IOException, ParseException {

        F f = null;
        B b = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ANDAND)) {
            lexer.next_token();
            f = parseF();
            b = parseB();
            return new B(f, b);
        }

        return null;

    }

    private F parseF() throws IOException, ParseException {

        G g = parseG();
        C c = parseC();
        return new F(g, c);

    }

    private C parseC() throws IOException, ParseException {

        G g = null;
        C c = null;
        C.OperatorType o = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.EQEQ)) {

            lexer.next_token();
            o = C.OperatorType.EQUAL;
            g = parseG();
            c = parseC();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.LT)) {

            lexer.next_token();
            o = C.OperatorType.LESS_THAN;
            g = parseG();
            c = parseC();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.GT)) {

            lexer.next_token();
            o = C.OperatorType.GREATER_THAN;
            g = parseG();
            c = parseC();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.LTEQ)) {

            lexer.next_token();
            o = C.OperatorType.LESS_EQ_THAN;
            g = parseG();
            c = parseC();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.GTEQ)) {

            lexer.next_token();
            o = C.OperatorType.GREATER_EQ_THAN;
            g = parseG();
            c = parseC();

        }

        if (o != null)
            return new C(g, c, o);
        else
            return null;

    }

    private G parseG() throws IOException, ParseException {

        H h = parseH();
        D d = parseD();
        return new G(h, d);

    }

    private D parseD() throws IOException, ParseException {

        H h = null;
        D d = null;
        D.OperatorType o = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.PLUS)) {

            lexer.next_token();
            o = D.OperatorType.PLUS;
            h = parseH();
            d = parseD();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.MINUS)) {

            lexer.next_token();
            o = D.OperatorType.MINUS;
            h = parseH();
            d = parseD();

        }

        if (o != null)
            return new D(h, d, o);

        return null;

    }

    private H parseH() throws IOException, ParseException {

        NewQ q = parseNewQ();
        J j = parseJ();

        return new H(q, j);

    }

    private J parseJ() throws IOException, ParseException {

        NewQ q = null;
        J j = null;
        J.OperatorType o = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.TIMES)) {

            lexer.next_token();
            o = J.OperatorType.MULTIPLY;
            q = parseNewQ();
            j = parseJ();

        } else if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.DIV)) {

            lexer.next_token();
            o = J.OperatorType.DIVISION;
            q = parseNewQ();
            j = parseJ();

        }

        if (o == null)
            return null;
        return new J(q, j, o);

    }

    private NewQ parseNewQ() throws IOException, ParseException {

        NewQ q = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.INTLIT)) {

            Object obj = lexer.getCurrent().value;
            lexer.next_token();
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.INT_LIT, obj, y);

            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.STRING_LITERAL)) {

            Object obj = lexer.getCurrent().value;
            lexer.next_token();
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.STRING_LIT, obj, y);

            return q;

        }
        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.BOOLEAN_LITERAL)) {

            if (lexer.getCurrent().value.toString().equals("true")) {

                Object obj = lexer.getCurrent().value;
                lexer.next_token();
                Y y = parseY();
                q = new NewQ(NewQ.TYPE.TRUE, obj, y);

                return q;

            }

            Object obj = lexer.getCurrent().value;
            lexer.next_token();
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.FALSE, obj, y);

            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID)) {

            Identifier idt = parseIdentifier(); //new Q(Q.TYPE.IDENTIFIER, lexer.getCurrent().value);
            eat(CommonConstants.SymbolName.ID);
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.IDENTIFIER, idt, y);
            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.THIS)) {

            lexer.next_token();
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.THIS, y);
            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.NEW)) {

            lexer.next_token();

            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID)) {

                Identifier idt = parseIdentifier();
                eat(CommonConstants.SymbolName.ID);
                eat(CommonConstants.SymbolName.LPAREN);
                eat(CommonConstants.SymbolName.RPAREN);
                Y y = parseY();
                q = new NewQ(NewQ.TYPE.NEW_IDENTIFIER, idt, y);

            } else {

                eat(CommonConstants.SymbolName.INT);
                eat(CommonConstants.SymbolName.LBRACKET);
                Expression e = parseExpression();
                eat(CommonConstants.SymbolName.RBRACKET);
                Y y = parseY();
                q = new NewQ(NewQ.TYPE.NEW_INT_ARR, e, y);

            }

            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.NOT)) {

            lexer.next_token();
            Expression e = parseExpression();
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.NOT_EXP, e, y);
            return q;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.LPAREN)) {

            lexer.next_token();
            Expression e = parseExpression();
            eat(CommonConstants.SymbolName.RPAREN);
            Y y = parseY();
            q = new NewQ(NewQ.TYPE.LPR_EXP_RPR, e, y);
            return q;

        }

        throw new ParseException(lexer.getCurrent().getLine(), lexer.getCurrent().getColumn(), "Expected Q!");

    }

    private Y parseY() throws IOException, ParseException {

        Y y = null;

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.LBRACKET)) {

            lexer.next_token();
            Expression e = parseExpression();
            eat(CommonConstants.SymbolName.RBRACKET);
            Y y2 = parseY();
            y = new Y(Y.TYPE.LBR_EXP_RBR_Y, e, y2);
            return y;

        }

        if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.DOT)) {

            lexer.next_token();

            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.LENGTH)) {
                eat(CommonConstants.SymbolName.LENGTH);
                y = new Y(Y.TYPE.DOT_LENGTH_Y);
                return y;
            }

            if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.ID)) {

                Identifier ident = parseIdentifier();
                eat(CommonConstants.SymbolName.ID);

                eat(CommonConstants.SymbolName.LPAREN);
                List<Expression> expressionList = new ArrayList<>();

                while (lexer.getCurrent().sym != CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.RPAREN)) {
                    expressionList.add(parseExpression());
                    if (lexer.getCurrent().sym == CommonConstants.SymbolMapping.get(CommonConstants.SymbolName.COMMA))
                        lexer.next_token();
                }

                eat(CommonConstants.SymbolName.RPAREN);
                Y y2 = parseY();
                y = new Y(Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y, ident, expressionList, y2);

                return y;

            }

        }

        return null;

    }

    private void error(CommonConstants.SymbolName symbolName) throws ParseException {
        throw new ParseException(lexer.getCurrent().getLine(), lexer.getCurrent().getColumn(), "Invalid token : " + CommonConstants.getSymbolName(lexer.getCurrent().sym) +
                ", Expected token : " + symbolName);
    }

}
