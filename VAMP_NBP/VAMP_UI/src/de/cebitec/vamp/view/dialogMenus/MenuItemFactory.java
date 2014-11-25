package de.cebitec.vamp.view.dialogMenus;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.parser.output.OutputParser;
import de.cebitec.vamp.util.fileChooser.FastaFileChooser;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.vamp.view.dataVisualisation.abstractViewer.Region;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;

/**
 * Factory for different JMenuItems with predefined functionality.
 *
 * @author Rolf Hilker
 */
public class MenuItemFactory extends JMenuItem implements ClipboardOwner {
    
    private static final long serialVersionUID = 1L;
    
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    public MenuItemFactory() {
        //nothing to do here
    }
    
    /**
     * Returns a JMenuItem for copying a sequence.
     * The text to copy has to be known, when the method is called.
     * @param sequenceToCopy the sequence to copy
     * @return the JMenuItem for copying a sequence.
     */
    public JMenuItem getCopyItem(final String sequenceToCopy){

        JMenuItem copyItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.Copy"));
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clipboard.setContents(new StringSelection(sequenceToCopy), MenuItemFactory.this);
            }
        });
        return copyItem;
    }
    
    /**
     * Returns a JMenuItem for copying a position.
     * @param currentPosition the position to copy to the clipboard.
     * @return JMenuItem for copying a position.
     */
    public JMenuItem getCopyPositionItem(final int currentPosition) {
        JMenuItem copyPositionItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.CopyPosition"));
        copyPositionItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clipboard.setContents(new StringSelection(Integer.toString(currentPosition)), MenuItemFactory.this);
            }
        });
        return copyPositionItem;
    }

    /**
     * Returns a JMenuItem for storing a sequence in fasta format.
     * The sequence to store has to be known, when the method is called.
     * @param sequence the sequence to store as fasta
     * @param feature the feature whose sequence is to be converted to fasta
     *                it contains the header information, but not the sequence
     * @return jmenuitem for storing a sequence in fasta format
     */
    public JMenuItem getStoreFastaItem(final String sequence, final PersistantFeature feature){
        return this.initStoreFastaItem(sequence, feature, -1, -1);

    }

    /**
     * Returns a JMenuItem for storing a sequence in fasta format.
     * The sequence to store has to be known, when the method is called.
     * @param sequence the sequence to store as fasta
     * @param seqStart the startpoint of the sequence
     * @param seqStop the endpoint of the sequence
     * @return jmenuitem for storing a sequence in fasta format
     */
    public JMenuItem getStoreFastaItem(final String sequence, final int seqStart, final int seqStop){
        return this.initStoreFastaItem(sequence, null, seqStart, seqStop);
    }

    /**
     * Initializes a store fasta item either from an feature or with given start
     * and stop indices.
     * @param sequence the sequence to store in fasta format
     * @param feature the feature containing the header information, <code>null</code> if not from an feature
     * @param seqStart the startpoint of the sequence (-1 if feature is used!)
     * @param seqEnd the endpoint of the sequence (-1 if feature is used!)
     * @return a menu item capable of storing a sequence in fasta format
     */
    private JMenuItem initStoreFastaItem(final String sequence, final PersistantFeature feature,
            final int seqStart, final int seqEnd) {

        JMenuItem storeFastaItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreFasta"));
        storeFastaItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String output;
                if (feature != null) {
                    output = this.generateFastaFromFeature();
                } else {
                    String header = "Copied sequence from:".concat(String.valueOf(seqStart)).concat(" to ").concat(String.valueOf(seqEnd));
                    output = OutputParser.generateFasta(sequence, header);
                }
                new FastaFileChooser(new String[]{"fasta"}, "fasta", output);
            }

            /**
             * Generates a string ready for output in a fasta file.
             */
            private String generateFastaFromFeature() {
                String ecNumber = feature.getEcNumber() != null ? feature.getEcNumber() : "no EC number";
                String locus = feature.getLocus() != null ? feature.getLocus() : "no locus";
                String product = feature.getProduct() != null ? feature.getProduct() : "no product";

                return OutputParser.generateFasta(sequence, ecNumber, locus, product);
            }
        });
        
        return storeFastaItem;
    }

    /**
     * Returns a JMenuItem for copying one or more CDS sequences.
     * The text to copy has to be known, when the method is called.
     * @param sequencesToStore the CDS sequence(s) to store
     * @param regions the regions to store
     * @param referenceName the name of the reference
     * @return the JMenuItem for copying one or more CDS sequences.
     */
    public JMenuItem getStoreFastaForCdsItem(final List<String> sequencesToStore, final List<Region> regions,
            final String referenceName){

        JMenuItem storeFastaCdsItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreFastaCDS"));
        storeFastaCdsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String output = "";
                for (int i = 0; i < sequencesToStore.size(); ++i) {
                    int length = regions.get(i).getStop() + 1 - regions.get(i).getStart();
                    String header = "Copied CDS sequence from: " 
                            + referenceName 
                            + ", Positions: " + regions.get(i).getStart()
                            + " to " + regions.get(i).getStop()
                            + ", Length: " + length 
                            + "bp, Amino Acids: " + length / 3;
                    output += OutputParser.generateFasta(sequencesToStore.get(i), header);
                }
                new FastaFileChooser(new String[]{"fasta"}, "fasta", output);
            }
        });
        return storeFastaCdsItem;
    }

    /**
     * Returns a JMenuItem for calculating a possible folding of the selected DNA
     * sequence with RNAFold.
     * The sequence to fold has to be known already!
     * @param rnaFolderControl instance of an rnafoldercontroller
     * @param sequenceToFold the DNA/RNA sequence to fold with RNAFold
     * @param header header string used for describing the folded rna
     * @return the menu item for RNA folding
     */
    public JMenuItem getRNAFoldItem(final RNAFolderI rnaFolderControl, final String sequenceToFold, final String header) {

            JMenuItem rNAFoldItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.RNAFold"));
            rNAFoldItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    rnaFolderControl.showRNAFolderView(sequenceToFold, header);
                }
            });

            return rNAFoldItem;
    }
    
    /**
     * @param parentText the JTextComponent whose text is to be copied
     * @return A JMenuItem for copying text from a JTextComponent.
     */
    public JMenuItem getCopyTextfieldItem(final JTextComponent parentText) {
        
        final JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                StringSelection textToCopy = new StringSelection(parentText.getSelectedText());
                clipboard.setContents(textToCopy, MenuItemFactory.this);
            }
        });
        return copyItem;
    }
    
    /**
     * @param parentText the JTextComponent whose text is to be copied
     * @return A JMenuItem for selecting all text from a JTextComponent.
     */
    public JMenuItem getSelectAllItem(final JTextComponent parentText) {
        
        final JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke("ctrl A"));
                
        selectAllItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                parentText.selectAll();
            }
        });
        return selectAllItem;
    }
    
    /**
     * Creates a JMenuItem which updates the navigator bar associated with the given
     * BoundsInfoManager to the stop of the first cdsRegion, when it is pressed.
     * @param boundsManager the bounds info manager, whose navigator bar is to be updated
     * @param cdsRegions the cds regions for which jumping to their stop position should be enabled
     * @return the JMenuItem with the above described functionality
     */
    public JMenuItem getJumpToStopPosItem(final BoundsInfoManager boundsManager, final List<Region> cdsRegions) {
        
        final JMenuItem jumpToStopPosItem = new JMenuItem("Jump to associated stop codon");
        
        jumpToStopPosItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cdsRegions.isEmpty()) {
                    if (cdsRegions.get(0).isForwardStrand()) {
                        boundsManager.navigatorBarUpdated(cdsRegions.get(0).getStop());
                    } else {
                        boundsManager.navigatorBarUpdated(cdsRegions.get(0).getStart());
                    }
                }
            }
        });
        return jumpToStopPosItem;
    }
    
    /**
     * Creates a JMenuItem which centers the navigator bar associated with the
     * given BoundsInfoManager at the new position, when it is pressed.
     * @param boundsManager the bounds info manager, whose navigator bar is to
     * be updated
     * @param pos the position to center
     * @return the JMenuItem with the above described functionality
     */
    public JMenuItem getJumpToPosItem(final BoundsInfoManager boundsManager, final int pos) {

        final JMenuItem jumpToPosItem = new JMenuItem("Center current position");

        jumpToPosItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boundsManager.navigatorBarUpdated(pos);
            }
        });
        return jumpToPosItem;
    }
    
    /**
     * @param parentText the JTextComponent whose text is to be copied
     * @return A JMenuItem for pasting text into a JTextComponent.
     */
    public JMenuItem getPasteItem(final JTextComponent parentText) {
        
        final JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        pasteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                parentText.setText(getClipboardContents());
            }
        });
        return pasteItem;
    }

    /**
     * @param parentText the JTextComponent whose text is to be copied
     * @return A JMenuItem for cutting text from a JTextComponent.
     */
    public JMenuItem getCutItem(final JTextComponent parentText) {
        
        final JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        cutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                StringSelection textToCopy = new StringSelection(parentText.getSelectedText());
                clipboard.setContents(textToCopy, MenuItemFactory.this);
                parentText.replaceSelection("");
            }
        });
        return cutItem;
    }
    
    /**
     * @return Any text found in the clipboard. If none is found, 
     * an empty String is returned.
     */
    public String getClipboardContents() {
        String result = "";
        Transferable contents = clipboard.getContents(null);
        final boolean hasTransferableText = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                JOptionPane.showMessageDialog(this, "Unsupported DataFlavor for clipboard copying.", "Paste Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "IOException occured during recovering of text from clipboard.", "Paste Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }
    

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //do nothing
    }



}
