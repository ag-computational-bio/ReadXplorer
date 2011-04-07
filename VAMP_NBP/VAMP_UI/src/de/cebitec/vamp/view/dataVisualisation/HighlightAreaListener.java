/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.view.dataVisualisation;

import de.cebitec.vamp.util.Parser;
import de.cebitec.vamp.util.SequenceUtils;
import de.cebitec.vamp.util.fileChooser.FastaFileChooser;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.SequenceBar;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Listener for highlighting areas. For example the dna sequence on the sequence bar.
 * Note that classes with a HighlightAreaListener have to implement IHighlightable
 *
 * @author Rolf Hilker
 */
public class HighlightAreaListener extends MouseAdapter implements ClipboardOwner {

    private static final int HEIGHT = 12;

    private final SequenceBar parentComponent;
    private final int baseLineY;
    private final int offsetY;
    private AbstractViewer grandparentViewer;
    private int startX;
    private boolean keepPainted;
    private boolean freezeRect;
    private Rectangle highlightRect;
    private boolean fwdStrand;

    private int seqStart;
    private int seqEnd;

    /**
     * @param graphic the graphics object to paint on
     * @param offsetY the y offset from the middle, which determines where to start painting the
     * highlighting rectangle
     */
    public HighlightAreaListener(final SequenceBar parentComponent, final int baseLineY,
                final int offsetY, final AbstractViewer grandparentViewer){
        this.parentComponent = parentComponent;
        this.baseLineY = baseLineY;
        this.offsetY = offsetY;
        this.grandparentViewer = grandparentViewer;
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
            double baseWidth = this.grandparentViewer.getBaseWidth();
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
            double baseWidth = this.grandparentViewer.getBaseWidth();
            int x = (int) (Math.round(e.getX()/baseWidth)*baseWidth);
            int xPos = x <= this.startX ? x : this.startX;
            int yPos = this.baseLineY - 7;
            yPos = e.getY() <= this.baseLineY ? yPos - this.offsetY : yPos + this.offsetY;

            this.setHighlightRectangle(new Rectangle(xPos, yPos, Math.abs(x - this.startX), HEIGHT));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e){
        for (MouseMotionListener mml : grandparentViewer.getMouseMotionListeners()){
            mml.mouseMoved(e);
            parentComponent.setToolTipText(grandparentViewer.getToolTipText());
        }
    }

    /**
     * Opens the pop up menu showing all available options for the highlighted
     * rectangle.
     * @param e method to be called after a click, so this is the mouse event resulting from that click
     */
    private void showPopUp(MouseEvent e) {

        if ((e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger())) {
            JPopupMenu popUp = new JPopupMenu();

            //add copy option
            JMenuItem copyItem = new JMenuItem(NbBundle.getMessage(HighlightAreaListener.class, "HighlightListener_Copy"));
            copyItem.addActionListener(new ActionListener() {

                @Override//
                public void actionPerformed(ActionEvent e) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(HighlightAreaListener.this.getMarkedSequence()), HighlightAreaListener.this);
                }
            });
            popUp.add(copyItem);

            //add store as fasta file option
            JMenuItem storeItem = new JMenuItem(NbBundle.getMessage(HighlightAreaListener.class, "HLA_StoreFasta"));
            storeItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String output = this.generateFastaFromFeature();
                    FastaFileChooser storeFastaFileChoser = new FastaFileChooser("fasta", output);
                }

                /**
                 * Generates a string ready for output in a fasta file.
                 */
                private String generateFastaFromFeature() {
                    String selSequence = HighlightAreaListener.this.getMarkedSequence();
                    String header = "Copied sequence from:".concat(String.valueOf(seqStart)).concat(" to ")
                            .concat(String.valueOf(seqEnd));
                    return Parser.generateFasta(selSequence, header);
                }
            });
            popUp.add(storeItem);

            //add calculate secondary structure option
            JMenuItem calcItem = new JMenuItem(NbBundle.getMessage(HighlightAreaListener.class, "HighlightListener_SecondaryStruct"));
            calcItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String selSequence = HighlightAreaListener.this.getMarkedSequence();//TODO: generate output from calling a rna folder
                    //http://bibiserv.techfak.uni-bielefeld.de/rnafold/submission.html
                    //über webservice ansprechen & online ergebnis abrufen + grafische darstellung in vamp anschließen
                    //würde dafür kleines extra tab oder fenster öffnen (tab wohl sinnvoller)
                    //über webservice ansprechen & online ergebnis abrufen + grafische darstellung in vamp anschließen
                    //würde dafür kleines extra tab oder fenster öffnen (tab wohl sinnvoller)
                    try {
                        String foldedSequence = HighlightAreaListener.this.callRNAFolder(selSequence); //TODO: find better place for method
                        System.out.println(foldedSequence);

                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex); //TODO: correct exception
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            popUp.add(calcItem);

            popUp.show((JComponent) HighlightAreaListener.this.parentComponent, e.getX(), e.getY());
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //do nothing
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
        BoundsInfo bounds = grandparentViewer.getBoundsInfo();
        final double baseWidth = grandparentViewer.getBaseWidth();
        final String seq = parentComponent.getPersistantReference().getSequence();
        int logleft = bounds.getLogLeft() - 1 + (int) (Math.round((highlightRect.x - grandparentViewer.getHorizontalMargin()) / baseWidth));
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
     * Calls http://bibiserv.techfak.uni-bielefeld.de/rnafold/submission.html with the given string
     * and returns the result string of the program.
     * @param selSequence the sequence to start RNA folder with
     * @throws MalformedURLException
     */
    @SuppressWarnings("SleepWhileHoldingLock")
    private String callRNAFolder(String selSequence) throws MalformedURLException, IOException {

        selSequence = ">vampSeq\r\n".concat(selSequence);

        try {
            /* declare addresslocation for service */
            final String server = "http://bibiwsserv.techfak.uni-bielefeld.de";
            /* declare where to find the describing WSDL */
            final URL wsdl = new URL("http://bibiserv.techfak.uni-bielefeld.de/wsdl/RNAfold.wsdl");


            if (selSequence.isEmpty() || selSequence == null) {
                System.err.println("java RNAfoldCOrig -F <FastaFile> [-T <double>] \n"); //return popup with msg
            }

            /* prepare the call (the same for all called methods) */
            Service ser = new Service(wsdl, new QName(
                    server + "/RNAfold/axis/RNAfoldPort", "RNAfoldImplementationService"));
            Call call = (Call) ser.createCall(new QName("RNAfoldPort"), "request_orig");
            /* call and get id */
            String id = (String) call.invoke(new Object[] {selSequence}); //new Object[] {"T", 37.0},
            /* print id on STDOUT */
            System.err.println("get id - '" + id + "'");
            int statuscode = 601;
            while ((statuscode > 600) && (statuscode < 700)) {
                try {
                    Thread.sleep(2500);
                    call = (Call) ser.createCall(new QName("RNAfoldPort"), "response_orig");
                    // call and get result as DOM Tree(if finished)
                    return (String) call.invoke(new Object[]{id});

                } catch (InterruptedException e) {
                    System.err.println("process can't sleep!");
                } catch (RemoteException e) {
                    // on error WS will throw a soapfault as hobitstatuscode
                    Element root = ((AxisFault) e).lookupFaultDetail(new QName(
                            "http://hobit.sourceforge.net/xsds/hobitStatuscode.xsd", "hobitStatuscode"));
                    if (root == null) {
                        System.err.println("ws remote error (no Hobitstatuscode): " + e.toString());
                    }
                    String description = root.getLastChild().getFirstChild().getNodeValue();
                    statuscode = Integer.parseInt(root.getFirstChild().getFirstChild().getNodeValue());
                    // print Statusinformation to STDERR
                    System.err.println("(" + statuscode + " - " + description + ")");
                }
            }

            /* error handling with proper information for the user */

        } catch (RemoteException e) {
            /* on error WS will throw a soapfault as hobitstatuscode */
            Element root = ((AxisFault) e).lookupFaultDetail(new QName(
                    "http://hobit.sourceforge.net/xsds/hobitStatuscode.xsd", "hobitStatuscode"));
            if (root == null) {
                System.err.println("ws remote error (no Hobitstatuscode): " + e.toString());
            } else {
                String description = root.getLastChild().getFirstChild().getNodeValue();
                String code = root.getFirstChild().getFirstChild().getNodeValue();
                System.out.println("Statuscode:  " + code);
                System.out.println("Description: " + description);
            }

            /*
             * Using this kind of Webservice there is only one one field for
             * returning an error message. When an axception occours, the
             * client side of Axis will throw a RemoteException which includes
             * the class name of the thrown exception. There is no way to get
             * more information, like the original stacktrace !!!
             */
        } catch (MalformedURLException e) {
            System.err.println("failed (" + e.toString() + ")");
        } catch (ServiceException e) {
            System.err.println("Service unavailable (" + e.toString() + ")");
        } catch (IOException e) {
            System.err.println("can't read sequence");
        }

        return "didn't work";
    }
}
