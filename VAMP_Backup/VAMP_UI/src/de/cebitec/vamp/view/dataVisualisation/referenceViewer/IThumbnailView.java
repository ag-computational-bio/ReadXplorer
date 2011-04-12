/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;

/**
 *
 * @author dkramer
 */
public interface IThumbnailView {
    
    public void addToList(PersistantFeature feature,ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer,ViewController con);

    public void removeAllFeatures(ReferenceViewer refViewer);

}
