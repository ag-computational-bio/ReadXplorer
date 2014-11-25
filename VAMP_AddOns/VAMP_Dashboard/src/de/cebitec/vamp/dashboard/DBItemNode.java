package de.cebitec.vamp.dashboard;

import de.cebitec.vamp.view.dialogMenus.explorer.StandardNode;
import java.beans.IntrospectionException;
import javax.swing.Action;

/**
 * A DBItemNode represents a BeanNode of a DBItem, which is designed for a track 
 * or a genome.
 *
 * @author jeff
 */
public class DBItemNode extends StandardNode {
        
    /**
     * A DBItemNode represents a BeanNode of a DBItem, which is designed for a
     * track or a genome.
     * @param bean the DBItem to associate with this node
     * @param children the children of the given <cc>bean</cc>
     * @throws IntrospectionException 
     */
    public DBItemNode(DBItem bean, DBItemChildren children) throws IntrospectionException {
        super(bean, children);
        setDisplayName(bean.getTitle());
        setShortDescription(bean.getDescription());
        setIconBaseWithExtension("de/cebitec/vamp/ui/visualisation/refOpen.png");
    }
    
    /**
     * A DBItemNode represents a BeanNode of a DBItem, which is designed for a
     * track or a genome.
     * @param bean the DBItem to associate with this node
     * @throws IntrospectionException
     */
    public DBItemNode(DBItem bean) throws IntrospectionException {
        super(bean);
        setDisplayName(bean.getTitle());
        setShortDescription(bean.getDescription());
        setIconBaseWithExtension("de/cebitec/vamp/ui/visualisation/trackOpen.png");
        
        
    //public DBItemNode(DBItem bean, Children children, Lookup lkp) throws IntrospectionException {
        /*if (children==null) {
            super(bean);
        }
        else {
            super(bean, children);
        }*/
        
        
        //super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        //this.movie = key;
        //setDisplayName(key.getTitle());
        
    }
    
    @Override
    public boolean canCut() {
        return true;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public Action[] getActions(boolean popup) {
        return new Action[] {
            /*SystemAction.get( CopyAction.class ),
            SystemAction.get( CutAction.class ),
            null,
            SystemAction.get( DeleteAction.class ) */
        };
    }    
}
