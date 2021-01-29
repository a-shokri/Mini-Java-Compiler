package ast;

import lexer.CommonConstants;

public class IntLiteral extends Tree {

    public int value;

    public IntLiteral(int value) {

        super(CommonConstants.AstNodeType.INT_LITERAL);
        this.value = value;

    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
