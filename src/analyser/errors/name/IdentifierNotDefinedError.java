package analyser.errors.name;

public class IdentifierNotDefinedError extends NameAnalysisError {

    public IdentifierNotDefinedError(String identifier, int line, int column) {

        setIdentifierName(identifier);
        setLine(line);
        setColumn(column);

    }

    @Override
    protected String getSpecificErrorMessage() {
        return "Identifier " + getIdentifierName() + " is not defined!";
    }
    
}
