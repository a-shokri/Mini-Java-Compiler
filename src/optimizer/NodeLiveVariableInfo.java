package optimizer;

import ast.Identifier;

import java.util.Set;

public class NodeLiveVariableInfo {
    Set<Identifier> usedIdentifiers;
    Set<Identifier> definedIdentifiers;

    public NodeLiveVariableInfo(  Set<Identifier> usedIdentifiers, Set<Identifier> definedIdentifiers ){
        this.usedIdentifiers = usedIdentifiers;
        this.definedIdentifiers = definedIdentifiers;
    }

    public void useIdentifier( Identifier identifier ){
        usedIdentifiers.add( identifier );
    }

    public void defineIdentifier( Identifier identifier ){
        definedIdentifiers.add( identifier );
    }


}
