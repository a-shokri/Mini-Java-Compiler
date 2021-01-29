package ast;

public class Print extends Statement {

    public Expression expression;

    public Print(Expression expression) {

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

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return "System.out.println(" + expression.toString() + ")";
    }


}