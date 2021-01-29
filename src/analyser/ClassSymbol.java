package analyser;

import analyser.errors.name.*;
import ast.*;

import java.util.ArrayList;
import java.util.List;

public class ClassSymbol extends Symbol {

    List<MethodSymbol> methodSymbols = new ArrayList<>();
    List<VarSymbol> globalVariableSymbols = new ArrayList<>();

    ClassSymbol extendedClassSymbol = null;

    // If the class is not the main class
    ClassDeclaration classDeclaration;
    // If the class is the main class
    private MainClass mainClass;


    public ClassSymbol(ClassDeclaration classDeclaration, SymbolTable symbolTable) throws DupplicateClassDefinitionError {
        this.symbolTable = symbolTable;
        setDefinitionLine(classDeclaration.classIdentifier.getLine());
        setDefinitionColumn(classDeclaration.classIdentifier.getColumn());
        classDeclaration.classIdentifier.setSymbol(this);
        setClassDeclaration(classDeclaration);
        setSymbolName(getClassDeclaration().classIdentifier.idName);
        String uniqueName = symbolTable.generateUniqueName(getSymbolName());
        setUniqueName(uniqueName);
        symbolTable.addClassSymbolToProgramScope(this);
        Type type = new Type( Type.TypeName.IDENTIFIER );
        type.identifier = this.classDeclaration.classIdentifier;
        type.setSymbol(this);
        setType( type );

    }

    public ClassSymbol(MainClass mainClass, SymbolTable symbolTable) throws DupplicateClassDefinitionError {
        this.symbolTable = symbolTable;

        setDefinitionLine(mainClass.classIdentifier.getLine());
        setDefinitionColumn(mainClass.classIdentifier.getColumn());
        mainClass.classIdentifier.setSymbol(this);
        setMainClass(mainClass);
        setSymbolName(getMainClass().classIdentifier.idName);
        setUniqueName(symbolTable.generateUniqueName(getSymbolName()));
        symbolTable.addClassSymbolToProgramScope(this);


        Type type = new Type( Type.TypeName.IDENTIFIER );
        // type.identifier = this.classDeclaration.classIdentifier;
        type.setSymbol(this);
        setType( type );

    }

    public List<NameAnalysisError> applyExtendedClass() {

        List<NameAnalysisError> errors = new ArrayList<>();
        // Check if it is not the main class then check the extended class
        if (getMainClass() == null && getClassDeclaration().extendsIdentifier != null) {

            try {
                setExtendedClassSymbol(symbolTable.getClassSymbol(getClassDeclaration().extendsIdentifier));
                getClassDeclaration().extendsIdentifier.setSymbol(getExtendedClassSymbol());
            } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                errors.add(classDefinitionNotFoundError);
            }

        }

        return errors;

    }

    public List<NameAnalysisError> applyMethodAndVariable() {

        List<NameAnalysisError> errors = new ArrayList<>();

        if (getMainClass() != null) { // If the Class is the MainClass, then it has only one method wothout any global variables
            methodSymbols.add(new MethodSymbol(getMainClass().getMethodIdentifier(), this));
        } else { // It is not the MainClass. Therefore, it might has global variables and several methods.

            if (classDeclaration.varDeclarations != null)
                for (VarDeclaration varDeclaration : classDeclaration.varDeclarations) {

                    try {
                        VarSymbol preDefinedVarSymbol = null;

                        try {
                            preDefinedVarSymbol = (VarSymbol) symbolTable.findCorrespondingSymbol(varDeclaration.identifier, true, false, false);
                        } catch (IdentifierNotDefinedError identifierNotDefinedError) {
                        }

                        if (preDefinedVarSymbol != null) // the variable is already defined in this class or it's ancestors
                            errors.add(new DuplicateVariableDefinitionError(varDeclaration.identifier.idName, varDeclaration.identifier.getLine(),
                                    varDeclaration.identifier.getColumn(), preDefinedVarSymbol));
                        else {
                            VarSymbol varSymbol = new VarSymbol(varDeclaration.identifier, varDeclaration.type, this);
                            globalVariableSymbols.add(varSymbol);
                        }

                    } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                        errors.add(classDefinitionNotFoundError);
                    }

                }

            if (classDeclaration.methodDeclarations != null)

                for (MethodDeclaration methodDeclaration : classDeclaration.methodDeclarations) {

                    MethodSymbol methodSymbol = null;

                    try {

                        methodSymbol = new MethodSymbol(methodDeclaration, this);
                        methodDeclaration.methodName.setSymbol(methodSymbol);
                        methodSymbol.applyMethodArguments();
                        methodSymbols.add(methodSymbol);

                    } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                        errors.add(classDefinitionNotFoundError);
                    }

                }
        }

        return errors;

    }

    public List<MethodSymbol> getMethodSymbols() {
        return methodSymbols;
    }

    public void setMethodSymbols(List<MethodSymbol> methodSymbols) {
        this.methodSymbols = methodSymbols;
    }

    public List<VarSymbol> getGlobalVariableSymbols() {
        return globalVariableSymbols;
    }

    public void setGlobalVariableSymbols(List<VarSymbol> globalVariableSymbols) {
        this.globalVariableSymbols = globalVariableSymbols;
    }

    public ClassSymbol getExtendedClassSymbol() {
        return extendedClassSymbol;
    }

    private void setExtendedClassSymbol(ClassSymbol extendedClassSymbol) {
        this.extendedClassSymbol = extendedClassSymbol;
    }

    public ClassDeclaration getClassDeclaration() {
        return classDeclaration;
    }

    private void setClassDeclaration(ClassDeclaration classDeclaration) {
        this.classDeclaration = classDeclaration;
    }

    public MainClass getMainClass() {
        return mainClass;
    }

    private void setMainClass(MainClass mainClass) {
        this.mainClass = mainClass;
    }


}
