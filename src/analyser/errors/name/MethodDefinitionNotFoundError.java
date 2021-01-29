package analyser.errors.name;

import analyser.ClassSymbol;
import ast.Type;

import java.util.List;

public class MethodDefinitionNotFoundError extends NameAnalysisError {

    ClassSymbol classSymbol;
    int argumentNumber;
    List<Type> argumentTypes;

    public MethodDefinitionNotFoundError(String methodIdentifier, int argumentNumber, int line, int column, ClassSymbol classSymbol) {
        setIdentifierName(methodIdentifier);
        setLine(line);
        setColumn(column);
        setClassSymbol(classSymbol);
        this.argumentNumber = argumentNumber;
    }

    public MethodDefinitionNotFoundError(String methodIdentifier, List<Type> argTypes, int line, int column, ClassSymbol classSymbol) {
        setIdentifierName(methodIdentifier);
        setLine(line);
        setColumn(column);
        setClassSymbol(classSymbol);
        this.argumentTypes = argTypes;
    }

    @Override
    public String getSpecificErrorMessage() {
        String msg = "Method " + getIdentifierName() + " with ";
        if( argumentTypes != null && argumentTypes.size() > 0 ) {
            msg += "arguments (";

            for ( int i = 0; i < argumentTypes.size(); i++) {
                Type type = argumentTypes.get(i);
                msg += (i>0?", " : "") + (!type.typeName.equals(Type.TypeName.IDENTIFIER) ? type.typeName : type.identifier.idName);
            }
            msg += ")";
        }
        else
            msg += argumentNumber + " argument" + (argumentNumber > 1 ? "s" : "");
        msg += " is not defined in class " + classSymbol.getSymbolName() +
                (classSymbol.getExtendedClassSymbol() != null ? " or it's hierarchies" : "") + "!";
        return msg;
    }

    public ClassSymbol getClassSymbol() {
        return classSymbol;
    }

    public void setClassSymbol(ClassSymbol classSymbol) {
        this.classSymbol = classSymbol;
    }

}
