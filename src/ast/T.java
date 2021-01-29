package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// T -> F B
public class T extends Tree {

    public F f;
    public B b;

    public T(F f, B b) {

        super(CommonConstants.AstNodeType.T);

        if (f != null)
            f.setParent(this);

        if (b != null)
            b.setParent(this);

        this.f = f;
        this.b = b;

    }

    @Override
    public int getLine() {
        return f.getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return f.toString() + (b != null ? b.toString() : "" );
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        if( f != null )
            usedIdList.addAll( f.getUsedIdentifierList() );
        if( b != null )
            usedIdList.addAll( b.getUsedIdentifierList() );
        return usedIdList;
    }

}
