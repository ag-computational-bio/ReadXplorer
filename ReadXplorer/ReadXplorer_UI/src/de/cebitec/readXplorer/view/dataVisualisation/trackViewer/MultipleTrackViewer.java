package de.cebitec.readXplorer.view.dataVisualisation.trackViewer;

import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;

/**
 *
 * @author jwinneba
 */
public class MultipleTrackViewer extends TrackViewer {

    private static final long serialVersionUID = 2L;

    public MultipleTrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, 
            PersistantReference refGen, TrackConnector trackCon, boolean combineTracks) {
        super(boundsManager, basePanel, refGen, trackCon, combineTracks);
    }
    
    

}
