package optimizer;

import ast.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControlFlowGraphNode {
    static int nodeUniqueNumber = 0;
    List<ControlFlowGraphEdge> incommingEdges = new ArrayList<>();
    List<ControlFlowGraphEdge> outgoingEdges = new ArrayList<>();

    String nodeName = "";
    public ControlFlowGraphNode(){
        nodeUniqueNumber++;
        nodeName = "N" + nodeUniqueNumber;
    }

    public void pinTo( ControlFlowGraphNode node ){
        for( ControlFlowGraphEdge edge : this.outgoingEdges  )
            edge.fromNode = node;
        node.outgoingEdges.addAll( this.outgoingEdges );

        for( ControlFlowGraphEdge edge : this.incommingEdges  )
            edge.toNode = node;
        node.incommingEdges.addAll( this.incommingEdges );
        this.incommingEdges.clear();
        this.outgoingEdges.clear();
    }

/*
    public void pinDownTo( ControlFlowGraphNode node ){
        for( ControlFlowGraphEdge edge : this.incommingEdges  )
            edge.toNode = node;
        node.incommingEdges.addAll( this.incommingEdges );
        node.outgoingEdges.addAll( this.outgoingEdges );
    }
*/


    public void addChild( ControlFlowGraphEdge outGoingEdge ){
        outgoingEdges.add( outGoingEdge );
    }

    public void addParent( ControlFlowGraphEdge outGoingEdge ){
        incommingEdges.add( outGoingEdge );
    }

    public ControlFlowGraphNode getEndNode( Set<String> visitedNodesName ){
        if( visitedNodesName.contains( nodeName ) )
            return null;

        visitedNodesName.add( nodeName );
        if( outgoingEdges == null || outgoingEdges.size() == 0 )
            return this;
        for( int i = 0; i < outgoingEdges.size(); i++ ) {
            ControlFlowGraphNode foundEndNode = outgoingEdges.get(i).toNode.getEndNode(visitedNodesName);
            if ( foundEndNode != null )
                return foundEndNode;
        }
        return null;
    }

    public ControlFlowGraphNode getEndNode(  ){
        HashSet<String> visitedNodeNames = new HashSet<>();
        return getEndNode( visitedNodeNames );

    }


//    public void computeInIdList(){
//        Set<String> inIdList = new HashSet<>();
//        for( ControlFlowGraphEdge outgoingEdge : outgoingEdges )
//            inIdList.addAll( outgoingEdge.inIdList );
//        for ( ControlFlowGraphEdge incomingEdge : incommingEdges ) {
//            boolean inputChanged = incomingEdge.computeInIdList(inIdList);
//            if( inputChanged )
//                incomingEdge.fromNode.computeInIdList();
//        }
//    }

    private List<ControlFlowGraphEdge> getAllEdgesInTheGraph( List<String> visitedNodes){
        if( visitedNodes.contains( this.nodeName ) )
            return new ArrayList<>();
        List<ControlFlowGraphEdge> edgeList = new ArrayList<>();
        edgeList.addAll( outgoingEdges );
        visitedNodes.add( nodeName );
        for( ControlFlowGraphEdge outgoingEdge : outgoingEdges )
            edgeList.addAll( outgoingEdge.toNode.getAllEdgesInTheGraph( visitedNodes ) );
        return edgeList;
    }

    public List<ControlFlowGraphEdge> getAllEdgesInTheGraph(){
        List<String> visitedNodeList = new ArrayList<>();
        return getAllEdgesInTheGraph( visitedNodeList );
    }

    static class ControlFlowGraphEdge{
        ControlFlowGraphNode fromNode;
        ControlFlowGraphNode toNode;

        Expression booleanExp;
        Statement assignmentStatement;
        Expression returnExpression;
        boolean nefBooleanExp = false;

        Set<String> inIdList = new HashSet<>();
        Set<String> outIdList = new HashSet<>();


        // if there are any changes in the input, returns true.
        public boolean computeInIdList(){
            boolean changed = false;
            Set<String> newOutIdList = new HashSet<>();
            if( toNode.outgoingEdges != null )
                for( ControlFlowGraphEdge toNodeOutEdge : toNode.outgoingEdges )
                    newOutIdList.addAll( toNodeOutEdge.inIdList );

            if( !(outIdList.containsAll( newOutIdList ) && newOutIdList.containsAll( outIdList )) ) // there is no changes in outIdList
                changed = true;

            outIdList = newOutIdList;

            HashSet newInIdList = new HashSet<>();
            newInIdList.addAll( outIdList );
            newInIdList.removeAll( computeDefIdList() );
            newInIdList.addAll( computeUseIdList() );
            if( !(inIdList.containsAll( newInIdList ) && newInIdList.containsAll( inIdList )) )
                changed = true;
            inIdList = newInIdList;
            return changed;
        }

        Set<String> computeDefIdList(){
            Set<String> defIdList = new HashSet<>();
            if( booleanExp == null && returnExpression == null ) {
                if (assignmentStatement instanceof Assign) {
                    defIdList.add(((Assign) assignmentStatement).identifier.idName);
                } else if (assignmentStatement instanceof ArrayAssign){
                    defIdList.add(((ArrayAssign) assignmentStatement).identifier.idName);
                }
            }
            return defIdList;
        }

        Set<String> computeUseIdList(){
            Set<String> usedIdList = new HashSet<>();
            if( booleanExp == null && returnExpression == null) {
                if (assignmentStatement instanceof Assign) {
                    usedIdList.addAll(((Assign) assignmentStatement).expression.getUsedIdentifierList());
                } else if (assignmentStatement instanceof ArrayAssign){
                    usedIdList.addAll(((ArrayAssign) assignmentStatement).value.getUsedIdentifierList());
                    usedIdList.addAll(((ArrayAssign) assignmentStatement).index.getUsedIdentifierList());
                }

                else if (assignmentStatement instanceof Sidef){
                    usedIdList.addAll(((Sidef) assignmentStatement).expression.getUsedIdentifierList());
                }
                else if (assignmentStatement instanceof Print){
                    usedIdList.addAll(((Print) assignmentStatement).expression.getUsedIdentifierList());
                }
            }
            else if( returnExpression == null )
                usedIdList.addAll( booleanExp.getUsedIdentifierList() );
            else
                usedIdList.addAll( returnExpression.getUsedIdentifierList() );

            return usedIdList;
        }

        public ControlFlowGraphEdge(ControlFlowGraphNode fromNode, ControlFlowGraphNode toNode, Expression booleanExp, boolean negOfBooleanExp){
            setNodes( fromNode, toNode );
            this.booleanExp = booleanExp;
            this.nefBooleanExp = negOfBooleanExp;
        }

        public ControlFlowGraphEdge(ControlFlowGraphNode fromNode, ControlFlowGraphNode toNode, Expression returnExpression){
            setNodes( fromNode, toNode );
            this.returnExpression = returnExpression;
        }

        public ControlFlowGraphEdge(ControlFlowGraphNode fromNode, ControlFlowGraphNode toNode, Statement assignmentStatement){
            setNodes( fromNode, toNode );
            this.assignmentStatement = assignmentStatement;
        }

        private void setNodes( ControlFlowGraphNode fromNode, ControlFlowGraphNode toNode ){
            this.fromNode = fromNode;
            this.toNode = toNode;

            fromNode.outgoingEdges.add( this );
            toNode.incommingEdges.add( this );

        }

        public String toString(Set<String> visitedNodeNames){
            String str = fromNode.nodeName + " -> " + toNode.nodeName + " [label=\"";
//            str += " in = {" + inIdList.toString() + "}\n";
            if( booleanExp != null )
                str += " (" + (nefBooleanExp ? "!" : "" ) + "[" + booleanExp.toString() + "])";
            if( returnExpression != null )
                str += returnExpression.toString();
            if( assignmentStatement != null )
                str += " " + assignmentStatement.toString();

//            str += "\n out = {" + outIdList.toString() + "}";

            str += "\"];\n";
            return str;
        }

    }

    public String toString( Set<String> visitedNodeNames ){
        String str = "";
        for (ControlFlowGraphEdge edge:outgoingEdges
                ) {
            str += edge.toString(visitedNodeNames);
            visitedNodeNames.add( nodeName );
            if( ! visitedNodeNames.contains( edge.toNode.nodeName ) )
                str += edge.toNode.toString(visitedNodeNames);
        }

        return str;

    }
    public String toString(){
        HashSet<String> set = new HashSet<>();
        return toString( set );
    }

    public boolean equals( Object obj ){
        return ((ControlFlowGraphNode)obj).toString().equals( toString() );
    }

    public int hashCode()
    {
        return 1;
    }

}
