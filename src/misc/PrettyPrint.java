package misc;

import analyser.NameAnalyzer;
import ast.*;

import java.util.*;

@SuppressWarnings("ALL")
public class PrettyPrint implements Visitor<String> {

    int level = 0;

    private NameAnalyzer nameAnalyzer;

    String current;

    public PrettyPrint(NameAnalyzer nameAnalyzer) {
        this.nameAnalyzer = nameAnalyzer;
    }

    void incLevel() {
        level += 1;
    }

    void decLevel() {
        level -= 1;
    }

    public String printInc() {
 /*       char[] arrayOfChar = new char[level];
        Arrays.fill(arrayOfChar, '\t');
  */      return "\t";//new String(arrayOfChar);
    }

    @Override
    public String visit(Program program) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(nameAnalyzer.program.mainClass.accept(this));

        Iterator iterator = nameAnalyzer.program.classList.iterator();
        while (iterator.hasNext()) {
            ClassDeclaration classDeclaration = (ClassDeclaration) iterator.next();
            stringBuilder.append(classDeclaration.accept(this));
            stringBuilder.append("\n");
            level = 0;
        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(MainClass mainClass) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("class ");
        current = "mainclass";
        stringBuilder.append(nameAnalyzer.program.mainClass.classIdentifier.accept(this));
        stringBuilder.append(" {\n\n");

        incLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("public static void main(String[] ");
        current = "mainmethod";
        stringBuilder.append(nameAnalyzer.program.mainClass.methodIdentifier.accept(this));
        stringBuilder.append(") {\n\n");

        incLevel();

        for (Statement statement : nameAnalyzer.program.mainClass.statements) {
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

        stringBuilder.append("\nclass ");
        stringBuilder.append(classDeclaration.classIdentifier.accept(this));

        if (classDeclaration.extendsIdentifier != null) {
            stringBuilder.append(" extends ");
            stringBuilder.append(classDeclaration.extendsIdentifier.accept(this));
        }

        stringBuilder.append(" {\n\n");

        incLevel();

        Iterator iterator = classDeclaration.varDeclarations.iterator();
        while (iterator.hasNext()) {
            VarDeclaration varDeclaration = (VarDeclaration) iterator.next();
            stringBuilder.append(varDeclaration.accept(this));
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
        return printInc() + varDeclaration.type.accept(this) + " " +
                varDeclaration.identifier.accept(this) + ";\n";
    }

    @Override
    public String visit(MethodDeclaration methodDeclaration) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(printInc());
        stringBuilder.append("public ");
        stringBuilder.append(methodDeclaration.methodType.accept(this));
        stringBuilder.append(" ");
        stringBuilder.append(methodDeclaration.methodName.accept(this));

        Iterator iterator = methodDeclaration.typeIdentifiers.iterator();

        HashMap map;
        boolean moreArgs = false;
        int i = 0;

        if (methodDeclaration.typeIdentifiers.size() > 0) {

            if (methodDeclaration.typeIdentifiers.size() > 1)
                moreArgs = true;

            stringBuilder.append("(");

            while (iterator.hasNext()) {

                map = (HashMap) iterator.next();

                for (Object type : map.keySet()) {

                    stringBuilder.append(((Type) type).accept(this));
                    stringBuilder.append(" ");
                    stringBuilder.append(((Identifier) map.get(type)).accept(this));
                    if (moreArgs && i < methodDeclaration.typeIdentifiers.size() - 1)
                        stringBuilder.append(", ");

                }

                i++;

            }

            moreArgs = false;
            stringBuilder.append(") {\n\n");

        } else
            stringBuilder.append("() {\n\n");


        incLevel();

        if (methodDeclaration.varDeclarations != null)
            for (VarDeclaration varDeclaration : methodDeclaration.varDeclarations) {
                stringBuilder.append(varDeclaration.accept(this));
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
        stringBuilder.append("return ");
        stringBuilder.append(methodDeclaration.returnExpression.accept(this));
        stringBuilder.append(";");

        incLevel();

        decLevel();
        decLevel();

        //stringBuilder.append("\n");

        //stringBuilder.append(printInc());
        stringBuilder.append("\n\n");

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("}\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Type type) {

        if (type.typeName.toString().equals(Type.TypeName.INT.toString()))
            return "int";
        else if (type.typeName.toString().equals(Type.TypeName.STRING.toString()))
            return "String";
        else if (type.typeName.toString().equals(Type.TypeName.INT_ARRAY.toString()))
            return "int[]";
        else if (type.typeName.toString().equals(Type.TypeName.BOOLEAN.toString()))
            return "boolean";
        else
            return type.identifier.accept(this);

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

        stringBuilder.append(printInc() + "if (");
        stringBuilder.append(ifThenElse.expr.accept(this));
        stringBuilder.append(")");
        stringBuilder.append(" {\n\n");

        incLevel();

        stringBuilder.append(ifThenElse.then.accept(this) + "\n");

        decLevel();
        stringBuilder.append(printInc() + "}\n\n");

        if (ifThenElse.elze != null) {

            String str = ifThenElse.elze.accept(this);

            if ((str != null) && (str.trim().length() > 0)) {

                stringBuilder.append(printInc());
                stringBuilder.append("else {\n\n");
                incLevel();
                decLevel();
                decLevel();
                stringBuilder.append(printInc());
                stringBuilder.append(str);
                incLevel();
                incLevel();
                stringBuilder.append("\n");

            }

        }

        decLevel();

        stringBuilder.append(printInc() + "}");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Skip skip) {
        return "";
    }

    @Override
    public String visit(While aWhile) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(printInc());
        stringBuilder.append("while (");
        stringBuilder.append(aWhile.expr.accept(this));
        stringBuilder.append(") {\n\n");

        incLevel();

        stringBuilder.append(aWhile.body.accept(this));

        decLevel();

        stringBuilder.append(printInc());
        stringBuilder.append("}\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Print print) {
        return printInc() + "System.out.println(" + print.expression.accept(this) + ");";
    }

    @Override
    public String visit(Assign assign) {
        return printInc() + assign.identifier.accept(this) + " = " + assign.expression.accept(this) + ";";
    }

    @Override
    public String visit(ArrayAssign arrayAssign) {
        return printInc() + arrayAssign.identifier.accept(this) + "["
                + arrayAssign.index.accept(this) + "] = " + arrayAssign.value.accept(this);
    }

    @Override
    public String visit(Sidef sidef) {
        return "sidef (" + sidef.expression.accept(this) + ");";
    }

    @Override
    public String visit(IntLiteral intLiteral) {
        return String.valueOf(intLiteral.value);
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        return stringLiteral.value;
    }

    @Override
    public String visit(Identifier identifier) {
        return identifier.getSymbol() != null ? identifier.getSymbol().getUniqueName() : "_#error#_";
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
            return a.t.accept(this) + " || " + a.a.accept(this);
        else if (a.t != null)
            return " || " + a.t.accept(this);
        else if (a.a != null)
            return a.a.accept(this) + " || ";
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
            return b.f.accept(this) + " && " + b.b.accept(this);
        else if (b.f != null)
            return " && " + b.f.accept(this);
        else if (b.b != null)
            return b.b.accept(this) + "&& ";
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

        String opString = c.getO().equals(C.OperatorType.EQUAL) ? " == " : " < ";

        if (c.g != null && c.c != null) {
            return c.g.accept(this) + opString + c.c.accept(this);
        } else if (c.g != null) {
            return opString + c.g.accept(this);
        } else if (c.c != null) {
            return c.c.accept(this) + opString;
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

        String opString = d.getO().equals(D.OperatorType.PLUS) ? " + " : " - ";

        if (d.h != null && d.d != null) {
            return d.h.accept(this) + opString + d.d.accept(this);
        } else if (d.h != null) {
            return opString + d.h.accept(this);
        } else if (d.d != null) {
            return d.d.accept(this) + opString;
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

        String opString = j.getO().equals(J.OperatorType.MULTIPLY) ? " * " : " / ";

        if (j.q != null && j.j != null) {
            return j.q.accept(this) + opString + j.j.accept(this);
        } else if (j.q != null) {
            return opString + j.q.accept(this);
        } else if (j.j != null) {
            return j.j.accept(this) + opString;
        } else {
            return "";
        }

    }

    @Override
    public String visit(NewQ newQ) {

        int lprpCounter = 0;
        StringBuilder stringBuilder = new StringBuilder();

        NewQ.TYPE type = newQ.getType();

        if (type == NewQ.TYPE.THIS)
            stringBuilder.append("this");
        else if (type == NewQ.TYPE.NEW_IDENTIFIER)
            stringBuilder.append("new ");
        else if (type == NewQ.TYPE.NEW_INT_ARR)
            stringBuilder.append("new int[]");
        else if (type == NewQ.TYPE.TRUE)
            stringBuilder.append("true");
        else if (type == NewQ.TYPE.FALSE)
            stringBuilder.append("false");
        else if (type == NewQ.TYPE.NOT_EXP)
            stringBuilder.append("!");
        /*else if (type == NewQ.TYPE.INT_LIT)
            stringBuilder.append(newQ.);
        else if (type == NewQ.TYPE.STRING_LIT)
            stringBuilder.append("String ");
        else if (type == NewQ.TYPE.BOOL_LIT)
            stringBuilder.append("boolean ");*/
        else if (type == NewQ.TYPE.LPR_EXP_RPR) {
            stringBuilder.append("(");
            lprpCounter++;
        }

        if (newQ.content.size() > 0) {

            Iterator iterator = newQ.content.iterator();

            while (iterator.hasNext()) {

                Object o = iterator.next();
                if (o instanceof Identifier) {
                    Identifier identifier = (Identifier) o;
                    stringBuilder.append(identifier.accept(this));
                    if (type == NewQ.TYPE.NEW_IDENTIFIER)
                        stringBuilder.append("()");
                } else if (o instanceof Y) {
                    Y y = (Y) o;
                    stringBuilder.append(y.accept(this));
                } else if (o instanceof Integer) {
                    stringBuilder.append(o.toString());
                } else if (o instanceof Expression) {
                    Expression expression = (Expression) o;
                    stringBuilder.append(expression.accept(this));
                    for (int i = 0; i < lprpCounter; i++)
                        stringBuilder.append(")");
                } else if (o instanceof String) {
                    stringBuilder.append("\"");
                    stringBuilder.append(o);
                    stringBuilder.append("\"");
                } else {
                    stringBuilder.append("");
                }

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(Y y) {

        StringBuilder stringBuilder = new StringBuilder();

        Y.TYPE type = y.getType();

        if (type == Y.TYPE.DOT_LENGTH_Y)
            stringBuilder.append(".length()");
        else if (type == Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y)
            stringBuilder.append(".");

        if (y.content.size() > 0) {

            Iterator iterator = y.content.iterator();

            while (iterator.hasNext()) {

                Object o = iterator.next();

                if (o == null) {
                    continue;
                } else if (o instanceof Identifier) {

                    Identifier identifier = (Identifier) o;

                    Tree object = identifier.getParent();

                    while (!((object instanceof Program) || (object instanceof ClassDeclaration)))
                        object = object.getParent();

                    if (object instanceof Program) {

                        Program program = (Program) object;

                        List<ClassDeclaration> classDeclarations = program.classList;

                        for (ClassDeclaration classDeclaration : classDeclarations) {

                            if (classDeclaration.extendsIdentifier == null) {

                                List<MethodDeclaration> methodDeclarations = classDeclaration.methodDeclarations;

                                for (MethodDeclaration methodDeclaration : methodDeclarations) {

                                    if (methodDeclaration.methodName.idName.equals(identifier.idName)) {

                                        stringBuilder.append(identifier.accept(this));
                                        stringBuilder.append("(");
                                        break;

                                    }

                                }

                            } else {

                                ClassDeclaration extendsClassDeclaration = null;

                                for (ClassDeclaration declaration : classDeclarations) {
                                    if (declaration.classIdentifier.idName.equals(classDeclaration.extendsIdentifier.idName)) {
                                        extendsClassDeclaration = declaration;
                                        break;
                                    }
                                }

                                List<MethodDeclaration> methodDeclarations = extendsClassDeclaration.methodDeclarations;

                                for (MethodDeclaration methodDeclaration : methodDeclarations) {

                                    if (methodDeclaration.methodName.idName.equals(identifier.idName)) {

                                        stringBuilder.append(methodDeclaration.methodName.idName + "_#error#_()");
                                        return stringBuilder.toString();

                                    }

                                }

                            }

                        }

                    } else if (object instanceof ClassDeclaration) {

                        ClassDeclaration extendsClassDeclaration = null;

                        ClassDeclaration classDeclaration = (ClassDeclaration) object;

                        if (classDeclaration.extendsIdentifier == null) {

                            List<MethodDeclaration> methodDeclarations = classDeclaration.methodDeclarations;

                            for (MethodDeclaration methodDeclaration : methodDeclarations) {

                                if (methodDeclaration.methodName.idName.equals(identifier.idName)) {

                                    stringBuilder.append(identifier.accept(this));
                                    stringBuilder.append("(");

                                }

                            }

                        } else {

                            Program program = (Program) classDeclaration.getParent();

                            List<ClassDeclaration> classDeclarations = program.classList;

                            for (ClassDeclaration declaration : classDeclarations) {
                                if (declaration.classIdentifier.idName.equals(classDeclaration.extendsIdentifier.idName)) {
                                    extendsClassDeclaration = declaration;
                                    break;
                                }
                            }

                            List<MethodDeclaration> methodDeclarations = extendsClassDeclaration.methodDeclarations;

                            for (MethodDeclaration methodDeclaration : methodDeclarations) {

                                if (methodDeclaration.methodName.idName.equals(identifier.idName)) {

                                    stringBuilder.append(methodDeclaration.methodName.idName + "_#error#_()");
                                    return stringBuilder.toString();

                                }

                            }

                        }

                    }

                } else if (o instanceof ArrayList) {

                    int i = ((ArrayList) o).size();

                    for (Object e : (ArrayList) o) {
                        Expression expression = (Expression) e;
                        stringBuilder.append(expression.accept(this));
                        if (i > 1 && i != ((ArrayList) o).size() - 1) {
                            stringBuilder.append(", ");
                            i--;
                        }
                    }

                }

            }

            if (type == Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y)
                stringBuilder.append(")");

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(NameAnalyzer nameAnalyzer) {
        return nameAnalyzer.program.accept(this);
    }

}
