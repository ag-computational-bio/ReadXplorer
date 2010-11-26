package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import javax.swing.JMenuItem;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;

/**
 *
 * @author ddoppmeier
 */
public class TrackItem extends JMenuItem{

        public static final long serialVersionUID = 568792362;
        private PersistantTrack track;

        public TrackItem(PersistantTrack track){
            super("Close \""+track.getDescription()+"\"");
            this.track = track;
        }

        public PersistantTrack getTrack(){
            return track;
        }
    }