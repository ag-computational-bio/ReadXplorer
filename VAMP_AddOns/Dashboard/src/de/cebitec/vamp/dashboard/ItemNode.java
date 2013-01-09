/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.dashboard;

import java.beans.IntrospectionException;
import javax.swing.Action;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author jeff
 */
public class ItemNode extends BeanNode {
    
    private Item item;
    
    public ItemNode(Item bean, ItemChildren children) throws IntrospectionException {
        super(bean, children);
        setDisplayName(bean.getTitle());
        setShortDescription(bean.getDescription());
        setIconBaseWithExtension("de/cebitec/vamp/ui/visualisation/refOpen.png");
    }
    
    public ItemNode(Item bean) throws IntrospectionException {
        super(bean);
        setDisplayName(bean.getTitle());
        setShortDescription(bean.getDescription());
        setIconBaseWithExtension("de/cebitec/vamp/ui/visualisation/trackOpen.png");
        
        
    //public ItemNode(Item bean, Children children, Lookup lkp) throws IntrospectionException {
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
    
    public boolean canCut() {
        return true;
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public Action[] getActions(boolean popup) {
        return new Action[] {
            /*SystemAction.get( CopyAction.class ),
            SystemAction.get( CutAction.class ),
            null,
            SystemAction.get( DeleteAction.class ) */
        };
    }

    public Item getData() {
        return (Item) this.getBean();
    }
    
}
