package temp;

import temp.CategoryNode;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author jeff
 */
public class GenomeChildren { //extends Children.Keys {
    
    /*private String[] Categories = new String[]{
        "Reference Genome",
        "Track"
    };*/
    
    private PersistantTrack[] tracks;
    
    public GenomeChildren(PersistantTrack[] tracks) {
        this.tracks = tracks;
    }
    
    protected Node[] createNodes(Object key) {
        Category obj = (Category) key;
        return new Node[] { new CategoryNode( obj ) };
    }
    
    protected void addNotify() {
        //super.addNotify();
        //Category[] objs = {new Category("Reference Genome"), new Category("Track")};
        /*for (int i = 0; i < objs.length; i++) {
            Category cat = new Category();
            cat.setName(Categories[i]);
            objs[i] = cat;
        }*/
        
        //this.setKeys(this.tracks);
    }
    
}