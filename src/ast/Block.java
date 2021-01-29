package ast;

import java.util.List;

public class Block extends Statement {

    public List<Statement> body;
    public Statement singleStatement;

    public Block(List<Statement> body) {

        super();
        if (body != null)
            for (Statement statement : body)
                statement.setParent(this);
        this.body = body;


    }

    public Block(Statement singleStatement) {
        super();
        if (singleStatement != null)
            singleStatement.setParent(this);
        this.singleStatement = singleStatement;
    }

    public List<Statement> getBody() {
        return body;
    }

    public void setBody(List<Statement> body) {
        this.body = body;
    }

    public int getStatementListSize() {
        return body.size();
    }

    public Statement getStatement(int index) {

        if (index < body.size()) {
            return body.get(index);
        }
        return null;

    }

    public void setStatement(int index, Statement statement) {

        if (index < body.size()) {
            body.set(index, statement);
        }

    }


    public Statement getSingleStatement() {
        return singleStatement;
    }

    @Override
    public int getLine() {

        if (getSingleStatement() != null)
            return getSingleStatement().getLine();

        if (body != null && body.size() > 0)
            return body.get(0).getLine();

        return super.getLine();

    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}