package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import de.cebitec.vamp.databackend.connector.ITrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanel;

/**
 *
 * @author jwinneba
 */
public class MultipleTrackViewer extends TrackViewer{

    private static final long serialVersionUID = 2L;

    public MultipleTrackViewer(BoundsInfoManager boundsManager, BasePanel basePanel, PersistantReference refGen, ITrackConnector trackCon) {
        super(boundsManager, basePanel, refGen, trackCon);
    }

}
