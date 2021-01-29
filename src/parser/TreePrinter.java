package parser;

import analyser.NameAnalyzer;
import ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("ALL")
public class TreePrinter implements Visitor<String> {

    int level = 0;

    public TreePrinter() {
    }

    void incLevel() {
        level += 1;
    }

    void decLevel() {
        level -= 1;
    }

    public String printInc() {
/*        char[] arrayOfChar = new char[level];
        Arrays.fill(arrayOfChar, '\t');
        return new String(arrayOfChar)*/
        return "\t";
    }

    @Override
    public String visit(Program program) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(program.mainClass.accept(this));

        Iterator iterator = program.classList.iterator();
        while (iterator.hasNext()) {
            ClassDeclaration classDeclaration = (ClassDeclaration) iterator.next();
            stringBuilder.append(classDeclaration.accept(this));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(MainClass mainClass) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(MAIN-CLASS ");
        stringBuilder.append(mainClass.classIdentifier.accept(this));
        stringBuilder.append(")");
        stringBuilder.append("  ");
        stringBuilder.append("{");
        stringBuilder.append("\n\n");

        incLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("(MAIN-FUNCTION (STRING-ARRAY ");
        stringBuilder.append(mainClass.methodIdentifier.accept(this));
        stringBuilder.append(")");
        stringBuilder.append("  ");
        stringBuilder.append("{");
        stringBuilder.append("\n\n");

        incLevel();

        for (Statement statement : mainClass.statements) {
            stringBuilder.append(statement.accept(this));
            stringBuilder.append("\n");
        }

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("}\n\n");

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("}\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(ClassDeclaration classDeclaration) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(printInc());

        stringBuilder.append("\n(CLASS-DECL " + classDeclaration.classIdentifier.accept(this));

        if (classDeclaration.extendsIdentifier != null) {
            stringBuilder.append(" EXTENDS " + classDeclaration.extendsIdentifier.accept(this));
        }

        stringBuilder.append(")  {\n\n");

        incLevel();

        Iterator iterator = classDeclaration.varDeclarations.iterator();
        while (iterator.hasNext()) {
            VarDeclaration varDeclaration = (VarDeclaration) iterator.next();
            stringBuilder.append(varDeclaration.accept(this));
            stringBuilder.append("\n");
        }

        stringBuilder.append("\n");

        iterator = classDeclaration.methodDeclarations.iterator();
        while (iterator.hasNext()) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) iterator.next();
            stringBuilder.append(methodDeclaration.accept(this));
            stringBuilder.append("\n");
        }

        stringBuilder.append("\n}");

        return stringBuilder.toString();

    }

    @Override
    public String visit(VarDeclaration varDeclaration) {
        return printInc() + "(VAR-DECL " + varDeclaration.type.accept(this) + " " +
                varDeclaration.identifier.accept(this) + ")" + "\n";
    }

    @Override
    public String visit(MethodDeclaration methodDeclaration) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(printInc());
        stringBuilder.append("(MTD-DECL ");
        stringBuilder.append(methodDeclaration.methodType.accept(this));
        stringBuilder.append(" ");
        stringBuilder.append(methodDeclaration.methodName.accept(this));
        stringBuilder.append(" ");

        Iterator iterator = methodDeclaration.typeIdentifiers.iterator();

        HashMap map;

        if (methodDeclaration.typeIdentifiers.size() > 0) {

            stringBuilder.append("(TY-ID-LIST");

            while (iterator.hasNext()) {

                stringBuilder.append(" ");

                map = (HashMap) iterator.next();

                for (Object type : map.keySet()) {
                    stringBuilder.append("(");
                    stringBuilder.append(((Type) type).accept(this));
                    stringBuilder.append(" ");
                    stringBuilder.append(((Identifier) map.get(type)).accept(this));
                    stringBuilder.append(")");
                }

            }

            stringBuilder.append(")  {\n\n");

        } else
            stringBuilder.append("(No Parameters)  {\n\n");


        incLevel();
        stringBuilder.append(printInc());
        stringBuilder.append("(BLOCK\n\n");
        incLevel();

        iterator = methodDeclaration.varDeclarations.iterator();
        VarDeclaration varDeclaration;

        while (iterator.hasNext()) {
            varDeclaration = (VarDeclaration) iterator.next();
            stringBuilder.append((varDeclaration).accept(this));
            stringBuilder.append("\n");
        }

        iterator = methodDeclaration.statements.iterator();
        Statement statement;

        while (iterator.hasNext()) {

            statement = (Statement) iterator.next();
            stringBuilder.append((statement).accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append(printInc());
        incLevel();
        stringBuilder.append("(RETURN ");
        stringBuilder.append(methodDeclaration.returnExpression.accept(this));
        stringBuilder.append(")");

        decLevel();
        decLevel();

        stringBuilder.append("\n\n");

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("}\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Type type) {

        if (type.typeName.toString().equals(Type.TypeName.INT.toString()))
            return "INT";
        else if (type.typeName.toString().equals(Type.TypeName.STRING.toString()))
            return "STRING";
        else if (type.typeName.toString().equals(Type.TypeName.INT_ARRAY.toString()))
            return "INT-ARRAY";
        else if (type.typeName.toString().equals(Type.TypeName.BOOLEAN.toString()))
            return "BOOLEAN";
        else
            return "(ID " + type.identifier.accept(this) + ")";

    }

    @Override
    public String visit(Block block) {

        StringBuilder stringBuilder = new StringBuilder();

        if (block.body != null) {

            Iterator iterator = block.body.iterator();

            while (iterator.hasNext()) {

                Statement statement = (Statement) iterator.next();
                stringBuilder.append(statement.accept(this));
                stringBuilder.append("\n");

            }

        } else {
            stringBuilder.append(block.singleStatement.accept(this));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(IfThenElse ifThenElse) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(printInc() + "(IF " + ifThenElse.expr.accept(this));
        stringBuilder.append(")  {\n\n");

        incLevel();

        stringBuilder.append(ifThenElse.then.accept(this) + "\n");

        decLevel();
        stringBuilder.append(printInc() + "}\n\n");

        if (ifThenElse.elze != null) {

            String str = ifThenElse.elze.accept(this);

            if ((str != null) && (str.trim().length() > 0)) {

                stringBuilder.append(printInc() + "(ELSE)");
                stringBuilder.append("  {\n\n");
                incLevel();
                stringBuilder.append(str);
                stringBuilder.append("\n");

            }

        }

        decLevel();

        stringBuilder.append(printInc() + "}\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(While aWhile) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(printInc());
        stringBuilder.append("(WHILE ");
        stringBuilder.append(aWhile.expr.accept(this));
        stringBuilder.append("\n");

        incLevel();

        stringBuilder.append(aWhile.body.accept(this));

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append(")\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Print print) {

        return printInc() + "(PRINTLN (" + print.expression.accept(this) + "))";

    }

    @Override
    public String visit(Assign assign) {

        return printInc() + "(EQSIGN " + assign.identifier.accept(this) + " " + assign.expression.accept(this) + ")";

    }

    @Override
    public String visit(ArrayAssign arrayAssign) {

        return printInc() + "(EQSIGN (ARRAY-ASSIGN " + arrayAssign.identifier.accept(this) + arrayAssign.index.accept(this)
                + arrayAssign.value.accept(this);

    }

    @Override
    public String visit(Skip skip) {
        return "";
    }

    @Override
    public String visit(Sidef sidef) {

        return "(SIDEF " + sidef.expression.accept(this) + ")";

    }

    @Override
    public String visit(IntLiteral intLiteral) {

        return "(INTLIT " + intLiteral.value + ")";

    }

    @Override
    public String visit(StringLiteral stringLiteral) {

        return "(STRINGLIT " + stringLiteral.value + ")";

    }

    @Override
    public String visit(Identifier identifier) {

        return "(ID " + identifier.idName + ")";

    }

    @Override
    public String visit(Expression n) {

        if (n.t != null && n.a != null)
            return n.t.accept(this) + n.a.accept(this);
        else if (n.t != null)
            return n.t.accept(this);
        else if (n.a != null)
            return n.a.accept(this);
        else
            return null;

    }

    @Override
    public String visit(A a) {

        if (a.t != null && a.a != null)
            return " (|| " + a.t.accept(this) + " " + a.a.accept(this) + ")";
        else if (a.t != null)
            return " (|| " + a.t.accept(this) + ")";
        else if (a.a != null)
            return " (|| " + a.a.accept(this) + ")";
        else
            return "";

    }

    @Override
    public String visit(T t) {

        if (t.f != null && t.b != null)
            return t.f.accept(this) + t.b.accept(this);
        else if (t.f != null)
            return t.f.accept(this);
        else if (t.b != null)
            return t.b.accept(this);
        else
            return null;

    }

    @Override
    public String visit(B b) {

        if (b.f != null && b.b != null)
            return " (&& " + b.f.accept(this) + " " + b.b.accept(this) + ")";
        else if (b.f != null)
            return " (&& " + b.f.accept(this) + ")";
        else if (b.b != null)
            return " (&& " + b.b.accept(this) + ")";
        else
            return "";

    }

    @Override
    public String visit(F f) {

        if (f.g != null && f.c != null)
            return f.g.accept(this) + f.c.accept(this);
        else if (f.g != null)
            return f.g.accept(this);
        else if (f.c != null)
            return f.c.accept(this);
        else
            return null;

    }

    @Override
    public String visit(C c) {

        String opString = c.getO().equals(C.OperatorType.EQUAL) ? " EQUALS " : " < ";

        if (c.g != null && c.c != null) {
            return opString + c.g.accept(this) + " " + c.c.accept(this) + ")";
        } else if (c.g != null) {
            return opString + c.g.accept(this) + ")";
        } else if (c.c != null) {
            return opString + c.c.accept(this) + ")";
        } else {
            return "";
        }

    }

    @Override
    public String visit(G g) {

        if (g.h != null && g.d != null)
            return g.h.accept(this) + g.d.accept(this);
        else if (g.h != null)
            return g.h.accept(this);
        else if (g.d != null)
            return g.d.accept(this);
        else
            return null;

    }

    @Override
    public String visit(D d) {

        String opString = d.getO().equals(D.OperatorType.PLUS) ? " PLUS " : " MINUS ";

        if (d.h != null && d.d != null) {
            return opString + d.h.accept(this) + " " + d.d.accept(this) + ")";
        } else if (d.h != null) {
            return opString + d.h.accept(this) + ")";
        } else if (d.d != null) {
            return opString + d.d.accept(this) + ")";
        } else {
            return "";
        }

    }

    @Override
    public String visit(H h) {

        if (h.q != null && h.j != null)
            return h.q.accept(this) + h.j.accept(this);
        else if (h.q != null)
            return h.q.accept(this);
        else if (h.j != null)
            return h.j.accept(this);
        else
            return null;


    }

    @Override
    public String visit(J j) {

        String opString = j.getO().equals(J.OperatorType.MULTIPLY) ? " TIMES " : " DIV ";

        if (j.q != null && j.j != null) {
            return opString + j.q.accept(this) + " " + j.j.accept(this) + ")";
        } else if (j.q != null) {
            return opString + j.q.accept(this) + ")";
        } else if (j.j != null) {
            return opString + j.j.accept(this) + ")";
        } else {
            return "";
        }

    }

    @Override
    public String visit(NewQ newQ) {

        StringBuilder stringBuilder = new StringBuilder();

        NewQ.TYPE type = newQ.getType();

        if (type == NewQ.TYPE.THIS)
            stringBuilder.append("THIS");
        else if (type == NewQ.TYPE.NEW_IDENTIFIER)
            stringBuilder.append("NEW-INSTANCE ");
        else if (type == NewQ.TYPE.NEW_INT_ARR)
            stringBuilder.append("NEW-INT_ARRAY");
        else if (type == NewQ.TYPE.TRUE)
            stringBuilder.append("TRUE");
        else if (type == NewQ.TYPE.FALSE)
            stringBuilder.append("FALSE");
        else if (type == NewQ.TYPE.NOT_EXP)
            stringBuilder.append("(!");
        else if (type == NewQ.TYPE.INT_LIT)
            stringBuilder.append("INTLIT ");
        else if (type == NewQ.TYPE.STRING_LIT)
            stringBuilder.append("STRINGLIT ");
        else if (type == NewQ.TYPE.BOOL_LIT)
            stringBuilder.append("BOOLEANLIT ");
        else if (type == NewQ.TYPE.LPR_EXP_RPR)
            stringBuilder.append("(");

        if (newQ.content.size() > 0) {

            Iterator iterator = newQ.content.iterator();

            while (iterator.hasNext()) {

                Object o = iterator.next();

                if (o instanceof Identifier) {
                    Identifier identifier = (Identifier) o;
                    stringBuilder.append(identifier.accept(this));
                    //stringBuilder.append(")");
                } else if (o instanceof Y) {
                    Y y = (Y) o;
                    stringBuilder.append(y.accept(this));
                } else if (o instanceof Integer) {
                    stringBuilder.append(o.toString());
                } else if (o instanceof Expression) {
                    Expression expression = (Expression) o;
                    stringBuilder.append(expression.accept(this));
                    stringBuilder.append(")");
                } else if (o instanceof String) {
                    stringBuilder.append(o);
                } else {
                    stringBuilder.append("");
                }

            }

            //stringBuilder.append("\n");

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(Y y) {

        StringBuilder stringBuilder = new StringBuilder();

        if (y.getType() == Y.TYPE.DOT_LENGTH_Y)
            stringBuilder.append("DOT LENGTH");
        else if (y.getType() == Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y)
            stringBuilder.append(" DOT (FUN-CALL ");

        if (y.content.size() > 0) {

            Iterator iterator = y.content.iterator();

            while (iterator.hasNext()) {

                Object o = iterator.next();

                if (o == null) {
                    continue;
                }

                if (o instanceof Identifier) {
                    Identifier identifier = (Identifier) o;
                    stringBuilder.append(identifier.accept(this));
                    stringBuilder.append(")");
                }

                if (o instanceof ArrayList) {

                    for (int i = 0; i < ((ArrayList) o).size(); i++) {

                        Expression expression = (Expression) ((ArrayList) o).get(i);
                        stringBuilder.append("(");
                        stringBuilder.append(expression.accept(this));
                        stringBuilder.append(")");

                    }

                }

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(NameAnalyzer nameAnalyzer) {
        return nameAnalyzer.visit(nameAnalyzer).toString();
    }

}