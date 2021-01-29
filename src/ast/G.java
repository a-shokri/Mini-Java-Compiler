package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// G -> H D
public class G extends Tree {

    public H h;
    public D d;

    public G(H h, D d) {
        super(CommonConstants.AstNodeType.G);
        if (h != null)
            h.setParent(this);
        if (d != null)
            d.setParent(this);
        this.h = h;
        this.d = d;
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
        return h.toString() + (d != null ? d.toString() : "" );
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
