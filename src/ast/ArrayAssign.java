package ast;

public class ArrayAssign extends Statement {

    public Identifier identifier;
    public Expression index;
    public Expression value;

    public ArrayAssign(Identifier identifier, Expression size, Expression value) {

        super();
        if (identifier != null)
            identifier.setParent(this);
        if (size != null)
            size.setParent(this);
        if (value != null)
            value.setParent(this)
                    ;
        this.identifier = identifier;
        this.index = size;
        this.value = value;

    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression expression) {
        this.index = expression;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public int getLine() {
        return getIdentifier().getLine();
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return identifier.idName + "[" + index.toString() + "] = " + value.toString();
    }

}