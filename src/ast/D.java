package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// D-> + H D | - H D | epsilon
public class D extends Tree {

    public enum OperatorType {PLUS, MINUS}

    public H h;
    public D d;
    public OperatorType o;

    public D(H h, D d, OperatorType o) {

        super(CommonConstants.AstNodeType.D);
        if (h != null)
            h.setParent(this);
        if (d != null)
            d.setParent(this);
        this.h = h;
        this.d = d;
        this.o = o;

    }

    public OperatorType getO() {
        return o;
    }

    @Override
    public int getLine() {
        return h.getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        String opr = "";
        switch ( o ){
            case PLUS:
                opr = " + ";
                break;
            case MINUS:
                opr = " - ";
                break;
        }
        return opr + h.toString() + (d != null ? d.toString() : "" );
    }
    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        if( h != null )
            usedIdList.addAll( h.getUsedIdentifierList() );
        if( d != null )
            usedIdList.addAll( d.getUsedIdentifierList() );
        return usedIdList;
    }
}
