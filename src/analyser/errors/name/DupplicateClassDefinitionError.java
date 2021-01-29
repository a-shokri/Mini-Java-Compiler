package analyser.errors.name;

import analyser.Symbol;

public class DupplicateClassDefinitionError extends NameAnalysisError {

    Symbol alreadyDefinedClassSymbol;

    public DupplicateClassDefinitionError(String classIdentifier, int line, int column, Symbol alreadyDefinedClassSymbol) {

        this.alreadyDefinedClassSymbol = alreadyDefinedClassSymbol;
        setIdentifierName(classIdentifier);
        setLine(line);
        setColumn(column);

    }

    @Override
    public String getSpecificErrorMessage() {

        return "Class " + getIdentifierName() + " is already defined at " + alreadyDefinedClassSymbol.getDefinitionLine()
                + ":" + alreadyDefinedClassSymbol.getDefinitionColumn() + " !";
        
    }

}
