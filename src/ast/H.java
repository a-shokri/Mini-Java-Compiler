package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// H -> Q J
public class H extends Tree {

    public NewQ q;
    public J j;

    public H(NewQ q, J j) {
        super(CommonConstants.AstNodeType.H);
        if (q != null)
            q.setParent(this);
        if (j != null)
            j.setParent(this);
        this.q = q;
        this.j = j;
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
        return q.toString() + (j != null ? j.toString() : "" );
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
