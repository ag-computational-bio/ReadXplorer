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

package de.cebitec.readXplorer.options;


import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.readXplorer.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbPreferences;


/**
 * Panel for choosing the genetic code to use. Meaning which start and stop
 * codons should be used.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
final class GeneticCodePanel extends OptionsPanel {

    private static final long serialVersionUID = 1L;

    private final GeneticCodeOptionsPanelController controller;
    private Preferences pref;
    List<GeneticCode> genCodes;


    GeneticCodePanel( GeneticCodeOptionsPanelController controller ) {
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        this.genCodes = genCodeFactory.getGeneticCodes();
        this.controller = controller;
        this.pref = NbPreferences.forModule( Object.class );
        this.initComponents();
        this.initChooseCodeComboBox();
        this.initListener();

    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooseCodeLabel = new javax.swing.JLabel();
        geneticCodeScrolPane = new javax.swing.JScrollPane();
        geneticCodeList = new javax.swing.JList<>();
        customCodonField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        customCodonLabel = new javax.swing.JLabel();
        removeButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(chooseCodeLabel, org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.chooseCodeLabel.text")); // NOI18N

        geneticCodeScrolPane.setViewportView(geneticCodeList);

        customCodonField.setText(org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.customCodonField.text")); // NOI18N
        customCodonField.setToolTipText(org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.customCodonField.toolTipText")); // NOI18N
        //ensure the tooltips are shown for 20 seconds to be able to read the data
        ToolTipManager.sharedInstance().setDismissDelay(20000);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(customCodonLabel, org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.customCodonLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.removeButton.text")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GeneticCodePanel.class, "GeneticCodePanel.removeButton.toolTipText")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(geneticCodeScrolPane)
                    .addComponent(chooseCodeLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(customCodonLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customCodonField, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(chooseCodeLabel)
                .addGap(18, 18, 18)
                .addComponent(geneticCodeScrolPane, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(customCodonField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customCodonLabel)
                    .addComponent(removeButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        this.addCodonsToList();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        this.removeCodonsFromList();
    }//GEN-LAST:event_removeButtonActionPerformed


    @Override
    public void load() {
        this.geneticCodeList.setSelectedIndex( Integer.valueOf( this.pref.get( Properties.GENETIC_CODE_INDEX, "0" ) ) );
    }


    /**
     * Store modified settings
     */
    @Override
    public void store() {
        if( geneticCodeList.getSelectedIndex() < genCodes.size() ) {
            // remember selected indices in geneticCodeList have to be conform with GeneticCodesToPropParser order!
            int identifier = genCodes.get( geneticCodeList.getSelectedIndex() ).getId();
            this.pref.put( Properties.SEL_GENETIC_CODE, String.valueOf( identifier ) );
            this.pref.put( Properties.GENETIC_CODE_INDEX, String.valueOf( geneticCodeList.getSelectedIndex() ) );
        }
        else {
            //special about this case is that it starts with a "(" which can be used for distinguishing both cases
            //the index is the other criterion
            if( geneticCodeList.getSelectedValue() != null ) {
                this.pref.put( Properties.SEL_GENETIC_CODE, String.valueOf( geneticCodeList.getSelectedValue() ) );
                this.pref.put( Properties.GENETIC_CODE_INDEX, String.valueOf( geneticCodeList.getSelectedIndex() ) );
            }
            else {
                this.pref.put( Properties.SEL_GENETIC_CODE, "1" );
                this.pref.put( Properties.GENETIC_CODE_INDEX, "0" );
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel chooseCodeLabel;
    private javax.swing.JTextField customCodonField;
    private javax.swing.JLabel customCodonLabel;
    private javax.swing.JList<Object> geneticCodeList;
    private javax.swing.JScrollPane geneticCodeScrolPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /**
     * Creates the necessary listeners.
     */
    private void initListener() {

        //listener for de/activating the remove button
        this.geneticCodeList.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                if( GeneticCodePanel.this.geneticCodeList.getSelectedIndex() >= genCodes.size() ) {
                    GeneticCodePanel.this.removeButton.setEnabled( true );
                }
                else {
                    GeneticCodePanel.this.removeButton.setEnabled( false );
                }
            }


        } );
    }


    /**
     * Creates the content of the combo box containing the genetic codes.
     */
    private void initChooseCodeComboBox() {
        this.geneticCodeList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );


        final List<String> geneticCodesData = new ArrayList<>();

        //get standard codes and add to tableD
        String codonsConcat;
        for( GeneticCode genCode : genCodes ) {
            codonsConcat = "<html>ID " + genCode.getId() + ": <b>(Starts: ";
            for( String codon : genCode.getStartCodons() ) {
                codonsConcat = codonsConcat.concat( codon ).concat( ", " );
            }
            codonsConcat = codonsConcat.concat( " Stops: " );
            for( String codon : genCode.getStopCodons() ) {
                codonsConcat = codonsConcat.concat( codon ).concat( ", " );
            }
            codonsConcat = codonsConcat.substring( 0, codonsConcat.length() - 2 );
            geneticCodesData.add( codonsConcat.concat( ")</b> - <i>" ).concat( genCode.getDescription() ).concat( "</i></html>" ) );
        }
        this.geneticCodeList.setModel( new GeneticCodeListModel( geneticCodesData ) );

        //get custom codes and add to table
        String storedCustomCodes = this.pref.get( Properties.CUSTOM_GENETIC_CODES, "" );
        while( storedCustomCodes.contains( "\n" ) ) {
            this.addGeneticCodeToTable( storedCustomCodes.substring( 0, storedCustomCodes.indexOf( '\n' ) ) );
            storedCustomCodes = storedCustomCodes.substring( storedCustomCodes.indexOf( '\n' ) + 1 );
        }
        if( storedCustomCodes.length() > 0 ) {
            this.addGeneticCodeToTable( storedCustomCodes.substring( 0, storedCustomCodes.length() ) );
        }

    }


    /**
     * Adds a custom genetic code taken from the customCodonField to both,
     * the gui list and the custom genetic codes storage.
     * <br>Input format: "(startcodon1, startcodon2, startcodon3, ...;
     * stopcodon1,
     * stopcodon2, ...) - codeName"
     */
    private void addCodonsToList() {
        String newGeneticCode = this.customCodonField.getText();
        newGeneticCode = this.checkInput( newGeneticCode );
        if( !(newGeneticCode).equals( "false" ) ) {
            //store in custom genetic codes storage
            String storedCustomCodes = this.pref.get( Properties.CUSTOM_GENETIC_CODES, "" );
            if( storedCustomCodes.length() > 0 ) {
                this.pref.put( Properties.CUSTOM_GENETIC_CODES, storedCustomCodes + "\n" + newGeneticCode );
            }
            else {
                this.pref.put( Properties.CUSTOM_GENETIC_CODES, newGeneticCode );
            }

            //add to table
            this.addGeneticCodeToTable( newGeneticCode );
        }
        else {
            this.customCodonField.setText( "Wrong input format!" );
        }
        this.validate();
    }


    /**
     * Checks the input string for correct format.
     * <p>
     * @param codonString input string containing the codons and the identifier
     *                    the identifier can be left empty
     * <p>
     * @return codonString with uppercase codons and <code>false</code> if input
     *         is not valid
     */
    private String checkInput( String codonString ) {
        String uppercaseCodons = "(";
        if( codonString.startsWith( "(" ) && codonString.indexOf( ')' ) > -1 ) {
            String codonPart = codonString.substring( 1, codonString.indexOf( ')' ) );
            if( codonPart.length() == 0 ) {
                return "false";
            }
            String[] startAndStops = codonPart.split( ";" );
            if( startAndStops.length <= 2 ) {
                for( String codons : startAndStops ) {
                    String[] splitted = codons.split( "," );
                    String codon;
                    for( int i = 0; i < splitted.length; ++i ) {
                        codon = splitted[i].toUpperCase().trim();
                        uppercaseCodons = uppercaseCodons.concat( codon ).concat( ", " );
                        while( codon.length() > 0 ) {
                            if( codon.startsWith( "A" ) || codon.startsWith( "G" ) || codon.startsWith( "C" )
                                || codon.startsWith( "T" ) ) {
                                codon = codon.substring( 1 );
                            }
                            else {
                                return "false";
                            }
                        }
                    }
                    uppercaseCodons = uppercaseCodons.substring( 0, uppercaseCodons.length() - 2 ).concat( "; " );
                }
                return uppercaseCodons.substring( 0, uppercaseCodons.length() - 2 ).
                        concat( codonString.substring( codonString.indexOf( ')' ), codonString.length() ) );
            }
        }
        return "false";
    }


    /**
     * Adds a custom genetic code string to the gui table model.
     * Input format: "(codon1, codon2, codon3, ...) - codeName"
     * <p>
     * @param newGeneticCode string containing the custom genetic code to add
     */
    private void addGeneticCodeToTable( String newGeneticCode ) {
        String codons = "<html><b>" + newGeneticCode.substring( 0, newGeneticCode.indexOf( ')' ) + 1 ) + "</b>";
        String identifier = "<i>" + newGeneticCode.substring( newGeneticCode.indexOf( ')' ) + 1, newGeneticCode.length() ) + "</i></html>";
        ((GeneticCodeListModel) this.geneticCodeList.getModel()).addElement( codons + identifier );
    }


    /**
     * Removes the selected custom genetic code from both,
     * the gui list and the custom genetic codes storage.
     */
    private void removeCodonsFromList() {

        String origCustomCodes = this.pref.get( Properties.CUSTOM_GENETIC_CODES, "" );
        String customCodes = origCustomCodes;
        int codeIndex;
        int index;
        int endIndex = 0;
        int lineBreakIndex;

        //remove from storage
        if( (codeIndex = this.geneticCodeList.getSelectedIndex()) >= (index = genCodes.size()) ) {

            if( codeIndex == Integer.valueOf( this.pref.get( Properties.GENETIC_CODE_INDEX, "0" ) ) ) {
                //reset genetic code to standard
                this.pref.put( Properties.SEL_GENETIC_CODE, String.valueOf( genCodes.get( 0 ).getId() ) );
                this.pref.put( Properties.GENETIC_CODE_INDEX, "0" );
            }

            while( index++ < codeIndex ) {
                lineBreakIndex = customCodes.indexOf( '\n' );
                endIndex += lineBreakIndex + 1;
                customCodes = customCodes.substring( lineBreakIndex + 1, customCodes.length() );
            }
            if( customCodes.indexOf( '\n' ) > -1 ) {
                customCodes = customCodes.substring( customCodes.indexOf( '\n' ) );
            }
            else {
                customCodes = "";
            }
            if( endIndex == 0 ) {
                ++endIndex;
            }
            this.pref.put( Properties.CUSTOM_GENETIC_CODES, origCustomCodes.substring( 0, endIndex - 1 ) + customCodes );

            //remove from table model
            ((GeneticCodeListModel) this.geneticCodeList.getModel()).removeElement( codeIndex );
            this.geneticCodeList.setSelectedIndex( 0 );
        }
    }


    /**
     * Internal class representing the table model for start codons.
     * Implements all methods needed for handling some geneticCodesData.
     */
    private class GeneticCodeListModel extends AbstractListModel<Object> {

        private static final long serialVersionUID = 1L;

        private List<String> geneticCodesDataModel;


        public GeneticCodeListModel( List<String> geneticCodesData ) {
            this.geneticCodesDataModel = geneticCodesData;
        }


        @Override
        public int getSize() {
            return this.geneticCodesDataModel.size();
        }


        @Override
        public Object getElementAt( int i ) {
            if( i >= this.getSize() ) {
                return 0; //means standard codons
            }
            return this.geneticCodesDataModel.get( i );
        }


        /**
         * Adds an element at the end of the array.
         * <p>
         * @param newCodonString String to add
         */
        public void addElement( String newCodonString ) {
            this.geneticCodesDataModel.add( newCodonString );
            int index = this.geneticCodesDataModel.size() - 1;
            this.fireContentsChanged( this, index, index );
        }


        /**
         * Removes an element from the data model array.
         * <p>
         * @param index the index of the entry to remove
         */
        public void removeElement( int index ) {
            this.geneticCodesDataModel.remove( index );
            this.fireContentsChanged( this, index, index );
        }


    }

}
