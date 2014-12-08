package de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard;


import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.DoubleVerifier;
import de.cebitec.readXplorer.transcriptomeAnalyses.verifier.IntegerVerifier;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.openide.util.NbPreferences;


public final class FivePrimeEnrichedTracksVisualPanel extends JPanel {

    private final String tssDetectionText = "<html><p align='justify'>For the determination of TSS for each a position i, the number of"
                                            + " read starts on that strand are considered. A putative TSS is displayed, if the read starts"
                                            + " at this position exceeded the background threshold (parameter: <b>fraction</b>) and the <b>ratio</b> xi/xi-1 at"
                                            + " this position exceeds the threshold (parameter: ratio). Although the default values are based"
                                            + " on experience, it is recommend to try different combinations of parameters to find out which"
                                            + "deliver the best results. Additionally, it is crucial to understand the difference between"
                                            + "coverage and read starts as latter one are used for calculation.</p></html>";
    private final String fractionText = "<html><b>Fraction</b> (used for background threshold calculation, #FP)</html>";
    private final String ratioText = "<html><b>Ratio</b></html>";
    private final String excludeTssWithDistanceText = "<html>Exclude TSS with putative <b>5'-UTR length</b> of</html>";
    private final String excludeInragenicTxt = "<html>Exclude all <b>intragenic</b> TSS</html>";
    private final String antisense3UtrText = "<html><b>as3'-UTR</b> detection with maximal distance to feature stop of</html>";
    private final String keepAllIntragenicTxt = "<html>Keep all <b>intragenic</b> TSS, assign TSS to next feature if distance is</html>";
    private final String includeBestMatchText = "<html>Include <b>best matched reads</b> into analysis</html>";
    private final String keepOnlyIntragenicTxt = "<html>Keep only <b>intragenic</b> TSS if next feature distance is</html>";
    private final String leaderlessDetectionText = "<html>Classification as <b>leaderless</b> if distance to <b>TLS</b> is</html>";
    private final String keepOnlyIntragenicTssAndAssigneToFeat = "<html>\t TSS will be assigned to the <b>next feature</b>.</html>";
    private final String featureTypeExclusionFormAnalysisTextPart1 = "<html><p align='justify'>Exclude feature types from analysis</p></html>";
    private final String featureTypeExclusionFormAnalysisTextPart2 = "<html><p align='justify'>**TSS of the excluded feature types will be assigned to\n"
                                                                     + "the next feature if all criteria such as maximal distance are met.</p></html>";
    private final String wizardName;
    private final HashMap<String, StartCodon> validStartCodons;
    private final HashSet<FeatureType> excludedFeatreTypes;

    public static final String PROP_SELECTED_FEAT_TYPES_FADE_OUT = "Exclude feature types from analysis";


    /**
     * Creates new form FivePrimeEnrichedTracksVisualPanel
     */
    public FivePrimeEnrichedTracksVisualPanel( String wizardName ) {
        initComponents();
        setInputVerifier();
        updateFields();
        this.manuallyMinStackSizeTF.setEnabled( false );
        this.wizardName = wizardName;

        this.leaderlessDetectionPanel.setBorder( BorderFactory.createTitledBorder( null, "<html>Detection of <b>leaderless</b> transcripts</html>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK ) );
        this.leaderlessDetectionLabel.setText( leaderlessDetectionText );
        this.tlsShiftPanel.setBorder( BorderFactory.createTitledBorder( null, "<html>Detection of <b>TLS shifts</b></html>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK ) );

        this.ratioLabel.setText( ratioText );
        this.fractionLabel.setText( fractionText );

        // in context of excluding/including intragenic tss
        this.exclusionOfAllIntragenicTssCB.setText( excludeInragenicTxt );
        this.keepAllIntragenicTssCB.setText( keepAllIntragenicTxt );
        this.keepOnlyIntragenicNextFeatCB.setText( keepOnlyIntragenicTxt );

        this.excludeTssWithDistanceLabel.setText( excludeTssWithDistanceText );

        this.antisense3UtrLabel.setText( antisense3UtrText );

        this.includeBestMatchedReads.setText( includeBestMatchText );

        this.additionalTextToKeepOnlyIntragenicLabel.setText( keepOnlyIntragenicTssAndAssigneToFeat );

        this.excludedFeatreTypes = new HashSet<>();
        this.excludingFeatureTypesLabelPart1.setText( featureTypeExclusionFormAnalysisTextPart1 );

        this.validStartCodons = new HashMap<>();
    }


    private void setInputVerifier() {
        this.fractionTF.setInputVerifier( new DoubleVerifier( this.fractionTF ) );
        this.limitationForUtrLengthTF.setInputVerifier( new IntegerVerifier( this.limitationForUtrLengthTF ) );
        this.keepOnlyIntragenicNextFeatDistLimitTF.setInputVerifier( new IntegerVerifier( this.keepOnlyIntragenicNextFeatDistLimitTF ) );
        this.keepAllIntragenicTssDistLimitTF.setInputVerifier( new IntegerVerifier( this.keepAllIntragenicTssDistLimitTF ) );
        this.keepAllIntragenicTssDistLimitTF.setEnabled( false );
        this.leaderlessDistanceLimitTF.setInputVerifier( new IntegerVerifier( this.leaderlessDistanceLimitTF ) );
        this.threeUtrAntisenseDistanceLimit.setInputVerifier( new IntegerVerifier( this.threeUtrAntisenseDistanceLimit ) );
        this.ratioTF.setInputVerifier( new IntegerVerifier( this.ratioTF ) );
        this.manuallyMinStackSizeTF.setInputVerifier( new IntegerVerifier( manuallyMinStackSizeTF ) );
    }


    @Override
    public String getName() {
        return "Identification of transcription start sites using 5′-end data";
    }


    public Double getFraction() {
        return Double.valueOf( fractionTF.getText() ) / 100;
    }


    public Integer getRatio() {
        return Integer.valueOf( ratioTF.getText() );
    }


    /**
     *
     * @return <true> if checkbox for exclusion of all intragenic tss is
     *         selected else <false>
     */
    public boolean isExclusionOfAllIntragenicTss() {
        return exclusionOfAllIntragenicTssCB.isSelected();
    }


    /**
     *
     * @return the maximum allowable distance for a 5'-UTR
     */
    public Integer getUtrLimitationDistance() {
        return Integer.valueOf( limitationForUtrLengthTF.getText() );
    }


    public Integer getKeepingInternalTssDistance() {
        return Integer.valueOf( keepOnlyIntragenicNextFeatDistLimitTF.getText() );
    }


    public Integer getKeepingAllInragenicTssDistance() {
        return Integer.valueOf( keepAllIntragenicTssDistLimitTF.getText() );
    }


    public Integer getLeaderlessDistance() {
        return Integer.valueOf( this.leaderlessDistanceLimitTF.getText() );
    }


    public Integer getPercentageForCdsShiftAnalysis() {
        return Integer.valueOf( this.percentageTF.getText() );
    }


    public boolean isKeepAllIntragenicTss() {
        return this.keepAllIntragenicTssCB.isSelected();
    }


    public boolean isKeepOnlyIntragenicTssAssignedToFeature() {
        return this.keepOnlyIntragenicNextFeatCB.isSelected();
    }


    public boolean isIncludingBestMathcedReads() {
        return this.includeBestMatchedReads.isSelected();
    }


    public Integer getMaxDistFor3PrimeUtrAntisenseDetection() {
        return Integer.valueOf( this.threeUtrAntisenseDistanceLimit.getText() );
    }


    public boolean isIncludeTrRnaIntoAnalysis() {
        return this.featureTypSourceCB.isSelected();
    }


    public HashMap<String, StartCodon> getValidStartCodonSet() {
        if( codonATG.isSelected() ) {
            this.validStartCodons.put( "ATG", StartCodon.ATG );
        }

        if( codonCTG.isSelected() ) {
            this.validStartCodons.put( "CTG", StartCodon.CTG );
        }

        if( codonGTG.isSelected() ) {
            this.validStartCodons.put( "GTG", StartCodon.GTG );
        }

        if( codonTTG.isSelected() ) {
            this.validStartCodons.put( "TTG", StartCodon.TTG );
        }

        return this.validStartCodons;
    }


    public boolean isMinStackSizeSetManually() {
        return manuallyMinStackSizeCB.isSelected();
    }


    public Integer getMinStackSizeSetManually() {
        return Integer.parseInt( this.manuallyMinStackSizeTF.getText() );
    }


    /**
     * Updates the checkboxes for the read classes with the globally stored
     * settings for this wizard. If no settings were stored, the default
     * configuration is chosen.
     */
    private void updateFields() {
        Preferences pref = NbPreferences.forModule( Object.class );
        String fractionValue = pref.get( wizardName + WizardPropertyStrings.PROP_Fraction, "5" );
        int fraction;
        if( fractionValue.equals( "5" ) ) {
            fraction = 5;
        }
        else {
            double i = Double.valueOf( fractionValue );
            fraction = (int) (i * 100);
        }

        this.fractionTF.setText( "" + fraction );
        this.ratioTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_RATIO, "5" ) );
        this.limitationForUtrLengthTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_UTR_LIMIT, "500" ) );
        this.keepOnlyIntragenicNextFeatDistLimitTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_UTR_LIMIT, "100" ) );
        this.percentageTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_PERCENTAGE_FOR_CDS_ANALYSIS, "10" ) );
        this.leaderlessDistanceLimitTF.setText( pref.get( wizardName + WizardPropertyStrings.PROP_LEADERLESS_LIMIT, "0" ) );
        this.threeUtrAntisenseDistanceLimit.setText( pref.get( wizardName + WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION, "100" ) );
        this.codonATG.setSelected( true );
        this.codonGTG.setSelected( true );
    }


    public HashSet<FeatureType> getExcludedFeatreTypes() {
        return excludedFeatreTypes;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        fractionLabel = new javax.swing.JLabel();
        fractionTF = new javax.swing.JTextField();
        ratioLabel = new javax.swing.JLabel();
        ratioTF = new javax.swing.JTextField();
        exclusionOfAllIntragenicTssCB = new javax.swing.JCheckBox();
        excludeTssWithDistanceLabel = new javax.swing.JLabel();
        limitationForUtrLengthTF = new javax.swing.JTextField();
        keepOnlyIntragenicNextFeatDistLimitTF = new javax.swing.JTextField();
        leaderlessDetectionPanel = new javax.swing.JPanel();
        leaderlessDetectionLabel = new javax.swing.JLabel();
        leaderlessDistanceLimitTF = new javax.swing.JTextField();
        tlsShiftPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        percentageTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        codonATG = new javax.swing.JCheckBox();
        codonGTG = new javax.swing.JCheckBox();
        codonCTG = new javax.swing.JCheckBox();
        codonTTG = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        keepAllIntragenicTssCB = new javax.swing.JCheckBox();
        includeBestMatchedReads = new javax.swing.JCheckBox();
        antisense3UtrLabel = new javax.swing.JLabel();
        threeUtrAntisenseDistanceLimit = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        featureTypSourceCB = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        keepAllIntragenicTssDistLimitTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        excludingFeatureTypesLabelPart1 = new javax.swing.JLabel();
        featureTypeMiscRnaCB = new javax.swing.JCheckBox();
        jSeparator4 = new javax.swing.JSeparator();
        additionalTextToKeepOnlyIntragenicLabel = new javax.swing.JLabel();
        keepOnlyIntragenicNextFeatCB = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        manuallyMinStackSizeCB = new javax.swing.JCheckBox();
        manuallyMinStackSizeTF = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(fractionLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.fractionLabel.text")); // NOI18N

        fractionTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.fractionTF.text")); // NOI18N
        fractionTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fractionTFActionPerformed(evt);
            }
        });
        fractionTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fractionTFKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ratioLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.ratioLabel.text")); // NOI18N

        ratioTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.ratioTF.text")); // NOI18N
        ratioTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratioTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(exclusionOfAllIntragenicTssCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.exclusionOfAllIntragenicTssCB.text")); // NOI18N
        exclusionOfAllIntragenicTssCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exclusionOfAllIntragenicTssCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(excludeTssWithDistanceLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.excludeTssWithDistanceLabel.text")); // NOI18N

        limitationForUtrLengthTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.limitationForUtrLengthTF.text")); // NOI18N

        keepOnlyIntragenicNextFeatDistLimitTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepOnlyIntragenicNextFeatDistLimitTF.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(leaderlessDetectionLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.leaderlessDetectionLabel.text")); // NOI18N

        leaderlessDistanceLimitTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.leaderlessDistanceLimitTF.text")); // NOI18N
        leaderlessDistanceLimitTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaderlessDistanceLimitTFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leaderlessDetectionPanelLayout = new javax.swing.GroupLayout(leaderlessDetectionPanel);
        leaderlessDetectionPanel.setLayout(leaderlessDetectionPanelLayout);
        leaderlessDetectionPanelLayout.setHorizontalGroup(
            leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leaderlessDetectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(leaderlessDetectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(leaderlessDistanceLimitTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        leaderlessDetectionPanelLayout.setVerticalGroup(
            leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leaderlessDetectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leaderlessDetectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leaderlessDetectionLabel)
                    .addComponent(leaderlessDistanceLimitTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel11.text")); // NOI18N

        percentageTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.percentageTF.text")); // NOI18N
        percentageTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                percentageTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codonATG, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.codonATG.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codonGTG, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.codonGTG.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codonCTG, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.codonCTG.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(codonTTG, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.codonTTG.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel12.text")); // NOI18N

        javax.swing.GroupLayout tlsShiftPanelLayout = new javax.swing.GroupLayout(tlsShiftPanel);
        tlsShiftPanel.setLayout(tlsShiftPanelLayout);
        tlsShiftPanelLayout.setHorizontalGroup(
            tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tlsShiftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tlsShiftPanelLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(percentageTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4))
                    .addGroup(tlsShiftPanelLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(codonATG)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codonGTG)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codonCTG)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(codonTTG)
                        .addContainerGap())))
        );
        tlsShiftPanelLayout.setVerticalGroup(
            tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tlsShiftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(percentageTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tlsShiftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(codonATG)
                        .addComponent(codonGTG)
                        .addComponent(codonCTG)
                        .addComponent(codonTTG))
                    .addComponent(jLabel12))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepAllIntragenicTssCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepAllIntragenicTssCB.text")); // NOI18N
        keepAllIntragenicTssCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepAllIntragenicTssCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(includeBestMatchedReads, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.includeBestMatchedReads.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(antisense3UtrLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.antisense3UtrLabel.text")); // NOI18N

        threeUtrAntisenseDistanceLimit.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.threeUtrAntisenseDistanceLimit.text")); // NOI18N
        threeUtrAntisenseDistanceLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threeUtrAntisenseDistanceLimitActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(featureTypSourceCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.featureTypSourceCB.text")); // NOI18N
        featureTypSourceCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                featureTypSourceCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel1.text")); // NOI18N

        keepAllIntragenicTssDistLimitTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepAllIntragenicTssDistLimitTF.text")); // NOI18N
        keepAllIntragenicTssDistLimitTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepAllIntragenicTssDistLimitTFActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(excludingFeatureTypesLabelPart1, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.excludingFeatureTypesLabelPart1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(featureTypeMiscRnaCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.featureTypeMiscRnaCB.text")); // NOI18N
        featureTypeMiscRnaCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                featureTypeMiscRnaCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(additionalTextToKeepOnlyIntragenicLabel, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.additionalTextToKeepOnlyIntragenicLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepOnlyIntragenicNextFeatCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.keepOnlyIntragenicNextFeatCB.text")); // NOI18N
        keepOnlyIntragenicNextFeatCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepOnlyIntragenicNextFeatCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(manuallyMinStackSizeCB, org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.manuallyMinStackSizeCB.text")); // NOI18N
        manuallyMinStackSizeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manuallyMinStackSizeCBActionPerformed(evt);
            }
        });

        manuallyMinStackSizeTF.setText(org.openide.util.NbBundle.getMessage(FivePrimeEnrichedTracksVisualPanel.class, "FivePrimeEnrichedTracksVisualPanel.manuallyMinStackSizeTF.text")); // NOI18N

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tlsShiftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(leaderlessDetectionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(includeBestMatchedReads, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(excludeTssWithDistanceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(antisense3UtrLabel)
                                                    .addComponent(ratioLabel)
                                                    .addComponent(fractionLabel)
                                                    .addComponent(exclusionOfAllIntragenicTssCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(keepAllIntragenicTssCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                                        .addComponent(featureTypSourceCB)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(featureTypeMiscRnaCB))
                                                    .addComponent(keepOnlyIntragenicNextFeatCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addGap(23, 23, 23))
                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(additionalTextToKeepOnlyIntragenicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                            .addComponent(fractionTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel3))
                                        .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(manuallyMinStackSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(limitationForUtrLengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(threeUtrAntisenseDistanceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(keepAllIntragenicTssDistLimitTF, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(keepOnlyIntragenicNextFeatDistLimitTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))))
                                        .addGap(15, 15, 15))))
                            .addComponent(jSeparator1)
                            .addComponent(jSeparator2)
                            .addComponent(jSeparator3)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addComponent(excludingFeatureTypesLabelPart1, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jSeparator4)
                            .addComponent(jSeparator5))))
                .addContainerGap())
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(manuallyMinStackSizeCB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manuallyMinStackSizeCB)
                    .addComponent(manuallyMinStackSizeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(ratioLabel))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fractionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(fractionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ratioTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exclusionOfAllIntragenicTssCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keepAllIntragenicTssCB)
                    .addComponent(keepAllIntragenicTssDistLimitTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keepOnlyIntragenicNextFeatDistLimitTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(keepOnlyIntragenicNextFeatCB))
                .addGap(2, 2, 2)
                .addComponent(additionalTextToKeepOnlyIntragenicLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limitationForUtrLengthTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(excludeTssWithDistanceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(threeUtrAntisenseDistanceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(antisense3UtrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(includeBestMatchedReads)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludingFeatureTypesLabelPart1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(featureTypSourceCB)
                    .addComponent(featureTypeMiscRnaCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(leaderlessDetectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tlsShiftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exclusionOfAllIntragenicTssCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exclusionOfAllIntragenicTssCBActionPerformed
        if( this.exclusionOfAllIntragenicTssCB.isSelected() ) {
            this.keepAllIntragenicTssCB.setSelected( false );
            this.keepAllIntragenicTssCB.setEnabled( false );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( false );
            this.keepOnlyIntragenicNextFeatCB.setEnabled( false );
            this.keepOnlyIntragenicNextFeatDistLimitTF.setEnabled( false );
            this.additionalTextToKeepOnlyIntragenicLabel.setEnabled( false );
        }
        else {
            this.keepAllIntragenicTssCB.setEnabled( true );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( false );
            this.keepOnlyIntragenicNextFeatCB.setEnabled( true );
            this.keepOnlyIntragenicNextFeatDistLimitTF.setEnabled( true );
            this.additionalTextToKeepOnlyIntragenicLabel.setEnabled( true );
        }
    }//GEN-LAST:event_exclusionOfAllIntragenicTssCBActionPerformed

    private void fractionTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fractionTFActionPerformed
    }//GEN-LAST:event_fractionTFActionPerformed

    private void fractionTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fractionTFKeyReleased
    }//GEN-LAST:event_fractionTFKeyReleased

    private void percentageTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_percentageTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_percentageTFActionPerformed

    private void leaderlessDistanceLimitTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaderlessDistanceLimitTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_leaderlessDistanceLimitTFActionPerformed

    private void threeUtrAntisenseDistanceLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_threeUtrAntisenseDistanceLimitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_threeUtrAntisenseDistanceLimitActionPerformed

    private void ratioTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratioTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ratioTFActionPerformed

    private void keepAllIntragenicTssCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepAllIntragenicTssCBActionPerformed
        if( this.keepAllIntragenicTssCB.isSelected() ) {
            this.exclusionOfAllIntragenicTssCB.setSelected( false );
            this.exclusionOfAllIntragenicTssCB.setEnabled( false );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( true );
            this.keepOnlyIntragenicNextFeatCB.setEnabled( false );
            this.keepOnlyIntragenicNextFeatDistLimitTF.setEnabled( false );
            additionalTextToKeepOnlyIntragenicLabel.setEnabled( false );
        }
        else {
            this.exclusionOfAllIntragenicTssCB.setEnabled( true );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( false );
            this.keepOnlyIntragenicNextFeatCB.setEnabled( true );
            this.keepOnlyIntragenicNextFeatDistLimitTF.setEnabled( true );
            additionalTextToKeepOnlyIntragenicLabel.setEnabled( true );
        }
    }//GEN-LAST:event_keepAllIntragenicTssCBActionPerformed

    private void keepAllIntragenicTssDistLimitTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepAllIntragenicTssDistLimitTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keepAllIntragenicTssDistLimitTFActionPerformed

    private void featureTypSourceCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_featureTypSourceCBActionPerformed
        if( featureTypSourceCB.isSelected() ) {
            this.excludedFeatreTypes.add( FeatureType.SOURCE );
        }
        else {
            this.excludedFeatreTypes.remove( FeatureType.SOURCE );
        }
    }//GEN-LAST:event_featureTypSourceCBActionPerformed

    private void featureTypeMiscRnaCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_featureTypeMiscRnaCBActionPerformed
        if( featureTypeMiscRnaCB.isSelected() ) {
            this.excludedFeatreTypes.add( FeatureType.MISC_RNA );
        }
        else {
            this.excludedFeatreTypes.remove( FeatureType.MISC_RNA );
        }
    }//GEN-LAST:event_featureTypeMiscRnaCBActionPerformed

    private void keepOnlyIntragenicNextFeatCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepOnlyIntragenicNextFeatCBActionPerformed
        if( keepOnlyIntragenicNextFeatCB.isSelected() ) {
            this.keepAllIntragenicTssCB.setEnabled( false );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( false );
            this.exclusionOfAllIntragenicTssCB.setEnabled( false );
        }
        else {
            this.keepAllIntragenicTssCB.setEnabled( true );
            this.keepAllIntragenicTssDistLimitTF.setEnabled( true );
            this.exclusionOfAllIntragenicTssCB.setEnabled( true );
        }
    }//GEN-LAST:event_keepOnlyIntragenicNextFeatCBActionPerformed

    private void manuallyMinStackSizeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manuallyMinStackSizeCBActionPerformed
        if( manuallyMinStackSizeCB.isSelected() ) {
            fractionLabel.setEnabled( false );
            fractionTF.setEditable( false );
            manuallyMinStackSizeTF.setEnabled( true );
        }
        else {
            fractionLabel.setEnabled( true );
            fractionTF.setEditable( true );
            manuallyMinStackSizeTF.setEnabled( false );
        }
    }//GEN-LAST:event_manuallyMinStackSizeCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel additionalTextToKeepOnlyIntragenicLabel;
    private javax.swing.JLabel antisense3UtrLabel;
    private javax.swing.JCheckBox codonATG;
    private javax.swing.JCheckBox codonCTG;
    private javax.swing.JCheckBox codonGTG;
    private javax.swing.JCheckBox codonTTG;
    private javax.swing.JLabel excludeTssWithDistanceLabel;
    private javax.swing.JLabel excludingFeatureTypesLabelPart1;
    private javax.swing.JCheckBox exclusionOfAllIntragenicTssCB;
    private javax.swing.JCheckBox featureTypSourceCB;
    private javax.swing.JCheckBox featureTypeMiscRnaCB;
    private javax.swing.JLabel fractionLabel;
    private javax.swing.JTextField fractionTF;
    private javax.swing.JCheckBox includeBestMatchedReads;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JCheckBox keepAllIntragenicTssCB;
    private javax.swing.JTextField keepAllIntragenicTssDistLimitTF;
    private javax.swing.JCheckBox keepOnlyIntragenicNextFeatCB;
    private javax.swing.JTextField keepOnlyIntragenicNextFeatDistLimitTF;
    private javax.swing.JLabel leaderlessDetectionLabel;
    private javax.swing.JPanel leaderlessDetectionPanel;
    private javax.swing.JTextField leaderlessDistanceLimitTF;
    private javax.swing.JTextField limitationForUtrLengthTF;
    private javax.swing.JCheckBox manuallyMinStackSizeCB;
    private javax.swing.JTextField manuallyMinStackSizeTF;
    private javax.swing.JTextField percentageTF;
    private javax.swing.JLabel ratioLabel;
    private javax.swing.JTextField ratioTF;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTextField threeUtrAntisenseDistanceLimit;
    private javax.swing.JPanel tlsShiftPanel;
    // End of variables declaration//GEN-END:variables
}
