package ast;

import lexer.CommonConstants;

import java.util.List;

public class ClassDeclaration extends Tree {

    public Identifier classIdentifier;
    public Identifier extendsIdentifier;
    public List<VarDeclaration> varDeclarations;
    public List<MethodDeclaration> methodDeclarations;


    public ClassDeclaration(Identifier classIdentifier, Identifier extendsIdentifier,
                            List<VarDeclaration> varDeclarations,
                            List<MethodDeclaration> methodDeclarations) {
        super(CommonConstants.AstNodeType.CLASS_DECLARATION);
        this.classIdentifier = classIdentifier;
        this.extendsIdentifier = extendsIdentifier;
        this.methodDeclarations = methodDeclarations;
        this.varDeclarations = varDeclarations;

        this.classIdentifier.setParent(this);
        if (this.extendsIdentifier != null)
            this.extendsIdentifier.setParent(this);
        for (MethodDeclaration methodDeclaration : methodDeclarations)
            methodDeclaration.setParent(this);
        for (VarDeclaration varDeclaration : varDeclarations)
            varDeclaration.setParent(this);
    }


    @Override
    public int getLine() {
        return classIdentifier.getLine();
    }
    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
