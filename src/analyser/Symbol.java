package analyser;

import ast.Type;

public class Symbol {

    private String uniqueName;

    private int definitionLine;

    private int definitionColumn;

    private String symbolName;

    private Type type;

    protected SymbolTable symbolTable;

/*
    // If a variable is of type of a class, we need to have a pointer from the symbol of that variable
    // to the symbol of that class.
    // Furthermore, the return type of a method could be specified with this property.
    private Symbol typeSymbol;

    // If the symbol is a method, this field referes to the class that this method is defined in.
    // If the symbol is a class, this field referes to the class that this class is extended from.
    // if the symbol is a variable, this field referes to the class that this symbol is defined in.
    private Symbol parentSymbol;

    // Every class has variables and methods. this field contains name identifiers of them.
    private List<Symbol> childrenSymbols = new ArrayList<>();
*/

    // represents the new scope for that symbol (e.g. Class scope, Method scope)
 //   private SymbolTable symbolTable;

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public int getDefinitionLine() {
        return definitionLine;
    }

    public void setDefinitionLine(int definitionLine) {
        this.definitionLine = definitionLine;
    }

    public int getDefinitionColumn() {
        return definitionColumn;
    }

    public void setDefinitionColumn(int definitionColumn) {
        this.definitionColumn = definitionColumn;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
    
}
