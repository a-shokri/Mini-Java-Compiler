package analyser;

import analyser.errors.type.TypeAnalysisError;
import ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeAnalyzer implements Visitor<Type> {

    private HashMap<String, Type> classTypeMap = new HashMap<>();
    public List<TypeAnalysisError> errorList = new ArrayList<>();
    public Program program;

    public TypeAnalyzer(Program program) {
        this.program = program;
    }

    public void typeAnalysis() {
        visit(program);
    }

    @Override
    public Type visit(Program n) {

        n.mainClass.accept(this);

        for (ClassDeclaration classDeclaration : n.classList)
            classDeclaration.accept(this);

        return null;

    }

    @Override
    public Type visit(Expression n) {

        Type type = null;

        if (n.a != null)
            type = n.a.accept(this);

        if (n.t != null)
            type = n.t.accept(this);

        return type;

    }

    @Override
    public Type visit(MethodDeclaration methodDeclaration) {
        return null;
    }

    @Override
    public Type visit(A a) {
        return null;
    }

    @Override
    public Type visit(B b) {
        return null;
    }

    @Override
    public Type visit(C c) {
        return null;
    }

    @Override
    public Type visit(ClassDeclaration classDeclaration) {
        return null;
    }

    @Override
    public Type visit(D d) {
        return null;
    }

    @Override
    public Type visit(F f) {
        return null;
    }

    @Override
    public Type visit(G g) {
        return null;
    }

    @Override
    public Type visit(H h) {
        return null;
    }

    @Override
    public Type visit(J j) {
        return null;
    }

    @Override
    public Type visit(MainClass mainClass) {
        return null;
    }

    @Override
    public Type visit(StringLiteral stringLiteral) {
        return null;
    }

    @Override
    public Type visit(T t) {
        return null;
    }

    @Override
    public Type visit(Type type) {
        return type;
    }

    @Override
    public Type visit(VarDeclaration varDeclaration) {
        return varDeclaration.type;
    }

    @Override
    public Type visit(Identifier identifier) {
        return null;
    }

    @Override
    public Type visit(IntLiteral intLiteral) {
        return null;
    }

    @Override
    public Type visit(Block block) {
        return null;
    }

    @Override
    public Type visit(Skip skip) {
        return null;
    }

    @Override
    public Type visit(IfThenElse ifThenElse) {
        return null;
    }

    @Override
    public Type visit(While aWhile) {
        return null;
    }

    @Override
    public Type visit(Print print) {
        return null;
    }

    @Override
    public Type visit(Assign assign) {

        Type idType = assign.identifier.accept(this);
        Type expType = assign.expression.accept(this);

        if (!(idType.typeName.name().equals(expType.typeName.name())))
            errorList.add(new TypeAnalysisError() {
                @Override
                protected String getSpecificErrorMessage() {
                    return "Error: Type Mismatch - Expected type: " + idType.typeName.name() +
                            ", Found type: " + expType.typeName.name();
                }
            });

        return null;

    }

    @Override
    public Type visit(ArrayAssign arrayAssign) {
        return null;
    }

    @Override
    public Type visit(Sidef sidef) {
        return null;
    }

    @Override
    public Type visit(NewQ newQ) {
        return null;
    }

    @Override
    public Type visit(Y y) {
        return null;
    }

    @Override
    public Type visit(NameAnalyzer nameAnalyzer) {
        return null;
    }

}
