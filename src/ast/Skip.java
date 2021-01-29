package ast;

/* Skip represents empty statement.
 * It is useful e.g. as "else" block of and if-then-else statement
 */
public class Skip extends Statement {

    public Skip() {
        super();
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

}