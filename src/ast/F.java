package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// F -> G C
public class F extends Tree {

    public G g;
    public C c;

    public F(G g, C c) {
        super(CommonConstants.AstNodeType.F);
        if (g != null)
            g.setParent(this);
        if (c != null)
            c.setParent(this);
        this.g = g;
        this.c = c;
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
        return g.toString() + (c != null ? c.toString() : "" );
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
