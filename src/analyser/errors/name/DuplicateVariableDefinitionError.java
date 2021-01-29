package analyser.errors.name;

import analyser.Symbol;

public class DuplicateVariableDefinitionError extends NameAnalysisError {

    Symbol alreadyDefinedVariableSymbol;

    public DuplicateVariableDefinitionError(String variableIdentifier, int line, int column, Symbol alreadyDefinedVariableSymbol) {
        setIdentifierName(variableIdentifier);
        setLine(line);
        setColumn(column);
        this.alreadyDefinedVariableSymbol = alreadyDefinedVariableSymbol;
    }

    @Override
    public String getSpecificErrorMessage() {
        return "Variable " + getIdentifierName() + " is already defined at " + alreadyDefinedVariableSymbol.getDefinitionLine()
                + ":" + alreadyDefinedVariableSymbol.getDefinitionColumn() + " !";
    }

}
