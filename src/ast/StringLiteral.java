package ast;

import lexer.CommonConstants;

public class StringLiteral extends Tree {

    public String value;

    public StringLiteral(String value) {

        super(CommonConstants.AstNodeType.STRING_LITERAL);
        this.value = value;

    }

    public String getValue() {
        return value;
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
