package ast;

import lexer.CommonConstants;

import java.util.HashSet;
import java.util.Set;

// A-> || T A | epsilon
public class A extends Tree {

    public T t;
    public A a;

    public A(T t, A a) {
        super(CommonConstants.AstNodeType.A);
        if (t != null)
            t.setParent(this);
        if (a != null)
            a.setParent(this);
        this.t = t;
        this.a = a;
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
        return " || " + t.toString() + (a == null ? a.toString() : "" );
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
