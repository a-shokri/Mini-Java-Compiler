package ast;

import lexer.CommonConstants;

public class Identifier extends Tree {

    public String idName;

    public Identifier(String idName) {

        super(CommonConstants.AstNodeType.IDENTIFIER);
        this.idName = idName;
        
    }

    @Override
    public Type getNodeType() {

        if (super.getNodeType() != null)
            return super.getNodeType();

        if (getSymbol() != null)
            return getSymbol().getType();

        return null;

    }


    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return idName;
    }
}
