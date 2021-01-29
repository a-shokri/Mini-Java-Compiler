package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// J-> * Q J | / Q J | epsilon
public class J extends Tree {

    public enum OperatorType {MULTIPLY, DIVISION}

    public NewQ q;
    public J j;
    public OperatorType o;

    public J(NewQ q, J j, OperatorType o) {

        super(CommonConstants.AstNodeType.J);
        if (q != null)
            q.setParent(this);
        if (j != null)
            j.setParent(this);
        this.q = q;
        this.j = j;
        this.o = o;

    }

    public OperatorType getO() {
        return o;
    }
    @Override
    public int getLine() {
        return q.getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        String opr = "";
        switch ( o ){
            case MULTIPLY:
                opr = " * ";
                break;
            case DIVISION:
                opr = " / ";
                break;
        }
        return opr + q.toString() + (j != null ? j.toString() : "" );
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        if( q != null )
            usedIdList.addAll( q.getUsedIdentifierList() );
        if( j != null )
            usedIdList.addAll( j.getUsedIdentifierList() );
        return usedIdList;
    }
}
