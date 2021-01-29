package analyser.errors.type;

import ast.Identifier;
import ast.Tree;
import ast.Type;

import java.util.ArrayList;
import java.util.List;

public class ExpectedTypeNotFoundError extends TypeAnalysisError {

    private List<Type.TypeName> expectedTypeNames = new ArrayList<>();
    private Type.TypeName foundTypeName;
    private Identifier expectedTypeIdentifier;
    private Identifier foundTypeIdentifier;
    private Tree astNode;

    @Override
    public int getLine() {
        return getAstNode() != null ? getAstNode().getLine() : super.getLine();
    }

    public ExpectedTypeNotFoundError(Type.TypeName[] typeNameArr, Type foundType, Tree astNode) {

        for (Type.TypeName tn : typeNameArr)
            expectedTypeNames.add(tn);

        setFoundTypeName(foundType.typeName);

        if (getFoundTypeName().equals(Type.TypeName.IDENTIFIER))
            setFoundTypeIdentifier(foundType.identifier);

        setAstNode(astNode);
    }

    public ExpectedTypeNotFoundError(Type expectedType, Type foundType, Tree astNode) {

        expectedTypeNames.add(expectedType.typeName);

        if (expectedType.typeName.equals(Type.TypeName.IDENTIFIER))
            setExpectedTypeIdentifier(expectedType.identifier);

        setFoundTypeName(foundType.typeName);

        if (getFoundTypeName().equals(Type.TypeName.IDENTIFIER))
            setFoundTypeIdentifier(foundType.identifier);

        setAstNode(astNode);

    }

    public ExpectedTypeNotFoundError(Type.TypeName expectedTypeName, Type foundType, Tree astNode) {

        expectedTypeNames.add(expectedTypeName);
        setFoundTypeName(foundType.typeName);

        if (getFoundTypeName().equals(Type.TypeName.IDENTIFIER))
            setFoundTypeIdentifier(foundType.identifier);

        setAstNode(astNode);

    }

    @Override
    protected String getSpecificErrorMessage() {

        String expectedTypeName = "";

        if (getExpectedTypeNames().get(0).equals(Type.TypeName.IDENTIFIER))
            expectedTypeName += getExpectedTypeIdentifier().idName;

        else {

            String expectedTypeNames = "";
            for (Type.TypeName etn : getExpectedTypeNames())
                expectedTypeName += ((expectedTypeName.isEmpty()) ? "" : " or ") + etn.name();

        }

        String foundTypeName = getFoundTypeName().equals(Type.TypeName.IDENTIFIER) ?
                getFoundTypeIdentifier().idName : getFoundTypeName().name();

        return "Expected type " + expectedTypeName + ", but found " + foundTypeName + "!";

    }

    private List<Type.TypeName> getExpectedTypeNames() {
        return expectedTypeNames;
    }

    public void setExpectedTypeNames(List<Type.TypeName> expectedTypeNames) {
        this.expectedTypeNames = expectedTypeNames;
    }

    private Type.TypeName getFoundTypeName() {
        return foundTypeName;
    }

    private void setFoundTypeName(Type.TypeName foundTypeName) {
        this.foundTypeName = foundTypeName;
    }

    private Identifier getExpectedTypeIdentifier() {
        return expectedTypeIdentifier;
    }

    private void setExpectedTypeIdentifier(Identifier expectedTypeIdentifier) {
        this.expectedTypeIdentifier = expectedTypeIdentifier;
    }

    private Identifier getFoundTypeIdentifier() {
        return foundTypeIdentifier;
    }

    private void setFoundTypeIdentifier(Identifier foundTypeIdentifier) {
        this.foundTypeIdentifier = foundTypeIdentifier;
    }

    private Tree getAstNode() {
        return astNode;
    }

    private void setAstNode(Tree astNode) {
        this.astNode = astNode;
    }

}
