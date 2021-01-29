package ast;

import lexer.CommonConstants;

public class VarDeclaration extends Tree {

    public Type type;
    public Identifier identifier;
    public int index = -1;

    public VarDeclaration(Type type, Identifier identifier) {

        super(CommonConstants.AstNodeType.VAR_DECLARATION);

        if (identifier != null)
            identifier.setParent(this);

        if (type != null)
            type.setParent(this);

        this.identifier = identifier;
        this.type = type;

    }

    @Override
    public int getLine() {
        return type.getLine();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
