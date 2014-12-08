
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;


import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
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
public class MotifSearchPanel extends javax.swing.JPanel implements Observable {

    public static final String CURRENT_DIR = "currentDirectory";
    private File minus10Input;
    private File minus35Input;
    private File bioProspOutMinus10;
    private File bioProspOutMinus35;
    private File logoMinus10, logoMinus35, info;
    private ProgressHandle progressHandle;
    private List<Observer> observerList;
    List<String> upstreamRegions;
    TreeMap<String, Integer> minus10Starts;
    TreeMap<String, Integer> minus35Starts;
    TreeMap<String, Integer> minus10Shifts;
    TreeMap<String, Integer> minus35Shifts;
    PromotorSearchParameters params;


    /**
     * Creates new form MotifSearchPanel
     */
    public MotifSearchPanel() {
        initComponents();
        additionalInits();
    }


    /**
     * Some additional settings on components like setting borders.
     */
    private void additionalInits() {
        this.observerList = new ArrayList<>();
        promotorsPanel.setBorder( BorderFactory.createTitledBorder( "Promotor region in Fasta format" ) );
        promotorsInFastaTextPane.setEditable( true );
        promotorsInFastaTextPane.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
        minus35RegionPanel.setBorder( BorderFactory.createTitledBorder( "Region of intrest (-35)" ) );
        minus10RegionPanel.setBorder( BorderFactory.createTitledBorder( "Region of intrest (-10)" ) );
        regionOfIntrestMinusThirtyviveTP.setEditable( true );
        regionOfIntrestMinusThirtyviveTP.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
        regionOfIntrestMinusTenTP.setEditable( true );
        regionOfIntrestMinusTenTP.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
        this.infoPanel.setBorder( BorderFactory.createTitledBorder( "Info panel" ) );

        this.minus10LogoPanel.setBorder( BorderFactory.createTitledBorder( "Identified -10 motif" ) );
        this.minus10LogoPanel.setLayout( new BorderLayout() );
        this.minus35LogoPanel.setBorder( BorderFactory.createTitledBorder( "Identified -35 motif" ) );
        this.minus35LogoPanel.setLayout( new BorderLayout() );
    }


    public void setMinSpacer1LengthToLabel( String length ) {
        this.meanSpacer1Label.setText( length );
    }


    public void setMinSpacer2LengthToLabel( String length ) {
        this.meanSpacer2Label.setText( length );
    }


    public void setMinus10MotifWidth( Integer width ) {
        this.motifWidthLabel10.setText( width.toString() );
    }


    public void setMinus35MotifWidth( Integer width ) {
        this.motifWidthLabel35.setText( width.toString() );
    }


    public File getMinus10Input() {
        return minus10Input;
    }


    public void setMinus10Input( File minus10Input ) {
        this.minus10Input = minus10Input;
    }


    public File getMinus35Input() {
        return minus35Input;
    }


    public void setMinus35Input( File minus35Input ) {
        this.minus35Input = minus35Input;
    }


    public File getBioProspOutMinus10() {
        return bioProspOutMinus10;
    }


    public void setBioProspOutMinus10( File bioProspOutMinus10 ) {
        this.bioProspOutMinus10 = bioProspOutMinus10;
    }


    public File getBioProspOutMinus35() {
        return bioProspOutMinus35;
    }


    public void setBioProspOutMinus35( File bioProspOutMinus35 ) {
        this.bioProspOutMinus35 = bioProspOutMinus35;
    }


    public File getLogoMinus10() {
        return logoMinus10;
    }


    public void setLogoMinus10( File logoMinus10 ) {
        this.logoMinus10 = logoMinus10;
    }


    public File getLogoMinus35() {
        return logoMinus35;
    }


    public void setLogoMinus35( File logoMinus35 ) {
        this.logoMinus35 = logoMinus35;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        promotorsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        promotorsInFastaTextPane = new javax.swing.JTextPane();
        minus35LogoPanel = new javax.swing.JPanel();
        minus10LogoPanel = new javax.swing.JPanel();
        minus35RegionPanel = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        regionOfIntrestMinusThirtyviveTP = new javax.swing.JTextPane();
        minus10RegionPanel = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        regionOfIntrestMinusTenTP = new javax.swing.JTextPane();
        infoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        motifWidthLabel35 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        meanSpacer2Label = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        motifWidthLabel10 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        meanSpacer1Label = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        noSegmentsOfSeqsContributionToMotif10 = new javax.swing.JLabel();
        noSegmentsOfSeqsContributionToMotif35 = new javax.swing.JLabel();

        jScrollPane1.setViewportView(promotorsInFastaTextPane);

        javax.swing.GroupLayout promotorsPanelLayout = new javax.swing.GroupLayout(promotorsPanel);
        promotorsPanel.setLayout(promotorsPanelLayout);
        promotorsPanelLayout.setHorizontalGroup(
            promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE))
        );
        promotorsPanelLayout.setVerticalGroup(
            promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(promotorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout minus35LogoPanelLayout = new javax.swing.GroupLayout(minus35LogoPanel);
        minus35LogoPanel.setLayout(minus35LogoPanelLayout);
        minus35LogoPanelLayout.setHorizontalGroup(
            minus35LogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        minus35LogoPanelLayout.setVerticalGroup(
            minus35LogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout minus10LogoPanelLayout = new javax.swing.GroupLayout(minus10LogoPanel);
        minus10LogoPanel.setLayout(minus10LogoPanelLayout);
        minus10LogoPanelLayout.setHorizontalGroup(
            minus10LogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        minus10LogoPanelLayout.setVerticalGroup(
            minus10LogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane9.setViewportView(regionOfIntrestMinusThirtyviveTP);

        javax.swing.GroupLayout minus35RegionPanelLayout = new javax.swing.GroupLayout(minus35RegionPanel);
        minus35RegionPanel.setLayout(minus35RegionPanelLayout);
        minus35RegionPanelLayout.setHorizontalGroup(
            minus35RegionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        minus35RegionPanelLayout.setVerticalGroup(
            minus35RegionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(minus35RegionPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jScrollPane10.setViewportView(regionOfIntrestMinusTenTP);

        javax.swing.GroupLayout minus10RegionPanelLayout = new javax.swing.GroupLayout(minus10RegionPanel);
        minus10RegionPanel.setLayout(minus10RegionPanelLayout);
        minus10RegionPanelLayout.setHorizontalGroup(
            minus10RegionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        minus10RegionPanelLayout.setVerticalGroup(
            minus10RegionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(motifWidthLabel35, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.motifWidthLabel35.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(meanSpacer2Label, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.meanSpacer2Label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(motifWidthLabel10, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.motifWidthLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(meanSpacer1Label, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.meanSpacer1Label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.jButton1.text")); // NOI18N
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
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(motifWidthLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(meanSpacer2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(motifWidthLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(meanSpacer1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(motifWidthLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(meanSpacer2Label)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(motifWidthLabel10)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(meanSpacer1Label)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(noSegmentsOfSeqsContributionToMotif10, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.noSegmentsOfSeqsContributionToMotif10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noSegmentsOfSeqsContributionToMotif35, org.openide.util.NbBundle.getMessage(MotifSearchPanel.class, "MotifSearchPanel.noSegmentsOfSeqsContributionToMotif35.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(minus35LogoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(minus10LogoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1)
                    .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(promotorsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(noSegmentsOfSeqsContributionToMotif35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(minus35RegionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(minus10RegionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(noSegmentsOfSeqsContributionToMotif10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(203, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(promotorsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minus35RegionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minus10RegionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(noSegmentsOfSeqsContributionToMotif35, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                    .addComponent(noSegmentsOfSeqsContributionToMotif10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(minus35LogoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minus10LogoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );

        jScrollPane5.setViewportView(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1108, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        progressHandle = ProgressHandleFactory.createHandle( "Saving Files ..." );
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        int returnVal = fileChooser.showSaveDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) {
            progressHandle.start( 4 );
            this.notifyObservers( PurposeEnum.PROMOTER_ANALYSIS );
            progressHandle.progress( 1 );
            File inputMinus10 = getMinus10Input();
            File inputMinus35 = getMinus35Input();
            inputMinus10.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\intputSequencesForBioProspectorMinus10.fna" ) );
            inputMinus35.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\intputSequencesForBioProspectorMinus35.fna" ) );
            File outputMinus10 = getBioProspOutMinus10();
            File outputMinus35 = getBioProspOutMinus35();
            outputMinus10.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\bestOutputFromBioProspectorMinus10.fna" ) );
            outputMinus35.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\bestOutputFromBioProspectorMinus35.fna" ) );
            File logoMinus10 = getLogoMinus10();
            File logoMinus35 = getLogoMinus35();
            logoMinus10.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\sequenceLogoMinus10.eps" ) );
            logoMinus35.renameTo( new File( fileChooser.getSelectedFile().getAbsolutePath() + "\\sequenceLogoMinus35.eps" ) );
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
    private javax.swing.JPanel infoPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel meanSpacer1Label;
    private javax.swing.JLabel meanSpacer2Label;
    private javax.swing.JPanel minus10LogoPanel;
    private javax.swing.JPanel minus10RegionPanel;
    private javax.swing.JPanel minus35LogoPanel;
    private javax.swing.JPanel minus35RegionPanel;
    private javax.swing.JLabel motifWidthLabel10;
    private javax.swing.JLabel motifWidthLabel35;
    private javax.swing.JLabel noSegmentsOfSeqsContributionToMotif10;
    private javax.swing.JLabel noSegmentsOfSeqsContributionToMotif35;
    private javax.swing.JTextPane promotorsInFastaTextPane;
    private javax.swing.JPanel promotorsPanel;
    private javax.swing.JTextPane regionOfIntrestMinusTenTP;
    private javax.swing.JTextPane regionOfIntrestMinusThirtyviveTP;
    // End of variables declaration//GEN-END:variables


    public void setStyledDocumentToRegionOfIntrestMinusTen( StyledDocument doc ) {

        this.regionOfIntrestMinusTenTP.setStyledDocument( doc );
    }


    public void setStyledDocumentToRegionOfIntrestMinus35( StyledDocument doc ) {
        this.regionOfIntrestMinusThirtyviveTP.setStyledDocument( doc );
    }


    public void setStyledDocToPromotorsFastaPane( StyledDocument doc ) {
        this.promotorsInFastaTextPane.setStyledDocument( doc );
    }


    public void setMinus10LogoToPanel( JLabel logo ) {
        this.minus10LogoPanel.add( logo, BorderLayout.CENTER );
    }


    public void setMinus35LogoToPanel( JLabel logo ) {
        this.minus35LogoPanel.add( logo, BorderLayout.CENTER );
    }


    public void setContributionMinus10Label( String text ) {
        this.noSegmentsOfSeqsContributionToMotif10.setText( text );
    }


    public void setContributionMinus35Label( String text ) {
        this.noSegmentsOfSeqsContributionToMotif35.setText( text );
    }


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


    public List<String> getUpstreamRegions() {
        return upstreamRegions;
    }


    public void setUpstreamRegions( List<String> upstreamRegions ) {
        this.upstreamRegions = upstreamRegions;
    }


    public TreeMap<String, Integer> getMinus10Starts() {
        return minus10Starts;
    }


    public void setMinus10Starts( TreeMap<String, Integer> minus10Starts ) {
        this.minus10Starts = minus10Starts;
    }


    public TreeMap<String, Integer> getMinus35Starts() {
        return minus35Starts;
    }


    public void setMinus35Starts( TreeMap<String, Integer> minus35Starts ) {
        this.minus35Starts = minus35Starts;
    }


    public TreeMap<String, Integer> getMinus10Shifts() {
        return minus10Shifts;
    }


    public void setMinus10Shifts( TreeMap<String, Integer> minus10Shifts ) {
        this.minus10Shifts = minus10Shifts;
    }


    public TreeMap<String, Integer> getMinus35Shifts() {
        return minus35Shifts;
    }


    public void setMinus35Shifts( TreeMap<String, Integer> minus35Shifts ) {
        this.minus35Shifts = minus35Shifts;
    }


    public PromotorSearchParameters getParams() {
        return params;
    }


    public void setParams( PromotorSearchParameters params ) {
        this.params = params;
    }


    public File getInfo() {
        return info;
    }


    public void setInfo( File info ) {
        this.info = info;
    }


}
