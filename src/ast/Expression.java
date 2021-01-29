package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

public class Expression extends Tree {

    public T t;
    public A a;

    public Expression(T t, A a) {

        super(CommonConstants.AstNodeType.EXPRESSION);
        if (a != null)
            a.setParent(this);
        if (t != null)
            t.setParent(this);
        this.a = a;
        this.t = t;

    }


    @Override
    public int getLine() {
        return t.getLine();
    }
    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        return a == null ? t.toString() : t.toString() + a.toString();
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        if( t != null )
            usedIdList.addAll( t.getUsedIdentifierList() );
        if( a != null )
            usedIdList.addAll( a.getUsedIdentifierList() );
        return usedIdList;
    }

}
