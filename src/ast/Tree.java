package ast;

import analyser.Symbol;
import lexer.CommonConstants;

public abstract class Tree {

    int line;
    int column;
    Symbol symbol;
    // every thing in a program sould be nodeType checked. e.g. An assignment, a method, ... . Therefore, we add a nodeType
    // to all nodes in AST tree.
    Type nodeType;

    private CommonConstants.AstNodeType astNodeType;

    Tree parent = null;

    public Tree(CommonConstants.AstNodeType astNodeType) {
        this.astNodeType = astNodeType;
    }

    protected Tree() {
    }

    public abstract <R> R accept(Visitor<R> v);


    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Tree getParent() {
        return parent;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }

    public Type getNodeType() {
        return nodeType;
    }

    public void setNodeType(Type nodeType) {
        this.nodeType = nodeType;
    }
    
}