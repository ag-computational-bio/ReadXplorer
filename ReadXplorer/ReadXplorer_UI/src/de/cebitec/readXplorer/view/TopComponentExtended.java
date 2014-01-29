package de.cebitec.readXplorer.view;

import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * A TopComponent, which returns its name when the <cc>toString()</cc> method is invoced.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class TopComponentExtended  extends TopComponent {
    private static final long serialVersionUID = 1L;

    /**
     * A TopComponent, which returns its name when the <cc>toString()</cc> method is invoced.
     */
    public TopComponentExtended() {
    }

    /**
     * A TopComponent, which returns its name when the <cc>toString()</cc> method is invoced.
     * @param lookup the lookup
     */
    public TopComponentExtended(Lookup lookup) {
        super(lookup);
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /**
     * @return The name of this extended TopComponent.
     */
    @Override
    public String toString() {
        return this.getName();
    }
}
