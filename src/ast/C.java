package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// C-> == G C | < G C | > G C | >= G C | <= G C | epsilon
public class C extends Tree {

    public enum OperatorType {EQUAL, LESS_THAN, GREATER_THAN, LESS_EQ_THAN, GREATER_EQ_THAN}

    public G g;
    public C c;
    public OperatorType o;

    public C(G g, C c, OperatorType o) {

        super(CommonConstants.AstNodeType.C);
        if (g != null)
            g.setParent(this);
        if (c != null)
            c.setParent(this);

        this.g = g;

        this.c = c;
        this.o = o;

    }

    public OperatorType getO() {
        return o;
    }

    @Override
    public int getLine() {
        return g.getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        String opr = "";
        switch ( o ){
            case EQUAL:
                opr = " == ";
                break;
            case LESS_THAN:
                opr = " < ";
                break;
            case GREATER_THAN:
                opr = " > ";
                break;
            case LESS_EQ_THAN:
                opr = " <= ";
                break;
            case GREATER_EQ_THAN:
                opr = " >= ";
                break;
        }
        return opr + g.toString() + (c != null ? c.toString() : "" );
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        if( g != null )
            usedIdList.addAll( g.getUsedIdentifierList() );
        if( c != null )
            usedIdList.addAll( c.getUsedIdentifierList() );
        return usedIdList;
    }
}
