package de.cebitec.vamp.view.dataVisualisation.referenceViewer;

import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * @author dkramer
 */
public interface IThumbnailView {
    /**
     * This method is used after selecting an annotation for which all tracks for a given reference should be viewed in Thumbnails.
     * @param annotation
     * @param refViewer the currently viewed ReferenceViewer
     */
    public void addAnnotationToList(PersistantAnnotation annotation, ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer);

    public void showThumbnailView(ReferenceViewer refViewer, ViewController con);

    public void removeAllAnnotations(ReferenceViewer refViewer);

    public void removeCertainAnnotation(PersistantAnnotation annotation);

    public void showPopUp(PersistantAnnotation annotation, ReferenceViewer refViewer, MouseEvent e, JPopupMenu popUp);

    public void showTablePopUp(JTable table, ReferenceViewer refViewer, MouseEvent e);

}
