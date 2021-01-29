package ast;

import lexer.CommonConstants;

import java.util.*;

// Q -> <INT_LIT> Y | "<STRING_LIT>" Y | true Y | false Y | Identifer Y | this Y | new Int [ E ] Y |
//      new Identifier() Y | ! E Y | ( E ) Y
//
public class NewQ extends Tree {

    public enum TYPE {
        INT_LIT, STRING_LIT, IDENTIFIER, THIS, TRUE, FALSE, NEW_INT_ARR, NEW_IDENTIFIER,
        NOT_EXP, LPR_EXP_RPR, BOOL_LIT
    }

    public List<Object> content = new ArrayList<>();
    public TYPE type;

    public NewQ(TYPE type, Object... arg) {

        super(CommonConstants.AstNodeType.NEWQ);
        this.type = type;

        if (arg != null && arg.length > 0) {

            content = Arrays.asList(arg);

            for (Object object :
                    content) {
                if (object instanceof Tree)
                    ((Tree) object).setParent(this);
            }

        }

    }


    public TYPE getType() {
        return type;
    }

    @Override
    public int getLine() {

        if (line > 0)
            return line;

        if (content != null && content.get(0) != null)
            return ((Tree) content.get(0)).getLine();

        return super.getLine();

    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    public String toString(){
        String str = "";
        switch ( type ){
            case NEW_IDENTIFIER:
                str = "new " + content.get(0).toString() + "()" + ( content.size() > 1 && content.get(1) != null ? content.get(1).toString() : "" ) ;
                break;
            case THIS:
                str = "this" + ( !content.isEmpty() ? content.get(0).toString() : "" )  ;
                break;
            case FALSE:
                str = "false" + ( !content.isEmpty() ? content.get(0).toString() : "" )  ;;
                break;
            case LPR_EXP_RPR:
                str = "(" + content.get(0).toString() + ")" +  ( content.size() > 1 && content.get(1) != null ? content.get(1).toString() : "" )  ;
                break;
            case NEW_INT_ARR:
                str = "new int[" + (content.size() > 0 ? content.get(0).toString() : "" ) + "]" +  ( content.size() > 1 && content.get(1) != null ? content.get(1).toString() : "" );
                break;
            case NOT_EXP:
                str = "!" + content.get(0).toString() + ( content.size() > 1 && content.get(1) != null ? content.get(1).toString() : "" );
                break;
            case TRUE:
                str = "true" + ( !content.isEmpty() ? content.get(0).toString() : "" )  ;;
                break;
            case INT_LIT: case STRING_LIT: case BOOL_LIT: case IDENTIFIER:
                str = ( !content.isEmpty() ? (type.equals( TYPE.STRING_LIT )? "\\\"" : "") + content.get(0).toString() + (type.equals( TYPE.STRING_LIT )? "\\\"" : "") : "" ) + ( content.size() > 1 && content.get(1) != null ? content.get(1).toString() : "" );
                break;

        }
        return str;
    }

    public Set<String> getUsedIdentifierList(){
        Set usedIdList = new HashSet<>();
        switch ( type ){
            case NEW_IDENTIFIER:
                if( content.size() > 1 && content.get(1) != null )
                    usedIdList.addAll( ((Y) content.get(1)).getUsedIdentifierList() );
                break;
            case LPR_EXP_RPR: case NEW_INT_ARR: case NOT_EXP:
                usedIdList.addAll( ((Expression)content.get(0)).getUsedIdentifierList() );
                if( content.size() > 1 && content.get(1) != null )
                    usedIdList.addAll( ((Y) content.get(1)).getUsedIdentifierList() );
                break;
            case IDENTIFIER:
                if( !( content.size() > 1 && content.get(1) != null ) ||
                        !( (((Y)content.get(1)).type.equals( Y.TYPE.DOT_IDENTIFIER_EXPRESSIONLIST_Y ) ) ||
                                (((Y)content.get(1)).type.equals( Y.TYPE.DOT_LENGTH_Y ) ) )  ) // It is simply an identifier
                    usedIdList.add( content.get(0).toString() );
                if( content.size() > 1 && content.get(1) != null )
                    usedIdList.addAll( ((Y) content.get(1)).getUsedIdentifierList() );

                break;
        }
        return usedIdList;
    }
}
