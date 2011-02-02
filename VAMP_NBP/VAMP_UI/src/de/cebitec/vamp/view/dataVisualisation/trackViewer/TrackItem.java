package de.cebitec.vamp.view.dataVisualisation.trackViewer;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import javax.swing.JMenuItem;

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