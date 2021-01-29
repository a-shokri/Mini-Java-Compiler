package ast;

import lexer.CommonConstants;

import java.util.*;

public class Y extends Tree {


    public enum TYPE {
        LBR_EXP_RBR_Y, DOT_LENGTH_Y, DOT_IDENTIFIER_EXPRESSIONLIST_Y
    }

    public List<Object> content = new ArrayList<>();
    public TYPE type = null;

    public Y(TYPE type, Object... arg) {

        super(CommonConstants.AstNodeType.Q);
        this.type = type;

        switch (type) {

            case LBR_EXP_RBR_Y:
                content.add(arg[0]); // Exp
                content.add(arg[1]); // Y
                break;

            case DOT_LENGTH_Y:
                break;

            case DOT_IDENTIFIER_EXPRESSIONLIST_Y:
                content = Arrays.asList(arg);
                break;

        }

        if (content != null)
            for (Object object : content)
                if (object instanceof Tree)
                    ((Tree) object).setParent(this);
                else if (object instanceof List) {

                    for (Object object2 : (List) object)
                        if (object2 instanceof Tree)
                            ((Tree) object2).setParent(this);

                }

    }

    public TYPE getType() {
        return type;
    }

    @Override
    public int getLine() {
        return content != null && content.size() > 0 ? ((Tree) content.get(0)).getLine() : super.getLine();
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString() {
        String str = "";
        switch (type) {
            case DOT_IDENTIFIER_EXPRESSIONLIST_Y:
                str = "." + content.get(0).toString() + "(";
                if (content.size() > 1 && content.get(1) != null ) {
                    if (content.get(1) instanceof List) {
                        String args = "";
                        for (Expression e : (List<Expression>) content.get(1))
                            args += (args.length() > 0 ? "," : "") + e.toString();
                        str += args;
                    }
                }
                str += ")";
                if (content.size() > 2 && content.get(2) != null )
                    str += content.get(2).toString();
                break;
            case DOT_LENGTH_Y:
                str = ".length" + (content.size() > 0 ? content.get(0).toString() : "");
                break;
            case LBR_EXP_RBR_Y:
                str = "[" + (!content.isEmpty() ? content.get(0).toString() : "") + "]";
                str += (content.size() > 1 && content.get(1) != null ? content.get(1) : "");
                break;
        }
        return str;
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();

        switch (type) {
            case DOT_IDENTIFIER_EXPRESSIONLIST_Y:
                if (content.size() > 1 && content.get(1) != null ) {
                    if (content.get(1) instanceof List) {
                        for (Expression e : (List<Expression>) content.get(1))
                            usedIdList.addAll( e.getUsedIdentifierList() );
                    }
                }
                if (content.size() > 2 && content.get(2) != null )
                    usedIdList.addAll( ((Y) content.get(2) ).getUsedIdentifierList());
                break;
            case DOT_LENGTH_Y:
                if (content.size() > 0 && content.get(0) != null )
                    usedIdList.addAll( ((Y) content.get(0) ).getUsedIdentifierList());
                break;
            case LBR_EXP_RBR_Y:
                usedIdList.addAll( ((Expression) content.get(0) ).getUsedIdentifierList());
                if (content.size() > 1 && content.get(1) != null )
                    usedIdList.addAll( ((Y) content.get(1) ).getUsedIdentifierList());
                break;
        }



        return usedIdList;
    }
}
