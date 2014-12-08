/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readXplorer.ui.visualisation.reference;


import de.cebitec.readXplorer.util.CodonUtilities;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


/**
 *
 * @author ddoppmeier, rhilker
 */
public class CodonSelector extends javax.swing.JPanel {

    private final static long serialVersionUID = 24966;

    private ReferenceViewer viewer;
    private Preferences pref;


    /**
     * Creates new form CodonSelector
     */
    public CodonSelector() {
        this.initComponents();
        this.initListener();
        this.createPanels();

    }


    public void setGenomeViewer( ReferenceViewer viewer ) {
        this.viewer = viewer;
        for( Component comp : this.getComponents() ) {
            if( comp instanceof CodonFamilyPanel ) {
                ((CodonFamilyPanel) comp).checkBoxes();
            }
        }
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        basePanel = new javax.swing.JPanel();

        javax.swing.GroupLayout basePanelLayout = new javax.swing.GroupLayout(basePanel);
        basePanel.setLayout(basePanelLayout);
        basePanelLayout.setHorizontalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        basePanelLayout.setVerticalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBorder(javax.swing.BorderFactory.createTitledBorder("Highlighted codons"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel basePanel;
    // End of variables declaration//GEN-END:variables


    /**
     * Updates the codon selector according to the genetic code chosen.
     * If not the standard code is chosen new components have to be generated
     * and added to this panel.
     */
    private void initListener() {
        this.pref = NbPreferences.forModule( Object.class );
        this.pref.addPreferenceChangeListener( new PreferenceChangeListener() {
            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                if( evt.getKey().equals( Properties.SEL_GENETIC_CODE ) ) {
                    createPanels();
                }
            }


        } );
    }


    /**
     * Creates the panels, which contain the check boxes for the start and stop
     * codons.
     */
    private void createPanels() {

        this.removeAll();
        this.setLayout( new BorderLayout() );

        Pair<String[], String[]> geneticCode = CodonUtilities.getGeneticCodeArrays();
        String[] startCodons = geneticCode.getFirst();
        String[] stopCodons = geneticCode.getSecond();

        CodonFamilyPanel startPanel = new CodonFamilyPanel( "Starts:", startCodons );
        CodonFamilyPanel stopPanel = new CodonFamilyPanel( "Stops:", stopCodons );
        this.add( startPanel, BorderLayout.NORTH );
        this.add( stopPanel, BorderLayout.SOUTH );
    }


    /**
     * A panel for the check boxes of all codons of the same type (start or
     * stop).
     * They are added dynamically in the panel, surrounded by a titled border,
     * titled
     * with the familyId.
     */
    private class CodonFamilyPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private final String familyId;


        /**
         * A panel for the check boxes of all codons of the same type (start or
         * stop). They are added dynamically in the panel, surrounded by a
         * titled border, titled with the familyId.
         * <p>
         * @param familyId the id of the codon family, which should be displayed
         *                 on the titled border.
         * @param the      list of codons, for which check boxes should be
         *                 created.
         */
        CodonFamilyPanel( String familyId, String[] codons ) {
            this.familyId = familyId;
            this.updateComponents( codons );
        }


        /**
         * Updates the components of this codon selector. When another genetic
         * code was chosen all old start codon checkboxes are removed and new
         * checkboxes are created.
         */
        private void updateComponents( String[] codons ) {

            JComponent[] newComps = new JComponent[codons.length];
            for( int i = 0; i < codons.length; ++i ) {

                //create as many checkboxes as needed and add them to this component
                final int index = i; //needs to be final for listener
                final JCheckBox newBox = new JCheckBox( codons[i] );//should be upper case already
                newBox.addActionListener( new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed( java.awt.event.ActionEvent evt ) {
                        if( familyId.contains( "Start" ) ) {
                            viewer.getSequenceBar().showStartCodons( index, newBox.isSelected() );
                        }
                        else { // means: familyId.contains("Stop")
                            viewer.getSequenceBar().showStopCodons( index, newBox.isSelected() );
                        }

                    }


                } );
                newComps[i] = newBox;
            }
            this.addCompsToPanel( newComps, familyId );
        }


        /**
         * Adds the given array of JCheckboxes to this CodonSelectorPanel.
         *
         * @param compsToAdd the array of checkboxes to add to this component
         */
        private void addCompsToPanel( final JComponent[] compsToAdd, String familyId ) {

            this.removeAll();
            this.setBorder( javax.swing.BorderFactory.createTitledBorder( familyId ) );
            int nbRows = 1 + (compsToAdd.length - 1) / 3;
            int nbColumns = 3;

            GridLayout layout = new GridLayout( nbRows, nbColumns );
            this.setLayout( layout );

            for( int i = 0; i < compsToAdd.length; ++i ) {
                this.add( compsToAdd[i] );
            }
        }


        /**
         * Used to check for boxes that are checked when a new viewer was
         * selected.
         */
        private void checkBoxes() {
            JCheckBox currentBox;
            Component comp;
            for( int i = 0; i < this.getComponentCount(); ++i ) {
                comp = this.getComponent( i );
                if( comp instanceof JCheckBox ) {
                    currentBox = (JCheckBox) this.getComponent( i ); //order needs to be correct
                    if( familyId.contains( "Start" ) ) {
                        viewer.getSequenceBar().showStartCodons( i, currentBox.isSelected() );
                    }
                    else { // means: familyId.contains("Stop")
                        viewer.getSequenceBar().showStopCodons( i, currentBox.isSelected() );
                    }
                }
            }
        }


    }

//    /**
//     * Calculates the preferred size of this panel.
//     * @return the preferred size of this panel
//     */
//    private Dimension calcPreferredSize(int nbBoxes) {
//        Dimension prefSize = this.getPreferredSize();
//        int prefHeight = CodonSelector.basicSize;
//        while (nbBoxes > 0) {
//            prefHeight += this.checkBoxheight;
//            nbBoxes -= 3;
//        }
//        return new Dimension(prefSize.width + 40, prefHeight);
//    }

}
