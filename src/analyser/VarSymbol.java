package analyser;

import analyser.errors.name.ClassDefinitionNotFoundError;
import ast.Identifier;
import ast.Type;

public class VarSymbol extends Symbol {

    public enum VarType {CLASS, INT, INT_ARR, STRING, STRING_ARR, BOOLEAN}

    // In the case that this variable is of a defined class. e.g. A a;
    private ClassSymbol typeClass;
    // In the case that this is a global variable in a class
    private ClassSymbol classSymbol;
    // In the case that this is a variable defined whithin a method (or an argument of a method)
    private MethodSymbol methodSymbol;

    public int index = -1;

    Identifier varIdentifier;

    public VarSymbol(Identifier varIdentifier, Type varType, ClassSymbol classSymbol) throws ClassDefinitionNotFoundError {
        this.symbolTable = classSymbol.symbolTable;
        setClassSymbol(classSymbol);
        setDefinitionLine(varIdentifier.getLine());
        setDefinitionColumn(varIdentifier.getColumn());
        setVarIdentifier(varIdentifier);
        setSymbolName(varIdentifier.idName);
        setUniqueName(symbolTable.generateUniqueName(getSymbolName()));
        getVarIdentifier().setSymbol(this);
        setType(varType);

        switch (varType.typeName) {
            case IDENTIFIER:
                setTypeClass(symbolTable.getClassSymbol(varType.identifier));
                varType.identifier.setSymbol( getTypeClass() );

        }

    }

    public VarSymbol(Identifier varIdentifier, Type varType, MethodSymbol methodSymbol) throws ClassDefinitionNotFoundError {
        this.symbolTable = methodSymbol.symbolTable;
        setMethodSymbol(methodSymbol);
        setDefinitionLine(varIdentifier.getLine());
        setDefinitionColumn(varIdentifier.getColumn());
        setVarIdentifier(varIdentifier);
        setSymbolName(varIdentifier.idName);
        setUniqueName(symbolTable.generateUniqueName(getSymbolName()));
        getVarIdentifier().setSymbol(this);
        setType(varType);

        switch (varType.typeName) {
            case IDENTIFIER:
                setTypeClass(symbolTable.getClassSymbol(varType.identifier));
                varType.identifier.setSymbol( getTypeClass() );
                break;
        }

    }

    public ClassSymbol getTypeClass() {
        return typeClass;
    }

    private void setTypeClass(ClassSymbol typeClass) {
        this.typeClass = typeClass;
    }

    public ClassSymbol getClassSymbol() {
        return classSymbol;
    }

    private void setClassSymbol(ClassSymbol classSymbol) {
        this.classSymbol = classSymbol;
    }

    public MethodSymbol getMethodSymbol() {
        return methodSymbol;
    }

    private void setMethodSymbol(MethodSymbol methodSymbol) {
        this.methodSymbol = methodSymbol;
    }

    public Identifier getVarIdentifier() {
        return varIdentifier;
    }

    private void setVarIdentifier(Identifier varIdentifier) {
        this.varIdentifier = varIdentifier;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
