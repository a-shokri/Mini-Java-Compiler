package analyser.errors.name;

import analyser.errors.AnalysisError;

public abstract class NameAnalysisError extends AnalysisError {

    private String identifierName;

    public String getIdentifierName() {
        return identifierName;
    }

    public void setIdentifierName(String identifierName) {
        this.identifierName = identifierName;
    }

}
