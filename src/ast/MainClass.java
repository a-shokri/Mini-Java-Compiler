package ast;

import lexer.CommonConstants;

import java.util.ArrayList;

public class MainClass extends Tree {

    public Identifier classIdentifier;
    public Identifier methodIdentifier;
    public ArrayList<Statement> statements;


    public MainClass(Identifier classIdentifier, Identifier methodIdentifier, ArrayList<Statement> statements) {

        super(CommonConstants.AstNodeType.MAIN_CLASS);

        if (classIdentifier != null)
            classIdentifier.setParent(this);

        if (methodIdentifier != null)
            methodIdentifier.setParent(this);

        if (statements != null) {

            for (Statement statement : statements)
                statement.setParent(this);

        }

        this.classIdentifier = classIdentifier;
        this.methodIdentifier = methodIdentifier;
        this.statements = statements;

    }

    public Identifier getClassIdentifier() {
        return classIdentifier;
    }

    public void setClassIdentifier(Identifier classIdentifier) {
        this.classIdentifier = classIdentifier;
    }

    public Identifier getMethodIdentifier() {
        return methodIdentifier;
    }

    public void setMethodIdentifier(Identifier methodIdentifier) {
        this.methodIdentifier = methodIdentifier;
    }

    public ArrayList<Statement> getStatement() {
        return statements;
    }

    public void setStatement(ArrayList<Statement> statements) {
        this.statements = statements;
    }


    @Override
    public int getLine() {
        return getClassIdentifier().getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
