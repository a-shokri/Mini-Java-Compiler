package ast;

import lexer.CommonConstants;

public abstract class Statement extends Tree {

    public Statement() {
        super(CommonConstants.AstNodeType.STATEMENT);
    }

}