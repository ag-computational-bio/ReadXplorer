/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package temp;

import de.cebitec.vamp.dashboard.ItemChildren;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;

/**
 *
 * @author jeff
 */
public class CategoryNode extends AbstractNode {
    
    /** Creates a new instance of CategoryNode */
    public CategoryNode( Category category ) {
        super(new ItemChildren(null));
        //super( new ItemChildren(category), Lookups.singleton(category) );
        setDisplayName(category.getName());
        setIconBaseWithExtension("org/netbeans/myfirstexplorer/marilyn_category.gif");
    }
    
    /*public PasteType getDropType(Transferable t, final int action, int index) {
        final Node dropNode = NodeTransfer.node( t, 
                DnDConstants.ACTION_COPY_OR_MOVE+NodeTransfer.CLIPBOARD_CUT );
        if( null != dropNode ) {
            final Movie movie = (Movie)dropNode.getLookup().lookup( Movie.class );
            if( null != movie  && !this.equals( dropNode.getParentNode() )) {
                return new PasteType() {
                    public Transferable paste() throws IOException {
                        getChildren().add( new Node[] { new MovieNode(movie) } );
                        if( (action & DnDConstants.ACTION_MOVE) != 0 ) {
                            dropNode.getParentNode().getChildren().remove( new Node[] {dropNode} );
                        }
                        return null;
                    }
                };
            }
        }
        return null;
    }*/
    
    /*public Cookie getCookie(Class clazz) {
        Children ch = getChildren();
        
        if (clazz.isInstance(ch)) {
            return (Cookie) ch;
        }
        
        return super.getCookie(clazz);
    }*/
    
    /*protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        PasteType paste = getDropType( t, DnDConstants.ACTION_COPY, -1 );
        if( null != paste )
            s.add( paste );
    }*/
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            /*SystemAction.get( NewAction.class ),
            SystemAction.get( PasteAction.class )*/ };
    }
    
    public boolean canDestroy() {
        return true;
    }
    
}
