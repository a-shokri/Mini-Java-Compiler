package optimizer;

import analyser.NameAnalyzer;
import ast.*;
import jas.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeadVariableEliminator {
    public static List<Statement> applyOprimizationComputationOnEdges( ControlFlowGraphNode methodStartNode){
        List<Statement> statementList = new ArrayList<>();
        boolean anyChanges = true;
        while( anyChanges ) {
            anyChanges = false;
            for (ControlFlowGraphNode.ControlFlowGraphEdge edge : methodStartNode.getAllEdgesInTheGraph())
                anyChanges = anyChanges || edge.computeInIdList();

        }

        for (ControlFlowGraphNode.ControlFlowGraphEdge edge : methodStartNode.getAllEdgesInTheGraph()){
            Set<String> defIdList = edge.computeDefIdList();
            Set<String> outIdList = edge.outIdList;

            if( !outIdList.containsAll( defIdList ) )
                statementList.add( edge.assignmentStatement );
        }


        return statementList;
    }

    public static List<Statement> optimizeTheMethodDeclaration( MethodDeclaration methodDeclaration, ControlFlowGraphNode cfgNode ){
        List<Statement> toBeEliminatedStatements = DeadVariableEliminator.applyOprimizationComputationOnEdges(cfgNode);
        for (int j = 0; j < toBeEliminatedStatements.size(); j++) {
//                removeStatement( methodDeclaration.statements, toBeEliminatedStatements.get(j) );
            removeStatementFromAST( toBeEliminatedStatements.get(j) );
        }
        return toBeEliminatedStatements;

    }

    private static void removeStatementFromAST( Statement toBeEliminatedStatement ){
            Tree parent = toBeEliminatedStatement.getParent();
            if( parent instanceof MethodDeclaration ){
                for( int i = 0; i < ((MethodDeclaration) parent).statements.size(); i++ )
                    if (twoStatementsAreTheSame(toBeEliminatedStatement, ((MethodDeclaration) parent).statements.get(i))) {
                        ((MethodDeclaration) parent).statements.remove(i);
                        return;
                    }
            }
            else if( parent instanceof While ){
                if( twoStatementsAreTheSame(toBeEliminatedStatement, ((While) parent).body) ) {
                    ((While) parent).body = null;
                    return;
                }
            }
            else if( parent instanceof IfThenElse ){
                if( twoStatementsAreTheSame(toBeEliminatedStatement, ((IfThenElse) parent).then) ) {
                    ((IfThenElse) parent).then = null;
                    return;
                }
                else
                if( ((IfThenElse) parent).elze != null && twoStatementsAreTheSame(toBeEliminatedStatement, ((IfThenElse) parent).elze) ) {
                    ((IfThenElse) parent).elze = null;
                    return;
                }
            }
            else if( parent instanceof Block ) {
                if (((Block) parent).singleStatement != null && twoStatementsAreTheSame(toBeEliminatedStatement, ((Block) parent).singleStatement)) {
                    ((Block) parent).singleStatement = null;
                    return;
                }
                if (((Block) parent).body != null) {
                    for (int i = 0; i < ((Block) parent).body.size(); i++)
                        if (twoStatementsAreTheSame(toBeEliminatedStatement, ((Block) parent).body.get(i))) {
                            ((Block) parent).body.remove(i);
                            return;
                        }
                }
            }

    }
    private static boolean twoStatementsAreTheSame( Statement currentStatement, Statement toBeEliminatedStatement ){
        if( currentStatement instanceof Assign  ){
            if( toBeEliminatedStatement instanceof Assign ){
                Assign asnToBeEliminatedStatement = (Assign) toBeEliminatedStatement;
                Assign asnCurrentStatement = (Assign) currentStatement;
                if( asnCurrentStatement.identifier.getLine() == asnToBeEliminatedStatement.identifier.getLine() &&
                        asnCurrentStatement.identifier.getColumn() == asnToBeEliminatedStatement.identifier.getColumn() )
                    return true;
                }
            }

        else if( currentStatement instanceof ArrayAssign ){
            ArrayAssign asnToBeEliminatedStatement = (ArrayAssign) toBeEliminatedStatement;
            ArrayAssign asnCurrentStatement = (ArrayAssign) currentStatement;
            if( asnCurrentStatement.identifier.getLine() == asnToBeEliminatedStatement.identifier.getLine() &&
                    asnCurrentStatement.identifier.getColumn() == asnToBeEliminatedStatement.identifier.getColumn() )
                return true;
        }
        return false;
    }

/*    private static void removeStatement( List<Statement> parentStatementList, Statement toBeEliminatedStatement ) {
        for (int k = 0; k < parentStatementList.size(); k++) {
            Statement currentStatement = parentStatementList.get(k);
            if (currentStatement instanceof Assign || currentStatement instanceof ArrayAssign) {
                if (twoStatementsAreTheSame(currentStatement, toBeEliminatedStatement)) {
                    parentStatementList.remove(k);
                    k = 0;
                    continue;
                }
            } else if (currentStatement instanceof Block) {
                Block block = (Block) currentStatement;
                if( block.singleStatement != null ) {
                    if (twoStatementsAreTheSame(block.singleStatement, toBeEliminatedStatement)) {
                        block.singleStatement = null;
                        k = 0;
                        continue;
                    } else if( block.singleStatement instanceof Block ){
                        // TODO: what do we do here?
                    }
                }
                else if ( block.body != null && block.body.size() > 0 ){
                    removeStatement( block.body, toBeEliminatedStatement );
                }
            }

             else if (currentStatement instanceof IfThenElse) {
                IfThenElse ite = (IfThenElse) currentStatement;
                removeStatement( ite.then );

            } else if (currentStatement instanceof While) {
                While w = (While) currentStatement;
                w.

            }

            if (methodDeclaration.statements.get(k).equals(toBeEliminatedStatements.get(j))) {
                methodDeclaration.statements.remove(k);
                toBeEliminatedStatements.remove(j);

                j = 0;
                break;
            }

        }
    }*/
}
