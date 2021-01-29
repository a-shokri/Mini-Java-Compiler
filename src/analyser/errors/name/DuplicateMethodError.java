package analyser.errors.name;

import analyser.Symbol;

public class DuplicateMethodError extends NameAnalysisError {

    Symbol alreadyDefinedMethodSymbol;

    public DuplicateMethodError(String variableIdentifier, int line, int column, Symbol alreadyDefinedMethodSymbol) {
        setIdentifierName(variableIdentifier);
        setLine(line);
        setColumn(column);
        this.alreadyDefinedMethodSymbol = alreadyDefinedMethodSymbol;
    }

    @Override
    public String getSpecificErrorMessage() {
        return "Method " + getIdentifierName() + " already defined at " + alreadyDefinedMethodSymbol.getDefinitionLine()
                + ":" + alreadyDefinedMethodSymbol.getDefinitionColumn() + " !";
    }

}
