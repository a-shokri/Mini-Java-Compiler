package ast;

public class IfThenElse extends Statement {

    public Expression expr;
    public Statement then;
    public Statement elze;  // we cannot use "else" here since it is a reserved word

    public IfThenElse(Expression expr, Statement then, Statement elze) {

        super();
        if (expr != null)
            expr.setParent(this);
        if (then != null)
            then.setParent(this);
        if (elze != null)
            elze.setParent(this);
        this.expr = expr;
        this.then = then;
        this.elze = elze;

    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public Statement getThen() {
        return then;
    }

    public void setThen(Statement then) {
        this.then = then;
    }

    public Statement getElze() {
        return elze;
    }

    public void setElze(Statement elze) {
        this.elze = elze;
    }

    @Override
    public int getLine() {
        return getExpr().getLine();
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}