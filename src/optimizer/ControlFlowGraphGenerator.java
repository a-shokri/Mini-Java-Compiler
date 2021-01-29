package optimizer;

import analyser.ClassSymbol;
import analyser.MethodSymbol;
import analyser.NameAnalyzer;
import ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControlFlowGraphGenerator  extends Tree implements Visitor<ControlFlowGraphNode> {
    NameAnalyzer nameAnalyzer;

    public ControlFlowGraphGenerator( NameAnalyzer nameAnalyzer ){
        this.nameAnalyzer = nameAnalyzer;
    }

    public HashMap<MethodSymbol, ControlFlowGraphNode> generateControlFlowGraphsForMethods(){
        HashMap<MethodSymbol, ControlFlowGraphNode> cfgnMap = new HashMap<>();

        List<ClassSymbol> classSymbolList = new ArrayList<>( nameAnalyzer.getSymbolTable().getClassSymbolList().values() );
        for ( ClassSymbol classSymbol : classSymbolList ){
            if( classSymbol.getMainClass() != null )
                continue;

            for( MethodSymbol methodSymbol : classSymbol.getMethodSymbols() ){
                cfgnMap.put( methodSymbol, visit( methodSymbol.getMethodDeclaration() ) );
            }
        }

        return cfgnMap;
    }
    @Override
    public <R> R accept(Visitor<R> v) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Program n) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Expression n) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(MethodDeclaration methodDeclaration) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode tmp = start;
        for( Statement statement : methodDeclaration.statements ){
            ControlFlowGraphNode startOfStatement = null;
            startOfStatement = visit( statement );
            if( startOfStatement != null ) {
                ControlFlowGraphNode statementEndNode = startOfStatement.getEndNode();
                startOfStatement.pinTo(tmp);
                tmp = statementEndNode;
            }
        }
        if( methodDeclaration.returnExpression != null && methodDeclaration.returnExpression.getUsedIdentifierList() != null &&
                methodDeclaration.returnExpression.getUsedIdentifierList().size() > 0) {

            ControlFlowGraphNode fromNode = new ControlFlowGraphNode();
            ControlFlowGraphNode toNode = new ControlFlowGraphNode();
            ControlFlowGraphNode.ControlFlowGraphEdge edge = new ControlFlowGraphNode.ControlFlowGraphEdge(fromNode, toNode, methodDeclaration.returnExpression);

            fromNode.pinTo( tmp );
        }


        return start;
    }

    @Override
    public ControlFlowGraphNode visit(A a) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(B b) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(C c) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(ClassDeclaration classDeclaration) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(D d) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(F f) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(G g) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(H h) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(J j) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(MainClass mainClass) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(StringLiteral stringLiteral) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(T t) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Type type) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(VarDeclaration varDeclaration) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Identifier identifier) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(IntLiteral intLiteral) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Block block) {
        if( (block.body == null || block.body.isEmpty()) && block.singleStatement == null )
            return null;

        if( block.singleStatement != null )
            return visit( block.singleStatement );
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode tmp = start;
        for( Statement statement : block.body ){
            ControlFlowGraphNode startOfStatement = null;
            startOfStatement = visit( statement );
            ControlFlowGraphNode statementEndNode = startOfStatement.getEndNode();
            startOfStatement.pinTo( tmp );
            tmp = statementEndNode;
        }
        return start;
    }

    @Override
    public ControlFlowGraphNode visit(Skip skip) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(IfThenElse ifThenElse) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode ifExpTrue = new ControlFlowGraphNode();
        ControlFlowGraphNode ifExpFalse = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();
        ControlFlowGraphNode.ControlFlowGraphEdge startToIfExpTrue = new ControlFlowGraphNode.ControlFlowGraphEdge(start, ifExpTrue, ifThenElse.expr, false);
        ControlFlowGraphNode.ControlFlowGraphEdge startToIfExpFalse = new ControlFlowGraphNode.ControlFlowGraphEdge(start, ifExpFalse, ifThenElse.expr, true);


        ControlFlowGraphNode thenStatement = visit(ifThenElse.then);
        ControlFlowGraphNode elseStatement = visit(ifThenElse.elze);

        if (thenStatement != null) {
            ControlFlowGraphNode thenStatementEndNode = thenStatement.getEndNode();
            thenStatement.pinTo( ifExpTrue );
            thenStatementEndNode.pinTo( end );
        }
        else{
            ifExpTrue.pinTo(end);
        }

        if (elseStatement != null) {
            ControlFlowGraphNode elseStatementEndNode = elseStatement.getEndNode();
            elseStatement.pinTo( ifExpFalse );
            elseStatementEndNode.pinTo( end );
        }
        else{
            ifExpFalse.pinTo(end);
        }

        return start;
    }

    @Override
    public ControlFlowGraphNode visit(While aWhile) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode whileExpTrue = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();
        ControlFlowGraphNode.ControlFlowGraphEdge startToWhileExpTrue = new ControlFlowGraphNode.ControlFlowGraphEdge(start, whileExpTrue, aWhile.expr, false);
        ControlFlowGraphNode.ControlFlowGraphEdge startToWhileExpFalse = new ControlFlowGraphNode.ControlFlowGraphEdge(start, end, aWhile.expr, true);


        ControlFlowGraphNode bodyStartNode = visit(aWhile.body);

        if (bodyStartNode != null) {
            ControlFlowGraphNode bodyEndNode = bodyStartNode.getEndNode();
            bodyStartNode.pinTo( whileExpTrue );
            bodyEndNode.pinTo( start );
        }
        else{
            whileExpTrue.pinTo(start);
        }

        return start;
    }

    @Override
    public ControlFlowGraphNode visit(Print print) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();

        ControlFlowGraphNode.ControlFlowGraphEdge edge = new ControlFlowGraphNode.ControlFlowGraphEdge(start, end, print);

        return start;
    }

    @Override
    public ControlFlowGraphNode visit(Assign assign) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();

        ControlFlowGraphNode.ControlFlowGraphEdge edge = new ControlFlowGraphNode.ControlFlowGraphEdge(start, end, assign);

        return start;
    }

    @Override
    public ControlFlowGraphNode visit(ArrayAssign arrayAssign) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();

        ControlFlowGraphNode.ControlFlowGraphEdge edge = new ControlFlowGraphNode.ControlFlowGraphEdge(start, end, arrayAssign);

        return start;
    }

    @Override
    public ControlFlowGraphNode visit(Sidef sidef) {
        ControlFlowGraphNode start = new ControlFlowGraphNode();
        ControlFlowGraphNode end = new ControlFlowGraphNode();

        ControlFlowGraphNode.ControlFlowGraphEdge edge = new ControlFlowGraphNode.ControlFlowGraphEdge(start, end, sidef);

        return start;
    }


    public ControlFlowGraphNode visit(Statement statement) {
        ControlFlowGraphNode node = null;
        if( statement instanceof ArrayAssign )
            node = visit( (ArrayAssign)statement );

        if( statement instanceof Assign )
            node = visit( (Assign) statement );

        if( statement instanceof Block )
            node = visit( (Block) statement );

        if( statement instanceof IfThenElse )
            node = visit( (IfThenElse) statement );

        if( statement instanceof Print )
            node = visit( (Print) statement );

        if( statement instanceof Sidef )
            node = visit( (Sidef) statement );

        if( statement instanceof While )
            node = visit( (While) statement );
        return node;
    }

    @Override
    public ControlFlowGraphNode visit(NewQ newQ) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(Y y) {
        return null;
    }

    @Override
    public ControlFlowGraphNode visit(NameAnalyzer nameAnalyzer) {
        return null;
    }
}
