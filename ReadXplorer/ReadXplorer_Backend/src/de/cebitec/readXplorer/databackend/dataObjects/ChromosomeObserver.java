package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.util.Observer;

/**
 * An observer for persistant chromosomes.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class ChromosomeObserver implements Observer {

    @Override
    public void update(Object args) {
        //nothing to do, we just want to inform the chromosome, that we are observing it
    }
    
}
