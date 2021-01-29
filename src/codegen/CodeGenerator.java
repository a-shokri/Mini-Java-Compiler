package codegen;

import analyser.ClassSymbol;
import analyser.MethodSymbol;
import analyser.NameAnalyzer;
import analyser.VarSymbol;
import ast.*;
import jasmin.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CodeGenerator implements Visitor<String> {

    private int labels = 0;
    private int slotNumber = 0;
    private ClassSymbol currentClassSymbol;
    private MethodSymbol currentMethodSymbol;
    private static final String LABEL = "Label";
    private boolean wantType = true;
    private int instructionCount = 0;
    private int localVariable = 0;
    private String filePath;
    private NameAnalyzer nameAnalyzer;

    public CodeGenerator(NameAnalyzer nameAnalyzer, String filePath) {

        this.nameAnalyzer = nameAnalyzer;
        this.filePath = filePath;

    }

    @Override
    public String visit(Program n) {

        String mainClass = n.mainClass.accept(this);
        File mainClassFile = generateJasminFile(n.mainClass.classIdentifier.idName, mainClass);
        generateClassFile(mainClassFile);

        for (ClassDeclaration classDeclaration : n.classList) {

            String otherClasses = classDeclaration.accept(this);
            File otherClassFile = generateJasminFile(classDeclaration.classIdentifier.idName, otherClasses);
            generateClassFile(otherClassFile);

        }

        return "";

    }

    @Override
    public String visit(MainClass mainClass) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(".class public ");
        stringBuilder.append(mainClass.classIdentifier.idName);
        stringBuilder.append("\n");
        stringBuilder.append(".super java/lang/Object\n");
        stringBuilder.append(".method public <init>()V\n");
        stringBuilder.append("aload_0\n");
        stringBuilder.append("invokenonvirtual java/lang/Object/<init>()V\n");
        stringBuilder.append("return\n");
        stringBuilder.append(".end method\n");
        stringBuilder.append(".method public static main([Ljava/lang/String;)V\n");
        stringBuilder.append(".limit locals 10\n");
        stringBuilder.append(".limit stack 10\n");

        for (Statement statement : mainClass.statements) {

            stringBuilder.append(statement.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append("return\n");
        stringBuilder.append(".end method\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(ClassDeclaration classDeclaration) {

        labels = 0;

        currentClassSymbol = (ClassSymbol) classDeclaration.getSymbol();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(".class public ");
        stringBuilder.append(classDeclaration.classIdentifier.idName);
        stringBuilder.append("\n");

        if (classDeclaration.extendsIdentifier != null) {

            stringBuilder.append(".super ");
            stringBuilder.append(classDeclaration.extendsIdentifier.idName);
            stringBuilder.append("\n\n");

        } else
            stringBuilder.append(".super java/lang/Object\n\n");


        for (VarDeclaration varDeclaration : classDeclaration.varDeclarations) {

            stringBuilder.append(varDeclaration.accept(this));
            stringBuilder.append("\n");

        }

        if (classDeclaration.extendsIdentifier != null) {

            stringBuilder.append(".method public <init>()V\n");
            stringBuilder.append("aload_0\n");
            stringBuilder.append("invokenonvirtual ");
            stringBuilder.append(classDeclaration.extendsIdentifier.idName);
            stringBuilder.append("/<init>()V\n");
            stringBuilder.append("return\n");
            stringBuilder.append(".end method\n");

        } else {

            stringBuilder.append(".method public <init>()V\n");
            stringBuilder.append("aload_0\n");
            stringBuilder.append("invokenonvirtual java/lang/Object/<init>()V\n");
            stringBuilder.append("return\n");
            stringBuilder.append(".end method\n");

        }

        for (MethodDeclaration methodDeclaration : classDeclaration.methodDeclarations) {

            stringBuilder.append(methodDeclaration.accept(this));
            stringBuilder.append("\n");

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(VarDeclaration varDeclaration) {

        if (currentMethodSymbol != null) {

            setVariableSlot(varDeclaration.identifier);
            return "";

        }

        return ".field public " + varDeclaration.identifier.idName + " " + varDeclaration.type.accept(this);

    }

    @Override
    public String visit(MethodDeclaration methodDeclaration) {

        slotNumber = 0;

        StringBuilder stringBuilder = new StringBuilder();

        currentMethodSymbol = (MethodSymbol) methodDeclaration.getSymbol();

        stringBuilder.append(".method public ");
        stringBuilder.append(methodDeclaration.methodName.idName);
        stringBuilder.append("(");

        /*Iterator iterator = methodDeclaration.typeIdentifiers.iterator();
        HashMap map;

        if (methodDeclaration.typeIdentifiers.index() > 0) {

            while (iterator.hasNext()) {

                map = (HashMap) iterator.next();

                for (Object type : map.keySet())
                    stringBuilder.append(((Type) type).accept(this));

            }

        }*/

        Iterator iterator = currentMethodSymbol.getMethodArguments().iterator();

        while (iterator.hasNext()) {

            VarSymbol varSymbol = (VarSymbol) iterator.next();

            varSymbol.setIndex(++slotNumber);

            stringBuilder.append(varSymbol.getType().accept(this));

        }

        stringBuilder.append(")");
        stringBuilder.append(methodDeclaration.methodType.accept(this));
        stringBuilder.append("\n");
        stringBuilder.append(".limit locals 50\n");
        stringBuilder.append(".limit stack 100\n");

        iterator = methodDeclaration.varDeclarations.iterator();
        VarDeclaration varDeclaration;

        while (iterator.hasNext()) {
            varDeclaration = (VarDeclaration) iterator.next();
            stringBuilder.append(varDeclaration.accept(this));
        }

        iterator = methodDeclaration.varDeclarations.iterator();
        Statement statement;

        while (iterator.hasNext()) {
            statement = (Statement) iterator.next();
            stringBuilder.append(statement.accept(this));
        }

        stringBuilder.append(methodDeclaration.returnExpression.accept(this));
        stringBuilder.append("\n");

        if (methodDeclaration.methodType.typeName.name().equals("INT") ||
                methodDeclaration.methodType.typeName.name().equals("BOOLEAN"))
            stringBuilder.append("ireturn\n");
        else
            stringBuilder.append("areturn\n");

        stringBuilder.append(".end method\n");

        currentMethodSymbol = null;

        return stringBuilder.toString();

    }

    @Override
    public String visit(Type type) {

        if (type.typeName.toString().equals(Type.TypeName.INT.toString()))
            return "I";
        else if (type.typeName.toString().equals(Type.TypeName.STRING.toString()))
            return "Ljava/lang/String";
        else if (type.typeName.toString().equals(Type.TypeName.INT_ARRAY.toString()))
            return "[I";
        else if (type.typeName.toString().equals(Type.TypeName.BOOLEAN.toString()))
            return "Z";
        else
            return "L" + type.identifier.accept(this) + ";";

    }

    @Override
    public String visit(Block block) {

        StringBuilder stringBuilder = new StringBuilder();

        if (block.body != null) {

            Iterator iterator = block.body.iterator();

            Statement statement;

            while ((iterator.hasNext())) {

                statement = (Statement) iterator.next();
                stringBuilder.append(statement.accept(this));
                stringBuilder.append("\n");

            }

        } else {

            if (block.singleStatement != null) {

                stringBuilder.append(block.singleStatement.accept(this));
                stringBuilder.append("\n");

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(IfThenElse ifThenElse) {

        StringBuilder stringBuilder = new StringBuilder();

        String label1 = generateLabel();
        String label2 = generateLabel();

        stringBuilder.append(ifThenElse.expr.accept(this));
        stringBuilder.append("ifeq ");
        stringBuilder.append(label1);
        stringBuilder.append("\n");


        if (ifThenElse.then != null) {

            stringBuilder.append(ifThenElse.then.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append("goto ");
        stringBuilder.append(label2);
        stringBuilder.append("\n");

        stringBuilder.append(label1);
        stringBuilder.append(":\n");

        if (ifThenElse.elze != null) {

            stringBuilder.append(ifThenElse.elze.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append(label2);
        stringBuilder.append(":\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(While aWhile) {

        StringBuilder stringBuilder = new StringBuilder();

        String label1 = generateLabel();
        String label2 = generateLabel();

        stringBuilder.append("goto ");
        stringBuilder.append(label1);
        stringBuilder.append("\n");

        stringBuilder.append(label2);
        stringBuilder.append(":\n");

        if (aWhile.body != null) {

            stringBuilder.append(aWhile.body.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append(label1);
        stringBuilder.append(":\n");

        stringBuilder.append(aWhile.expr.accept(this));
        stringBuilder.append("\n");

        stringBuilder.append("ifne ");
        stringBuilder.append(label2);
        stringBuilder.append("\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Print print) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
        stringBuilder.append(print.expression.accept(this));
        stringBuilder.append("\n");
        stringBuilder.append("invokevirtual java/io/PrintStream/println(");
        stringBuilder.append(print.getNodeType().accept(this));
        stringBuilder.append(")V\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Assign assign) {

        StringBuilder stringBuilder = new StringBuilder();

        int index = getVariableSlot(assign.identifier);

        if (index == -1) {

            stringBuilder.append("aload_0\n");
            stringBuilder.append(assign.expression.accept(this));
            stringBuilder.append("\n");

            String string = "putfield " + currentClassSymbol.getClassDeclaration().classIdentifier.idName + "/" +
                    assign.identifier.idName + " " + assign.getNodeType().accept(this) + "\n";

            stringBuilder.append(string);

        } else {

            stringBuilder.append(assign.expression.accept(this));
            stringBuilder.append("\n");

            Type type = assign.expression.getNodeType();

            if (type.typeName == Type.TypeName.INT || type.typeName == Type.TypeName.BOOLEAN) {

                stringBuilder.append("istore ");
                stringBuilder.append(index);
                stringBuilder.append("\n");

            } else {

                stringBuilder.append("astore ");
                stringBuilder.append(index);
                stringBuilder.append("\n");

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(ArrayAssign arrayAssign) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(arrayAssign.identifier.accept(this));
        stringBuilder.append(arrayAssign.index.accept(this));
        stringBuilder.append(arrayAssign.value.accept(this));

        stringBuilder.append("iastore\n");

        return stringBuilder.toString();

    }

    @Override
    public String visit(Skip skip) {
        return "";
    }

    @Override
    public String visit(Sidef sidef) {
        return sidef.expression.accept(this) + "pop";
    }

    @Override
    public String visit(IntLiteral intLiteral) {
        return "ldc " + intLiteral.value + "\n";
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        return "ldc \"" + stringLiteral.value + "\"\n";
    }

    @Override
    public String visit(Identifier identifier) {

        StringBuilder stringBuilder = new StringBuilder();

        int index = getVariableSlot(identifier);

        if (index == -1) {

            stringBuilder.append("aload_0\n");

            String string = "getfield " + currentClassSymbol.getClassDeclaration().classIdentifier.idName + "/" +
                    identifier.idName + " " + identifier.getNodeType().accept(this) + "\n";

            stringBuilder.append(string);

        } else {

            Type type = identifier.getNodeType();

            if (type.typeName == Type.TypeName.INT || type.typeName == Type.TypeName.BOOLEAN) {

                stringBuilder.append("iload ");
                stringBuilder.append(index);
                stringBuilder.append("\n");

            } else {

                stringBuilder.append("aload ");
                stringBuilder.append(index);
                stringBuilder.append("\n");

            }

        }

        return stringBuilder.toString();

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
            return "";

    }

    @Override
    public String visit(A a) {

        StringBuilder stringBuilder = new StringBuilder();

        String label1 = generateLabel();
        String label2 = generateLabel();

        if (a.t != null) {

            stringBuilder.append(a.t.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append("ifeq ");
        stringBuilder.append(label1);
        stringBuilder.append("\n");

        stringBuilder.append("iconst_1\n");

        stringBuilder.append("goto ");
        stringBuilder.append(label2);
        stringBuilder.append("\n");

        stringBuilder.append(label1);
        stringBuilder.append(":\n");

        if (a.a != null) {

            stringBuilder.append(a.a.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append(label2);
        stringBuilder.append(":\n");

        return stringBuilder.toString();

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
            return "";

    }

    @Override
    public String visit(B b) {

        StringBuilder stringBuilder = new StringBuilder();

        String label1 = generateLabel();
        String label2 = generateLabel();

        if (b.f != null) {

            stringBuilder.append(b.f.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append("ifeq ");
        stringBuilder.append(label1);
        stringBuilder.append("\n");

        if (b.b != null) {

            stringBuilder.append(b.b.accept(this));
            stringBuilder.append("\n");

        }

        stringBuilder.append("goto ");
        stringBuilder.append(label2);
        stringBuilder.append("\n");

        stringBuilder.append(label1);
        stringBuilder.append(":\n");

        stringBuilder.append("iconst_0\n");

        stringBuilder.append(label2);
        stringBuilder.append(":\n");

        return stringBuilder.toString();

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
            return "";

    }

    @Override
    public String visit(C c) {

        StringBuilder stringBuilder = new StringBuilder();

        String opString = c.getO().equals(C.OperatorType.EQUAL) ? "EQ" : "LT";

        if (opString.equals("LT")) {

            String label1 = generateLabel();
            String label2 = generateLabel();

            if (c.g != null) {

                stringBuilder.append(c.g.accept(this));
                stringBuilder.append("\n");

            }

            if (c.c != null) {

                stringBuilder.append(c.c.accept(this));
                stringBuilder.append("\n");
            }

            stringBuilder.append("if_icmplt ");
            stringBuilder.append(label1);
            stringBuilder.append("\n");

            stringBuilder.append("iconst_0\n");

            stringBuilder.append("goto ");
            stringBuilder.append(label2);
            stringBuilder.append("\n");

            stringBuilder.append(label1);
            stringBuilder.append(":\n");

            stringBuilder.append("iconst_1\n");

            stringBuilder.append(label2);
            stringBuilder.append(":\n");

        } else {

            Type type = c.getNodeType();

            String label1 = generateLabel();
            String label2 = generateLabel();

            if (c.g != null) {

                stringBuilder.append(c.g.accept(this));
                stringBuilder.append("\n");

            }

            if (c.c != null) {

                stringBuilder.append(c.c.accept(this));
                stringBuilder.append("\n");
            }

            if (type.typeName == Type.TypeName.INT || type.typeName == Type.TypeName.BOOLEAN) {

                stringBuilder.append("if_icmpeq ");
                stringBuilder.append(label1);
                stringBuilder.append("\n");

                stringBuilder.append("iconst_0\n");

                stringBuilder.append("goto ");
                stringBuilder.append(label2);
                stringBuilder.append("\n");

                stringBuilder.append(label1);
                stringBuilder.append(":\n");

                stringBuilder.append("iconst_1\n");

                stringBuilder.append(label2);
                stringBuilder.append(":\n");

            } else if (type.typeName == Type.TypeName.INT_ARRAY || type.typeName == Type.TypeName.STRING ||
                    type.typeName == Type.TypeName.IDENTIFIER) {

                stringBuilder.append("if_acmpeq ");
                stringBuilder.append(label1);
                stringBuilder.append("\n");

                stringBuilder.append("iconst_0\n");

                stringBuilder.append("goto ");
                stringBuilder.append(label2);
                stringBuilder.append("\n");

                stringBuilder.append(label1);
                stringBuilder.append(":\n");

                stringBuilder.append("iconst_1\n");

                stringBuilder.append(label2);
                stringBuilder.append(":\n");

            }


        }

        return stringBuilder.toString();

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
            return "";

    }

    @Override
    public String visit(D d) {

        StringBuilder stringBuilder = new StringBuilder();

        String opString = d.getO().equals(D.OperatorType.PLUS) ? "P" : "M";

        if (opString.equals("M")) {

            if (d.h != null) {

                stringBuilder.append(d.h.accept(this));
                stringBuilder.append("\n");

            }

            if (d.d != null) {

                stringBuilder.append(d.d.accept(this));
                stringBuilder.append("\n");

            }

            stringBuilder.append("isub\n");

        } else {

            String type = d.h != null ? d.h.q.type.name() : d.d.h.q.type.name();

            if (type.equals("INT_LIT")) {

                if (d.h != null) {

                    stringBuilder.append(d.h.accept(this));
                    stringBuilder.append("\n");

                }

                if (d.d != null) {

                    stringBuilder.append(d.d.accept(this));
                    stringBuilder.append("\n");

                }

                stringBuilder.append("iadd\n");

            } else {

                stringBuilder.append("new java/lang/StringBuilder\n");
                stringBuilder.append("dup\n");
                stringBuilder.append("invokespecial java/lang/StringBuilder/<init>()V\n");

                if (d.h != null) {

                    stringBuilder.append(d.h.accept(this));
                    stringBuilder.append("\n");
                    stringBuilder.append("invokevirtual java/lang/StringBuilder/append(");
                    stringBuilder.append(d.h.q.type.name());
                    stringBuilder.append(")Ljava/lang/StringBuilder;\n");

                }

                if (d.d != null) {

                    stringBuilder.append(d.d.accept(this));
                    stringBuilder.append("\n");
                    stringBuilder.append("invokevirtual java/lang/StringBuilder/append(");
                    stringBuilder.append(d.d.h.q.type.name());
                    stringBuilder.append(")Ljava/lang/StringBuilder;\n");

                }

                stringBuilder.append("invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;");

            }

        }

        return stringBuilder.toString();

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
            return "";

    }


    @Override
    public String visit(J j) {

        StringBuilder stringBuilder = new StringBuilder();

        String opString = j.getO().equals(J.OperatorType.MULTIPLY) ? "M" : "D";

        if (j.q != null) {

            stringBuilder.append(j.q.accept(this));
            stringBuilder.append("\n");

        }

        if (j.j != null) {

            stringBuilder.append(j.j.accept(this));
            stringBuilder.append("\n");

        }


        if (opString.equals("M"))
            stringBuilder.append("imul\n");
        else
            stringBuilder.append("idiv\n");


        return stringBuilder.toString();

    }

    @Override
    public String visit(NewQ newQ) {

        StringBuilder stringBuilder = new StringBuilder();

        NewQ.TYPE type = newQ.type;

        if (type == NewQ.TYPE.THIS)
            stringBuilder.append("aload_0");
        else if (type == NewQ.TYPE.NEW_IDENTIFIER) {

            stringBuilder.append("new ");

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object o = iterator.next();

                    if (o instanceof Identifier) {

                        stringBuilder.append(((Identifier) o).accept(this));
                        stringBuilder.append("\n");
                        stringBuilder.append("dup\n");
                        stringBuilder.append("invokespecial ");
                        stringBuilder.append(((Identifier) o).accept(this));
                        stringBuilder.append("/<init>()V\n");

                    }

                }

            }

        } else if (type == NewQ.TYPE.NEW_INT_ARR) {

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object o = iterator.next();

                    if (o instanceof Expression) {

                        stringBuilder.append(((Expression) o).accept(this));
                        stringBuilder.append("\n");

                    }

                }

            }

            stringBuilder.append("newarray int\n");

        } else if (type == NewQ.TYPE.TRUE) {

            stringBuilder.append("iconst_1");

        } else if (type == NewQ.TYPE.FALSE) {

            stringBuilder.append("iconst_0");

        } else if (type == NewQ.TYPE.NOT_EXP) {

            String label1 = generateLabel();
            String label2 = generateLabel();

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object object = iterator.next();

                    if (object instanceof Expression) {

                        stringBuilder.append(((Expression) object).accept(this));
                        stringBuilder.append("\n");

                        stringBuilder.append("ifeq ");
                        stringBuilder.append(label1);
                        stringBuilder.append("\n");

                        stringBuilder.append("iconst_0\n");

                        stringBuilder.append("goto ");
                        stringBuilder.append(label2);
                        stringBuilder.append("\n");

                        stringBuilder.append(label1);
                        stringBuilder.append(":\n");

                        stringBuilder.append("iconst_1\n");

                        stringBuilder.append(label2);
                        stringBuilder.append(":\n");

                    }


                }

            }

        } else if (type == NewQ.TYPE.INT_LIT) {

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object object = iterator.next();

                    if (object instanceof Integer) {

                        stringBuilder.append("ldc");
                        stringBuilder.append(object);
                        stringBuilder.append("\n");

                    }

                }

            }

        } else if (type == NewQ.TYPE.STRING_LIT) {

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object object = iterator.next();

                    if (object instanceof String) {

                        stringBuilder.append("ldc \"");
                        stringBuilder.append(object);
                        stringBuilder.append("\"\n");

                    }

                }

            }

        } else if (type == NewQ.TYPE.LPR_EXP_RPR) {

            if (newQ.content.size() > 0) {

                Iterator iterator = newQ.content.iterator();

                while (iterator.hasNext()) {

                    Object object = iterator.next();

                    if (object instanceof Identifier)
                        stringBuilder.append(((Identifier) object).accept(this));
                    else if (object instanceof Y)
                        stringBuilder.append(((Y) object).accept(this));
                    else if (object instanceof Integer) {

                        stringBuilder.append("ldc");
                        stringBuilder.append(object);
                        stringBuilder.append("\n");

                    } else if (object instanceof String) {

                        stringBuilder.append("ldc \"");
                        stringBuilder.append(object);
                        stringBuilder.append("\"\n");

                    } else if (object instanceof Expression)
                        stringBuilder.append(((Expression) object).accept(this));


                }

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(Y y) {

        StringBuilder stringBuilder = new StringBuilder();

        Y.TYPE type = y.type;

        if (type == Y.TYPE.DOT_LENGTH_Y) {

            stringBuilder = new StringBuilder();

            if (y.content.size() > 0) {

                Iterator iterator = y.content.iterator();

                while (iterator.hasNext() && iterator.next() instanceof Identifier) {

                    stringBuilder.append(((Identifier) iterator.next()).accept(this));
                    stringBuilder.append("\n");
                    stringBuilder.append("arraylength\n");

                }

            }

        } else if (type == Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y) {

            stringBuilder = new StringBuilder();

            if (y.content.size() > 0) {

                Iterator iterator = y.content.iterator();

                while (iterator.hasNext()) {

                    if (iterator.next() instanceof ArrayList) {

                    } else if (iterator.next() instanceof Identifier) {

                    }

                }

            }

        }

        return stringBuilder.toString();

    }

    @Override
    public String visit(NameAnalyzer nameAnalyzer) {
        return "";
    }

    private File generateJasminFile(String filename, String content) {

        File file = new File(filePath + filename + ".j");
        FileWriter fileWriter;

        try {

            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;

    }

    private void generateClassFile(File file) {

        try {

            String[] fileArgs = {"--d", file.getParent(), file.getPath()};
            Main.main(fileArgs);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }


    }

    private String generateNewQContent(NewQ newQ) {

        StringBuilder stringBuilder = new StringBuilder();

        if (newQ.content.size() > 0) {

        }

        return stringBuilder.toString();
    }

    private String generateLabel() {
        return LABEL + (++labels);
    }

    private void setVariableSlot(Identifier identifier) {

        if (identifier.getSymbol() != null && identifier.getSymbol() instanceof VarSymbol) {

            ((VarSymbol) identifier.getSymbol()).setIndex(++slotNumber);

        }

    }

    private int getVariableSlot(Identifier identifier) {

        return (identifier.getSymbol() != null && identifier.getSymbol() instanceof VarSymbol) ?
                ((VarSymbol) identifier.getSymbol()).getIndex() : -1;

    }

}
