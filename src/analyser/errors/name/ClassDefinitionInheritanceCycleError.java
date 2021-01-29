package analyser.errors.name;

import analyser.ClassSymbol;
import ast.Identifier;

public class ClassDefinitionInheritanceCycleError extends NameAnalysisError {

    ClassSymbol classSymbol;
    ClassSymbol extendedClassSymbol;

    public ClassDefinitionInheritanceCycleError(ClassSymbol classSymbol, ClassSymbol extendedClassSymbol,
                                                Identifier tmpClassDeclarationIdentifier) {

        this.classSymbol = classSymbol;
        this.extendedClassSymbol = extendedClassSymbol;
        setLine(tmpClassDeclarationIdentifier.getLine());
        setColumn(tmpClassDeclarationIdentifier.getColumn());

    }

    @Override
    protected String getSpecificErrorMessage() {

        return "Class inheritance cycle: Class " + classSymbol.getSymbolName() + " is extending class " + extendedClassSymbol.getSymbolName() +
                " which is already in it's extension branch!";

    }

}
