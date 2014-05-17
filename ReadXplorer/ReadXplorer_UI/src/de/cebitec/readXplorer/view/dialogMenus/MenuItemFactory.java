/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.parser.output.OutputParser;
import de.cebitec.readXplorer.util.CodonUtilities;
import de.cebitec.readXplorer.util.fileChooser.FastaFileChooser;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.Region;
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
    
    /**
     * Creates a Factory for different JMenuItems with predefined functionality.
     */
    public MenuItemFactory() {
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
     * Returns a JMenuItem for translating and copying a given DNA sequence.
     * @param dnaSeqToTranslateAndCopy the dnaSequence to translate and copy
     * @return The JMenuItem for translating and copying a given DNA sequence.
     */
    public JMenuItem getCopyTranslatedItem(final String dnaSeqToTranslateAndCopy){

        JMenuItem translationItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.Translation"));
        translationItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                 GeneticCode code = CodonUtilities.getGeneticCode();
                 String translatedSequence = code.getTranslationForString(dnaSeqToTranslateAndCopy);
                 clipboard.setContents(new StringSelection(translatedSequence), MenuItemFactory.this);
            }
        });
        return translationItem;
    }
    
    /**
     * Returns a JMenuItem for storing a sequence in fasta format.
     * The sequence to store has to be known, when the method is called.
     * @param sequence the sequence to store as fasta
     * @param refName name of the reference sequence
     * @param feature the feature whose sequence is to be converted to fasta
     *                it contains the header information, but not the sequence
     * @return The JMenuItem for storing a sequence in fasta format
     */
    public JMenuItem getStoreFastaItem(final String sequence, final String refName, final PersistantFeature feature){
        String title = NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreFasta");
        return this.initStoreFastaItem(title, sequence, refName, feature, -1, -1);

    }
    
    /**
     * Returns a JMenuItem for storing the translation of a DNA sequence of a
     * reference feature in fasta format. The sequence to translate has to be
     * known, when the method is called.
     * @param dnaSeqToTranslateAndStore the dna sequence to translate and store
     * @param refName the name of the reference, of which the sequence originates
     * @param feature the reference feature, for which the translation is stored
     * @return The JMenuItem for storing the translation of a DNA sequence of a
     * reference feature in fasta format.
     */
    public JMenuItem getStoreTranslatedFeatureFastaItem(final String dnaSeqToTranslateAndStore,final String refName, final PersistantFeature feature){
         GeneticCode code = CodonUtilities.getGeneticCode();
         String translatedSequence = code.getTranslationForString(dnaSeqToTranslateAndStore);
         String title = NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreTranslatedFasta");
           
         return this.initStoreFastaItem(title,translatedSequence, refName, feature, -1, -1);
    }

    /**
     * Returns a JMenuItem for storing a sequence in fasta format. The sequence
     * to store has to be known, when the method is called.
     * @param dnaSeqToTranslateAndStore the sequence to translate and store as
     * fasta
     * @param refName name of the reference sequence
     * @param seqStart the startpoint of the sequence
     * @param seqStop the endpoint of the sequence
     * @return The JMenuItem  for storing a translated sequence in fasta format
     */
    public JMenuItem getStoreTranslatedFastaItem(final String dnaSeqToTranslateAndStore, final String refName, final int seqStart, final int seqStop){
        GeneticCode code = CodonUtilities.getGeneticCode();
        String translatedSequence = code.getTranslationForString(dnaSeqToTranslateAndStore);
        String title = NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreTranslatedFasta");

        return this.initStoreFastaItem(title, translatedSequence, refName, null, seqStart, seqStop);
    }
    
    /**
     * Returns a JMenuItem for storing a sequence in fasta format. The sequence
     * to store has to be known, when the method is called.
     * @param sequence the sequence to translate and store as
     * fasta
     * @param refName name of the reference sequence
     * @param seqStart the startpoint of the sequence
     * @param seqStop the endpoint of the sequence
     * @return The JMenuItem for storing a translated sequence in fasta format
     */
    public JMenuItem getStoreFastaItem(final String sequence, final String refName, final int seqStart, final int seqStop){
        //GeneticCode code = GeneticCodeFactory.getGeneticCodeById(Integer.valueOf(pref.get(Properties.SEL_GENETIC_CODE, "1")));
        // String sequenceCopy= code.getTranslationForString(sequence);
       String titel = NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreFasta");
        return this.initStoreFastaItem(titel, sequence, refName, null, seqStart, seqStop);
    }

    /**
     * Initializes a store fasta item either from an feature or with given start
     * and stop indices.
     * @param title the title of the JMenuItem
     * @param sequence the sequence to store in fasta format
     * @param refName name of the reference sequence
     * @param feature the feature containing the header information, <code>null</code> if not from an feature
     * @param seqStart the startpoint of the sequence (-1 if feature is used!)
     * @param seqEnd the endpoint of the sequence (-1 if feature is used!)
     * @return a menu item capable of storing a sequence in fasta format
     */
    private JMenuItem initStoreFastaItem(String title,final String sequence, final String refName, final PersistantFeature feature,
            final int seqStart, final int seqEnd) {
        
        JMenuItem storeFastaItem = new JMenuItem(title);
        storeFastaItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String output;
                if (feature != null) {
                    output = this.generateFastaFromFeature();
                } else {
                    String header = "Copied sequence from: " + refName + " position " + seqStart + " to " + seqEnd;
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

        JMenuItem storeFastaCdsItem = new JMenuItem(NbBundle.getMessage(MenuItemFactory.class, "MenuItem.StoreFastaORF"));
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
