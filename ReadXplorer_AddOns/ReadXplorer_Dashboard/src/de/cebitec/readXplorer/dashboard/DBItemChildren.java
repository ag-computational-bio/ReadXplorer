package de.cebitec.readXplorer.dashboard;

import de.cebitec.readXplorer.view.dialogMenus.explorer.StandardItemChildren;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * Children container for DBItems. 
 *
 * @author jeff
 */
public class DBItemChildren extends StandardItemChildren {
    
    private List<DBItem> items;
    
    /**
     * Children container for DBItems. 
     * @param items the list of items to store as children
     */
    public DBItemChildren(List<DBItem> items) {
        this.items = items;
    }
    
    
    @Override
    protected List<Node> initCollection() {
        List<Node> childrenNodes = new ArrayList<>();
        for (DBItem item : this.items) {
            try {
                childrenNodes.add(new DBItemNode(item));
            } catch (IntrospectionException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        return childrenNodes;
    }
}