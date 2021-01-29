package analyser;

import analyser.errors.name.ClassDefinitionNotFoundError;
import analyser.errors.name.DupplicateClassDefinitionError;
import analyser.errors.name.IdentifierNotDefinedError;
import analyser.errors.name.MethodDefinitionNotFoundError;
import ast.*;

import java.util.HashMap;
import java.util.List;

public class SymbolTable {

    public SymbolTable(){}

    private int identifierCounter = 0;
    private HashMap<String, ClassSymbol> classSymbolList = new HashMap<>();

    public String generateUniqueName(String symbolName) {
        return symbolName + "_" + (identifierCounter++) + "_";
    }

    public void addClassSymbolToProgramScope(ClassSymbol classSymbol) throws DupplicateClassDefinitionError {
        ClassSymbol prev = classSymbolList.get(classSymbol.getSymbolName());
        if (prev != null)
            throw new DupplicateClassDefinitionError(classSymbol.getSymbolName(), classSymbol.getDefinitionLine(), classSymbol.getDefinitionColumn(), prev);
//        classSymbol.setUniqueName(classSymbol.getSymbolName() + "_" + identifierCounter);
        classSymbolList.put(classSymbol.getSymbolName(), classSymbol);
//        identifierCounter++;
    }

    public ClassSymbol getClassSymbol(Identifier classIdentifier) throws ClassDefinitionNotFoundError {
        ClassSymbol prev = classSymbolList.get(classIdentifier.idName);
        if (prev == null)
            throw new ClassDefinitionNotFoundError(classIdentifier.idName, classIdentifier.getLine(), classIdentifier.getColumn());
        return prev;
    }

    public MethodSymbol getMethodSymbol(Identifier classIdentifier, Identifier methodIdentifer, int argumentNumber) throws ClassDefinitionNotFoundError, MethodDefinitionNotFoundError {
        ClassSymbol classSymbol = getClassSymbol(classIdentifier);
        List<MethodSymbol> methodSymbolList = classSymbol.getMethodSymbols();
        for (MethodSymbol methodSymbol : methodSymbolList) {
            if (methodSymbol.getMethodIdenifier() != null) { // Main method in the main class
                if (methodSymbol.getMethodIdenifier().idName.equals(methodIdentifer.idName))
                    return methodSymbol;
            } else {
                if (methodSymbol.getMethodDeclaration().methodName.idName.equals(methodIdentifer.idName))
                    return methodSymbol;
            }
        }


        throw new MethodDefinitionNotFoundError(methodIdentifer.idName, argumentNumber, methodIdentifer.getLine(), methodIdentifer.getColumn(),
                classSymbol);
    }

    public Symbol findCorrespondingSymbolInAncestorClasses(Identifier identifier, boolean findVariableSymbol, boolean findMethodSymbol) throws IdentifierNotDefinedError {
        if (identifier == null)
            return null;
        Tree tmp = identifier.getParent();
        boolean passedItsClass = false;
        while (tmp != null) {

            if (tmp instanceof ClassDeclaration) {
                if (!passedItsClass) { // Now we reached the classSymbol of that identifier, now we have to go to the ancestor classes.
                    passedItsClass = true;
                } else { // Now we are in the ancestors class

                    // Then check class global variables
                    ClassDeclaration classDeclaration = tmp instanceof MethodDeclaration ? (ClassDeclaration) ((MethodDeclaration) tmp).getParent() :
                            (ClassDeclaration) tmp;
                    ClassSymbol classSymbol = (ClassSymbol) classDeclaration.classIdentifier.getSymbol();
                    if (findVariableSymbol)
                        for (VarSymbol varSymbol : classSymbol.globalVariableSymbols)
                            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                                return varSymbol;
                    if (findMethodSymbol)
                        for (MethodSymbol methodSymbol : classSymbol.methodSymbols) {
                            Identifier identifier1 = methodSymbol.methodIdenifier != null ? methodSymbol.methodIdenifier : methodSymbol.methodDeclaration.methodName;
                            if (identifier1.idName.equals(identifier.idName))
                                return methodSymbol;
                        }

                    // Then, check if the class is extending another class
                    while (true) {
                        classSymbol = classSymbol.extendedClassSymbol;
                        // Here, we searched all method variables, class variables, methods and variables in the extended classes and found nothing!
                        // Now, we are going to take a look at the classes names as the last step to find that identifer.
                        if (classSymbol == null) {
                            try {
                                Symbol classSymbol1 = getClassSymbol(identifier);
                                return classSymbol1;
                            } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                                throw new IdentifierNotDefinedError(identifier.idName, identifier.getLine(), identifier.getColumn());
                            }
                        }
                        for (VarSymbol varSymbol : classSymbol.globalVariableSymbols)
                            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                                return varSymbol;
                        for (MethodSymbol methodSymbol1 : classSymbol.methodSymbols)
                            if (methodSymbol1.getMethodIdenifier() != null) { // this is the main method in the main class
                                if (methodSymbol1.getMethodIdenifier().idName.equals(identifier.idName))
                                    return methodSymbol1;
                            } else if (methodSymbol1.getMethodDeclaration().methodName.idName.equals(identifier.idName))
                                return methodSymbol1;
                    }
                }

                try {
                    ClassSymbol classSymbol = (ClassSymbol) findCorrespondingSymbol(((ClassDeclaration) tmp).extendsIdentifier, false, false, true);
                    if (classSymbol != null)
                        tmp = classSymbol.classDeclaration;
                    else
                        tmp = null;
                } catch (IdentifierNotDefinedError e) {

                }
            } else
                tmp = tmp.getParent();
        }
        throw new IdentifierNotDefinedError(identifier.idName, identifier.getLine(), identifier.getColumn());
    }

    public Symbol findCorrespondingClass(NewQ newQ) { // for something like this.foo()
        Tree tmp = newQ.getParent();
        while (tmp != null) {
            if (tmp instanceof ClassDeclaration)
                return ((ClassDeclaration) tmp).classIdentifier.getSymbol();
            tmp = tmp.getParent();
        }
        return null;
    }

    public Symbol findCorrespondingClass(Y y) { // for something like new B().foo() to find the class symbol
        Tree tmp = y.getParent();
        while (tmp != null) {
            if (tmp instanceof ClassDeclaration)
                return ((ClassDeclaration) tmp).classIdentifier.getSymbol();
            tmp = tmp.getParent();
        }
        return null;
    }

    public Symbol findCorrespondingSymbol(Identifier identifier, boolean findVar, boolean findMethod, boolean findClass) throws IdentifierNotDefinedError {
        if (identifier == null)
            return null;
        if (identifier.getSymbol() != null)
            return identifier.getSymbol();
        Tree tmp = identifier.getParent();
        while (tmp != null) {
            if (tmp instanceof MethodDeclaration || tmp instanceof ClassDeclaration) {
                if (tmp instanceof MethodDeclaration) {
                    MethodDeclaration methodDeclaration = (MethodDeclaration) tmp;
                    MethodSymbol methodSymbol = (MethodSymbol) methodDeclaration.methodName.getSymbol();
                    // First, check variables defined in the first part of the method
                    if (findVar) {
                        for (VarSymbol varSymbol : methodSymbol.getMethodVariables())
                            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                                return varSymbol;

                        // Then, check method arguments
                        for (VarSymbol varSymbol : methodSymbol.getMethodArguments())
                            if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                                return varSymbol;
                    }
                    if (findMethod)
                        if (methodSymbol.getSymbolName().equals(identifier.idName))
                            return methodSymbol;
                }


                // Then check class global variables
                ClassDeclaration classDeclaration = tmp instanceof MethodDeclaration ? (ClassDeclaration) ((MethodDeclaration) tmp).getParent() :
                        (ClassDeclaration) tmp;
                ClassSymbol classSymbol = (ClassSymbol) classDeclaration.classIdentifier.getSymbol();
                if (findClass)
                    if (classSymbol.getSymbolName().equals(identifier.idName))
                        return classSymbol;

                if (findVar)
                    for (VarSymbol varSymbol : classSymbol.globalVariableSymbols)
                        if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                            return varSymbol;
                if (findMethod)
                    for (MethodSymbol methodSymbol : classSymbol.methodSymbols)
                        if (methodSymbol.getSymbolName().equals(identifier.idName))
                            return methodSymbol;

                // Then, check if the class is extending another class
                while (true) {
                    classSymbol = classSymbol.extendedClassSymbol;
                    // Here, we searched all method variables, class variables, methods and variables in the extended classes and found nothing!
                    // Now, we are going to take a look at the classes names as the last step to find that identifer.
                    if (classSymbol == null) {
                        try {
                            Symbol classSymbol1 = getClassSymbol(identifier);
                            return classSymbol1;
                        } catch (ClassDefinitionNotFoundError classDefinitionNotFoundError) {
                            throw new IdentifierNotDefinedError(identifier.idName, identifier.getLine(), identifier.getColumn());
                        }
                    }
                    for (VarSymbol varSymbol : classSymbol.globalVariableSymbols)
                        if (varSymbol.getVarIdentifier().idName.equals(identifier.idName))
                            return varSymbol;
                    for (MethodSymbol methodSymbol1 : classSymbol.methodSymbols)
                        if (methodSymbol1.getMethodIdenifier() != null) { // this is the main method in the main class
                            if (methodSymbol1.getMethodIdenifier().idName.equals(identifier.idName))
                                return methodSymbol1;
                        } else if (methodSymbol1.getMethodDeclaration().methodName.idName.equals(identifier.idName))
                            return methodSymbol1;
                }
            }
            tmp = tmp.getParent();
        }
        throw new IdentifierNotDefinedError(identifier.idName, identifier.getLine(), identifier.getColumn());

    }

    public HashMap<String, ClassSymbol> getClassSymbolList() {
        return classSymbolList;
    }

}
