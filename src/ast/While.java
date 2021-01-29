package ast;

public class While extends Statement {

    public Expression expr;
    public Statement body;

    public While(Expression expr, Statement body) {

        super();

        if (expr != null)
            expr.setParent(this);

        if (body != null)
            body.setParent(this);

        this.expr = expr;
        this.body = body;

    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public Statement getBody() {
        return body;
    }

    public void setBody(Statement body) {
        this.body = body;
    }

    @Override
    public int getLine() {
        return expr.getLine();
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}