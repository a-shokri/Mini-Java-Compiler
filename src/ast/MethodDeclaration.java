package ast;

import lexer.CommonConstants;

import java.util.List;
import java.util.Map;

public class MethodDeclaration extends Tree {

    public Type methodType;
    public Identifier methodName;
    public List<Map<Type, Identifier>> typeIdentifiers;
    public List<VarDeclaration> varDeclarations;
    public List<Statement> statements;
    public Expression returnExpression;

    public MethodDeclaration(Type methodType, Identifier methodName, List<Map<Type, Identifier>> typeIdentifiers,
                             List<VarDeclaration> varDeclarations, List<Statement> statements, Expression returnExpression) {

        super(CommonConstants.AstNodeType.METHOD_DECLARATION);
        this.methodType = methodType;
        this.methodName = methodName;
        this.typeIdentifiers = typeIdentifiers;
        this.varDeclarations = varDeclarations;
        this.statements = statements;
        this.returnExpression = returnExpression;

        if (this.methodType != null)
            this.methodType.setParent(this);

        this.methodName.setParent(this);

        if (this.typeIdentifiers != null)
            for (Map<Type, Identifier> map : this.typeIdentifiers)
                for (Type key : map.keySet()) {
                    key.setParent(this);
                    map.get(key).setParent(this);
                }

        if (varDeclarations != null)
            for (VarDeclaration varDeclaration : varDeclarations)
                varDeclaration.setParent(this);

        if (statements != null)
            for (Statement statement : statements)
                statement.setParent(this);

        returnExpression.setParent(this);

    }


    @Override
    public int getLine() {
        return methodName.getLine();
    }
    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
