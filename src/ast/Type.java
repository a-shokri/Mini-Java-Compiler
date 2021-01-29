package ast;

import lexer.CommonConstants;

public class Type extends Tree {

    public enum TypeName {
        INT, BOOLEAN, STRING, INT_ARRAY, IDENTIFIER, STRING_ARR, VOID
    }

    public TypeName typeName;
    public Identifier identifier = null;

    public Type(TypeName typeName) {

        super(CommonConstants.AstNodeType.TYPE);
        this.typeName = typeName;

    }

    public Type(Identifier identifier) {

        super(CommonConstants.AstNodeType.IDENTIFIER);

        if (identifier != null)
            identifier.setParent(this);

        this.typeName = TypeName.IDENTIFIER;
        this.identifier = identifier;

    }


    @Override
    public int getLine() {
        return identifier != null ? identifier.getLine() : super.getLine();
    }
    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}
