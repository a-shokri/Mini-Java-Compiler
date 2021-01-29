package analyser.errors.name;

public class ClassDefinitionNotFoundError extends NameAnalysisError {

    public ClassDefinitionNotFoundError(String classIdentifier, int line, int column) {

        setIdentifierName(classIdentifier);
        setLine(line);
        setColumn(column);
        
    }

    @Override
    public String getSpecificErrorMessage() {
        return "Class " + getIdentifierName() + " is not defined!";
    }

}
