package ast;

public class Assign extends Statement {

    public Identifier identifier;
    public Expression expression;

    public Assign(Identifier identifier, Expression expression) {

        super();
        if (identifier != null)
            identifier.setParent(this);
        if (expression != null)
            expression.setParent(this);

        this.identifier = identifier;
        this.expression = expression;

    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public int getLine() {
        return getIdentifier().getLine();
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return identifier.idName + " = " + expression.toString();
    }

}