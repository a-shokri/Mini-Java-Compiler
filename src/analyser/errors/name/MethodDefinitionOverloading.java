package analyser.errors.name;

import analyser.MethodSymbol;

public class MethodDefinitionOverloading extends NameAnalysisError {

    MethodSymbol overloadeeMethod;

    public MethodDefinitionOverloading(String methodIdentifier, int line, int column, MethodSymbol overloadeeMethod) {

        this.overloadeeMethod = overloadeeMethod;
        setIdentifierName(methodIdentifier);
        setLine(line);
        setColumn(column);

    }

    @Override
    protected String getSpecificErrorMessage() {

        return "Method overloading is not permitted! " + overloadeeMethod.getMethodDeclaration().methodName.idName + " method is already defined in class "
                + overloadeeMethod.getClassSymbol().getSymbolName() + " at " +
                overloadeeMethod.getMethodDeclaration().methodName.getLine() + "," + overloadeeMethod.getMethodDeclaration().methodName.getColumn();
        
    }

}
