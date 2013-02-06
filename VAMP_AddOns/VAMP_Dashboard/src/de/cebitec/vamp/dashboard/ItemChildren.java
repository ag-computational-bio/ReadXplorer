/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.dashboard;

import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author jeff
 */
public class ItemChildren  extends Index.ArrayChildren {
    
    private List<Item> items;
    
    /*private String[][] items = new String[][]{
        {"0", "Reference Genome", "River of No Return"},
        {"1", "Reference Genome", "All About Eve"},
        {"2", "Reference Genome", "Home Town Story"},
        {"3", "Track", "We're Not Married!"},
        {"4", "Track", "Love Happy"},
        {"5", "Track", "Some Like It Hot"},
        
    };*/
    
    public ItemChildren(List<Item> items) {
        this.items = items;
    }
    
    
    protected java.util.List<Node> initCollection() {
        List childrenNodes = new ArrayList<Node>();
        /*for( int i=0; i<items.length; i++ ) {
            if( category.getName().equals( items[i][1] ) ) {
                Item item = new Item();
                item.setNumber(new Integer(items[i][0]));
                item.setCategory(items[i][1]);
                item.setTitle(items[i][2]);
                childrenNodes.add( new ItemNode( item ) );
            }
        }*/
        for (Item item : this.items.toArray(new Item[0])) {
            
            /*if (o instanceof PersistantTrack) {
                PersistantTrack r = (PersistantTrack) o;
                item = new Item(r);
                
            }
            else if (o instanceof PersistantReference) {
                PersistantReference r = (PersistantReference) o;
                item = new Item(r);
            }*/
            try {
                childrenNodes.add( new ItemNode( item ) );
            } catch (IntrospectionException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        return childrenNodes;
    }
}