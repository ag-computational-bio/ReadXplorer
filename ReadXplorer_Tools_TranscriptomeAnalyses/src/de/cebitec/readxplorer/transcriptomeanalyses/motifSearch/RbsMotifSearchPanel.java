/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.motifSearch;


import de.cebitec.readxplorer.transcriptomeanalyses.datastructures.Operon;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.text.StyledDocument;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;


/**
 *
 * @author jritter
 */
public class RbsMotifSearchPanel extends javax.swing.JPanel implements
        Observable {

    private ProgressHandle progressHandle;
    private File bioProspInput, bioProspOut, sequenceLogo, info;
    private List<Observer> observerList;
    TreeMap<String, Integer> rbsStarts;
    TreeMap<String, Integer> rbsShifts;
    RbsAnalysisParameters params;
    List<Operon> operons;
    List<String> upstreamRegions;


    /**
     * Creates new form RbsMotifSearchPanel
     */
    public RbsMotifSearchPanel() {
        initComponents();
        additionalInits();
    }


    /**
     * Some additional settings on components like setting borders.
     */
    private void additionalInits() {
        this.logoPanel.setLayout( new BorderLayout() );
        this.logoPanel.setBorder( BorderFactory.createTitledBorder( "Identified motif" ) );
        this.jPanel2.setBorder( BorderFactory.createTitledBorder( "Selected length of sequence for analysis" ) );
        this.regionsToAnalyseTP.setEditable( true );
        this.regionsToAnalyseTP.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
        this.jPanel3.setBorder( BorderFactory.createTitledBorder( "Regions of interest" ) );
        this.regionOfIntrestTP.setEditable( true );
        this.regionOfIntrestTP.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
        this.infoPanel.setBorder( BorderFactory.createTitledBorder( "Info panel" ) );
        this.observerList = new ArrayList<>();
    }


    public void setLogo( JLabel logo ) {
        this.logoPanel.add( logo, BorderLayout.CENTER );
    }


    public void setRegionsToAnalyzeToPane( StyledDocument doc ) {
        this.regionsToAnalyseTP.setStyledDocument( doc );
    }


    public void setContributedSequencesToMotif( String text ) {
        this.contributedSequencesToMotifLabel.setText( text );
    }


    public void setRegionOfIntrestToPane( StyledDocument doc ) {
        this.regionOfIntrestTP.setStyledDocument( doc );
    }


    public File getBioProspOut() {
        return bioProspOut;
    }


    public void setBioProspOut( File bioProspOut ) {
        this.bioProspOut = bioProspOut;
    }


    public void disableRegionOfIntrestPanel() {
        this.regionOfIntrestTP.setEnabled( false );
        this.jPanel3.setEnabled( false );
        this.jScrollPane3.setEnabled( false );
    }


    public void setMotifWidth( Integer width ) {
        this.motifWidthLabel.setText( width.toString() );
    }


    public void setRegionLengthForBioProspector( Integer length ) {
        this.lengthOfSequenceForBioProspectorLabel.setText( length.toString() );
    }


    public void setMeanSpacerLength( String length ) {
        this.meanSpacerLabel.setText( length );
    }


    public File getBioProspInput() {
        return bioProspInput;
    }


    public void setBioProspInput( File bioProspInput ) {
        this.bioProspInput = bioProspInput;
    }


    public File getSequenceLogo() {
        return sequenceLogo;
    }


    public void setSequenceLogo( File sequenceLogo ) {
        this.sequenceLogo = sequenceLogo;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        regionsToAnalyseTP = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        regionOfIntrestTP = new javax.swing.JTextPane();
        jSeparator1 = new javax.swing.JSeparator();
        logoPanel = new javax.swing.JPanel();
        infoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lengthOfSequenceForBioProspectorLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        motifWidthLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        meanSpacerLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        contributedSequencesToMotifLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        jScrollPane2.setViewportView(regionsToAnalyseTP);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jScrollPane3.setViewportView(regionOfIntrestTP);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout logoPanelLayout = new javax.swing.GroupLayout(logoPanel);
        logoPanel.setLayout(logoPanelLayout);
        logoPanelLayout.setHorizontalGroup(
            logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        logoPanelLayout.setVerticalGroup(
            logoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lengthOfSequenceForBioProspectorLabel, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.lengthOfSequenceForBioProspectorLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(motifWidthLabel, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.motifWidthLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(meanSpacerLabel, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.meanSpacerLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(contributedSequencesToMotifLabel, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.contributedSequencesToMotifLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(RbsMotifSearchPanel.class, "RbsMotifSearchPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(49, 49, 49)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contributedSequencesToMotifLabel)
                    .addComponent(meanSpacerLabel)
                    .addComponent(motifWidthLabel)
                    .addComponent(lengthOfSequenceForBioProspectorLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, infoPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lengthOfSequenceForBioProspectorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(motifWidthLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(meanSpacerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(contributedSequencesToMotifLabel))
                .addGap(38, 38, 38)
                .addComponent(jButton1)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(logoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        progressHandle = ProgressHandleFactory.createHandle( "Saving Files ..." );
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        int returnVal = fileChooser.showSaveDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) {
            progressHandle.start( 4 );
            this.notifyObservers( this );
            progressHandle.progress( 1 );
            File input = getBioProspInput();
            input.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\intputSequencesForBioProspector.fna" ) );
            File output = getBioProspOut();
            output.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\bestOutputFromBioProspector.fna" ) );
            File logo = getSequenceLogo();
            logo.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\rbsLogo.eps" ) );
            File info = getInfo();
            info.renameTo( new File( fileChooser.getSelectedFile().getAbsoluteFile() + "\\info.txt" ) );
            progressHandle.progress( 2 );
        }
        progressHandle.progress( 3 );
        progressHandle.progress( 4 );

        JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), "Export was successful!",
                                       "Export was successful!", JOptionPane.INFORMATION_MESSAGE );
        progressHandle.finish();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel contributedSequencesToMotifLabel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lengthOfSequenceForBioProspectorLabel;
    private javax.swing.JPanel logoPanel;
    private javax.swing.JLabel meanSpacerLabel;
    private javax.swing.JLabel motifWidthLabel;
    private javax.swing.JTextPane regionOfIntrestTP;
    private javax.swing.JTextPane regionsToAnalyseTP;
    // End of variables declaration//GEN-END:variables


    @Override
    public void registerObserver( Observer observer ) {
        this.observerList.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observerList.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : observerList ) {
            observer.update( data );
        }
    }


    public TreeMap<String, Integer> getRbsStarts() {
        return rbsStarts;
    }


    public void setRbsStarts( TreeMap<String, Integer> rbsStarts ) {
        this.rbsStarts = rbsStarts;
    }


    public TreeMap<String, Integer> getRbsShifts() {
        return rbsShifts;
    }


    public void setRbsShifts( TreeMap<String, Integer> rbsShifts ) {
        this.rbsShifts = rbsShifts;
    }


    public RbsAnalysisParameters getParams() {
        return params;
    }


    public void setParams( RbsAnalysisParameters params ) {
        this.params = params;
    }


    public List<Operon> getOperons() {
        return operons;
    }


    public void setOperons( List<Operon> operons ) {
        this.operons = operons;
    }


    public List<String> getUpstreamRegions() {
        return upstreamRegions;
    }


    public void setUpstreamRegions( List<String> upstreamRegions ) {
        this.upstreamRegions = upstreamRegions;
    }


    public File getInfo() {
        return info;
    }


    public void setInfo( File info ) {
        this.info = info;
    }


}
