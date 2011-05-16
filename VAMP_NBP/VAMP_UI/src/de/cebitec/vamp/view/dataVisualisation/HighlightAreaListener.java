package de.cebitec.vamp.view.dataVisualisation;

import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.SequenceBar;
import de.cebitec.vamp.view.dialogMenus.MenuItemFactory;
import de.cebitec.vamp.view.dialogMenus.RNAFolderI;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

/**
 * Listener for highlighting areas on a sequence bar.
 * Note that classes with a HighlightAreaListener have to implement IHighlightable
 *
 * @author Rolf Hilker
 */
public class HighlightAreaListener extends MouseAdapter {

    private static final int HEIGHT = 12;

    private final SequenceBar parentComponent;
    private final int baseLineY;
    private final int offsetY;
    private int startX;
    private boolean keepPainted;
    private boolean freezeRect;
    private Rectangle highlightRect;
    private boolean fwdStrand;

    private int seqStart;
    private int seqEnd;

    /**
     * @param parentComponent the component the listener is associated to
     * @param baseLineY the baseline of the vie
     * @param offsetY the y offset from the middle, which determines where to start painting the
     * highlighting rectangle
     */
    public HighlightAreaListener(final SequenceBar parentComponent, final int baseLineY, final int offsetY){
        this.parentComponent = parentComponent;
        this.baseLineY = baseLineY;
        this.offsetY = offsetY;
        this.startX = -1;
        this.keepPainted = false;
        this.freezeRect = false;
        this.fwdStrand = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        boolean inRect = false;
        if (highlightRect != null){
            final int x = e.getX();
            inRect = x > highlightRect.x && x < highlightRect.x + highlightRect.width;
        }

        if (this.keepPainted && !inRect){
            this.keepPainted = false;
            this.freezeRect = false;
            this.setHighlightRectangle(null);
        } else
        if (inRect) {
            this.showPopUp(e);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1){
            this.freezeRect = false;
            this.keepPainted = true;
            double baseWidth = this.parentComponent.getBaseWidth();
            this.startX = (int) (Math.round(e.getX()/baseWidth)*baseWidth);
            int yPos = this.baseLineY - 7;
            this.fwdStrand = e.getY() <= this.baseLineY;
            yPos = this.fwdStrand ? yPos - this.offsetY : yPos + this.offsetY;

            this.setHighlightRectangle(new Rectangle(this.startX, yPos, 2, HEIGHT));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.freezeRect = true;
        if (!this.keepPainted){
            this.setHighlightRectangle(null);
            this.freezeRect = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        /* update rectangle according to new mouse position & start position
         * only x value of mouse event is important! */
        if (!this.freezeRect){
            double baseWidth = this.parentComponent.getBaseWidth();
            int x = (int) (Math.round(e.getX()/baseWidth)*baseWidth);
            int xPos = x <= this.startX ? x : this.startX;
            int yPos = this.baseLineY - 7;
            this.fwdStrand = e.getY() <= this.baseLineY;
            yPos = e.getY() <= this.baseLineY ? yPos - this.offsetY : yPos + this.offsetY;

            this.setHighlightRectangle(new Rectangle(xPos, yPos, Math.abs(x - this.startX), HEIGHT));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e){
        this.parentComponent.updateMouseListeners(e);
    }

    /**
     * Should be called when the bounds of the parent component changed their hook.
     * We don't want the rectangle to remain in that case.
     * TODO: Implement that rectangle moves with bounds.
     */
    public void boundsChangedHook(){
        this.keepPainted = false;
        this.freezeRect = false;
        this.setHighlightRectangle(null);
    }

    /**
     * Opens the pop up menu showing all available options for the highlighted
     * rectangle.
     * @param e method to be called after a click, so this is the mouse event resulting from that click
     */
    private void showPopUp(MouseEvent e) {

        if ((e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger())) {
            JPopupMenu popUp = new JPopupMenu();
            MenuItemFactory menuItemFactory = new MenuItemFactory();

            final String selSequence = HighlightAreaListener.this.getMarkedSequence();
            final String header = HighlightAreaListener.this.getHeader();
            //add copy option
            popUp.add(menuItemFactory.getCopyItem(selSequence));
            //add store as fasta file option
            popUp.add(menuItemFactory.getStoreFastaItem(selSequence, seqStart, seqEnd));
            //add calculate secondary structure option
            final RNAFolderI rnaFolderControl = Lookup.getDefault().lookup(RNAFolderI.class);
            if (rnaFolderControl != null) {
                popUp.add(menuItemFactory.getRNAFoldItem(rnaFolderControl, selSequence, header));
            }
            

            popUp.show((JComponent) e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Sets the current rectangle both in this class and in the parent component.
     * @param rectangle
     */
    private void setHighlightRectangle(final Rectangle rectangle) {
        this.highlightRect = rectangle;
        this.parentComponent.setHighlightRectangle(this.highlightRect);
    }

    /**
     * Returns the highlighted sequence.
     * @return the highlighted sequence
     */
    private String getMarkedSequence() {
        BoundsInfo bounds = parentComponent.getViewerBoundsInfo();
        final double baseWidth = parentComponent.getBaseWidth();
        final String seq = parentComponent.getPersistantReference().getSequence();
        int logleft = bounds.getLogLeft() - 1 +  Math.round((float) ((highlightRect.x - parentComponent.getViewerHorizontalMargin()) / baseWidth));
        int logright = logleft + (int) (Math.round(highlightRect.width / baseWidth));
        logleft = logleft < 0 ? 0 : logleft;
        logright = logright > seq.length() ? seq.length() : logright;
        String selSequence = seq.substring(logleft, logright);
        if (!fwdStrand) {
            selSequence = SequenceUtils.complementDNA(SequenceUtils.reverseString(selSequence));
        }
        this.seqStart = logleft+1;
        this.seqEnd = logright;
        return selSequence;
    }

    /**
     * Creates the header for the highlighted sequence.
     * @return the header for the sequence
     */
    private String getHeader() {
        final String strand = fwdStrand ? ">>" : "<<";
        return this.parentComponent.getPersistantReference().getName()+" ("+strand+" " + seqStart + "-" + seqEnd + ")";
    }
}
