package de.cebitec.vamp.util.polyTree;

/**
 * Node visitor for resetting the visited flag for the visited nodes after a traversal.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ResetVisitedVisitor implements NodeVisitor {

    /**
     * Node visitor for resetting the visited flag for the visited nodes after a traversal.
     */
    public ResetVisitedVisitor() {
    }
    
    @Override
    public void visit(Node node) {
        node.setVisited(false);
    }
    
}
