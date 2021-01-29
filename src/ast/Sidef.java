package ast;


public class Sidef extends Statement {

    public Expression expression;

    public Sidef(Expression expression) {

        super();

        if (expression != null)
            expression.setParent(this);

        this.expression = expression;
        
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public int getLine() {
        return expression.getLine();
    }

    public <R> R accept(Visitor<R> paramVisitor) {
        return paramVisitor.visit(this);
    }

    public String toString(){
        return "sidef(" + expression.toString() + ")";
    }

}
