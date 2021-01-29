package analyser;

import analyser.errors.name.ClassDefinitionNotFoundError;
import analyser.errors.name.DuplicateVariableDefinitionError;
import analyser.errors.name.NameAnalysisError;
import ast.Identifier;
import ast.MethodDeclaration;
import ast.Type;
import ast.VarDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodSymbol extends Symbol {
    
    // In the case that this method returns an instance of a class. e.g. public A foo(){...};
    private ClassSymbol typeClass;
    // The class that this method belongs to.
    private ClassSymbol classSymbol;

    // In the case that this method is not the main method of the program
    MethodDeclaration methodDeclaration;

    //In the case that this method is the main method of the program
    Identifier methodIdenifier;
    // The number of arguments this method takes. e.g. public int foo( int i){...} takes one argument.
    int argumentNumber;

    private List<VarSymbol> methodArguments = new ArrayList<>();
    private List<VarSymbol> methodVariables = new ArrayList<>();

    public MethodSymbol(MethodDeclaration methodDeclaration, ClassSymbol classSymbol) throws ClassDefinitionNotFoundError {
        this.symbolTable = classSymbol.symbolTable;
        setDefinitionLine(methodDeclaration.methodName.getLine());
        setDefinitionColumn(methodDeclaration.methodName.getColumn());
        setMethodDeclaration(methodDeclaration);
        setSymbolName(getMethodDeclaration().methodName.idName);
        getMethodDeclaration().methodName.setSymbol(this);
        setArgumentNumber( methodDeclaration.typeIdentifiers == null ? 0 : methodDeclaration.typeIdentifiers.size() );
        setClassSymbol( classSymbol );
        setUniqueName( symbolTable.generateUniqueName( getSymbolName() ) );
        if( methodDeclaration.methodType.typeName.equals( Type.TypeName.IDENTIFIER ) ){
                setTypeClass( symbolTable.getClassSymbol( methodDeclaration.methodType.identifier ) );
        }
        else{
            setType( methodDeclaration.methodType );
        }

    }

    public MethodSymbol(Identifier mainMethodIdentifier, ClassSymbol classSymbol) {
        this.symbolTable = classSymbol.symbolTable;

        setDefinitionLine(mainMethodIdentifier.getLine());
        setDefinitionColumn(mainMethodIdentifier.getColumn());
        setMethodIdenifier(mainMethodIdentifier);
        setSymbolName(getMethodIdenifier().idName);
        getMethodIdenifier().setSymbol(this);
        setArgumentNumber( 1 );
        setClassSymbol( classSymbol );
        setUniqueName( symbolTable.generateUniqueName( getSymbolName() ) );
    }

    public List<NameAnalysisError> applyMethodArguments() {
        List<NameAnalysisError> errors = new ArrayList<>();

        if (methodIdenifier != null) { // This is the main method of the main class of the program

        } else {

            if (methodDeclaration.typeIdentifiers != null)
                for (Map<Type, Identifier> varDeclaration : methodDeclaration.typeIdentifiers) {

                    for (Type type : varDeclaration.keySet()) {

                        try {
                            checkPreDefinedVariable(varDeclaration.get(type));
                        } catch (DuplicateVariableDefinitionError duplicateVariableDefinitionError) {
                            errors.add(duplicateVariableDefinitionError);
                        }

                        try {
                            methodArguments.add(new VarSymbol(varDeclaration.get(type), type, this));
                        } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                            errors.add(classDefinitionNotFoundError);
                        }

                    }

                }

        }

        return errors;
    }


        public List<NameAnalysisError> applyMethodVariables() {

        List<NameAnalysisError> errors = new ArrayList<>();

        if (methodIdenifier != null) { // This is the main method of the main class of the program

        } else {


            if (methodDeclaration.varDeclarations != null)
                for (VarDeclaration varDeclaration : methodDeclaration.varDeclarations) {

                    try {
                        checkPreDefinedVariable(varDeclaration.identifier);
                    } catch (DuplicateVariableDefinitionError duplicateVariableDefinitionError) {
                        errors.add(duplicateVariableDefinitionError);
                    }

                    try {
                        methodVariables.add(new VarSymbol(varDeclaration.identifier, varDeclaration.type, this));
                    } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                        errors.add(classDefinitionNotFoundError);
                    }

                }
        }

        return errors;

    }

    private void checkPreDefinedVariable(Identifier identifier) throws DuplicateVariableDefinitionError {
        for (VarSymbol varSymbol : methodArguments) {
            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                throw new DuplicateVariableDefinitionError(identifier.idName, identifier.getLine(), identifier.getColumn(), varSymbol);
        }
        for (VarSymbol varSymbol : methodVariables) {
            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                throw new DuplicateVariableDefinitionError(identifier.idName, identifier.getLine(), identifier.getColumn(), varSymbol);
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

    public int getArgumentNumber() {
        return argumentNumber;
    }

    public void setArgumentNumber(int argumentNumber) {
        this.argumentNumber = argumentNumber;
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }

    private void setMethodDeclaration(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public Identifier getMethodIdenifier() {
        return methodIdenifier;
    }

    private void setMethodIdenifier(Identifier methodIdenifier) {
        this.methodIdenifier = methodIdenifier;
    }

    public List<VarSymbol> getMethodArguments() {
        return methodArguments;
    }

    public void setMethodArguments(List<VarSymbol> methodArguments) {
        this.methodArguments = methodArguments;
    }

    public List<VarSymbol> getMethodVariables() {
        return methodVariables;
    }

    public void setMethodVariables(List<VarSymbol> methodVariables) {
        this.methodVariables = methodVariables;
    }

}
