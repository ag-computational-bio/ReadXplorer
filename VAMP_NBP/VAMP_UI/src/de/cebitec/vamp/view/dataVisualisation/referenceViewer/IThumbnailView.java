/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import java.awt.event.MouseEvent;
import javax.swing.JTable;

/**
 *
 * @author dkramer
 */
public interface IThumbnailView {
    /**
     * This method is used after selecting a feature for which all tracks for a given reference should be viewed in Thumbnails.
     * @param feature
     * @param refViewer the currently viewed ReferenceViewer
     */
    public void addFeatureToList(PersistantFeature feature,ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer,ViewController con);

    public void removeAllFeatures(ReferenceViewer refViewer);

    public void removeCertainFeatures(PersistantFeature f);

    public void showPopUp(PersistantFeature f, ReferenceViewer refViewer,MouseEvent e);

    public void showTablePopUp(JTable table,ReferenceViewer refViewer,MouseEvent e);

}
