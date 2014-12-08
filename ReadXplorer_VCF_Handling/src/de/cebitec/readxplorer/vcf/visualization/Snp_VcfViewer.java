
package de.cebitec.readxplorer.vcf.visualization;


import de.cebitec.readXplorer.databackend.ThreadListener;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import de.cebitec.readXplorer.view.dataVisualisation.basePanel.BasePanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JSlider;
import org.broadinstitute.variant.variantcontext.Allele;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.openide.util.NbPreferences;


/**
 *
 * @author Margarita Steinhauer
 */
public class Snp_VcfViewer extends AbstractViewer implements ThreadListener {

    private List<VariantContext> snpVcfList;

    private static int height = 70;
    private final int labelMargin;
    private final int scaleFactor;
    private final boolean snpsLoaded;
    private List<VariantContext> visibleSNPs;


    private static Color SNP_A = new Color( 178, 34, 34 );
    private static Color SNP_C = new Color( 106, 90, 205 );
    private static Color SNP_G = new Color( 0, 0, 128 );
    private static Color SNP_T = new Color( 255, 140, 0 );
    private static Color SNP_N = new Color( 0, 0, 0 );
    private boolean colorChanges = false;
    private boolean automaticScaling = false;
    private JSlider verticalSlider = null;


    /**
     * Create a new panel to show SNP information
     *
     * @param boundsManager manager for component bounds
     * @param basePanel     serves as basis for other visual components
     * @param refGen        reference genome
     * @param trackCon      database connection to one track, that is displayed
     * @param combineTracks true, if the coverage of the tracks contained in the
     *                      track connector should be combined.
     */
    public Snp_VcfViewer( BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference refGen ) {
        super( boundsManager, basePanel, refGen );

        snpVcfList = new ArrayList<>();
        visibleSNPs = new ArrayList<>();
        labelMargin = 3;
        scaleFactor = 1;
        snpsLoaded = true;
//        boundsManager.addBoundsListener(this);
        this.setActive( true );

        final Preferences pref = NbPreferences.forModule( Object.class );
        this.setColors( pref );

        pref.addPreferenceChangeListener( new PreferenceChangeListener() {

            @Override
            public void preferenceChange( PreferenceChangeEvent evt ) {
                Snp_VcfViewer.this.setColors( pref );
                repaint();
            }


        } );
        this.setViewerSize();

    }


    /**
     * Sets the Colors for each SNP.
     *
     * @param pref
     */
    private void setColors( Preferences pref ) {
        boolean uniformColouration = pref.getBoolean( "uniformDesired", false );
        if( uniformColouration ) {
            String colourRGB = pref.get( "uniformColour", "" );
            //TODO check if this method is used anywhere
            if( !colourRGB.isEmpty() ) {
                SNP_A = new Color( Integer.parseInt( colourRGB ) );
                SNP_C = new Color( Integer.parseInt( colourRGB ) );
                SNP_G = new Color( Integer.parseInt( colourRGB ) );
                SNP_T = new Color( Integer.parseInt( colourRGB ) );
                SNP_N = new Color( Integer.parseInt( colourRGB ) );

            }
        }
        else {
            String snpAColor = pref.get( "SNP: A", "" );
            String snpCColor = pref.get( "SNP: C", "" );
            String snpGColor = pref.get( "SNP: G", "" );
            String snpTColor = pref.get( "SNP: T", "" );
            String snpNColor = pref.get( "SNP: N", "" );

            if( !snpAColor.isEmpty() ) {
                SNP_A = new Color( Integer.parseInt( snpAColor ) );
            }
            if( !snpCColor.isEmpty() ) {
                SNP_C = new Color( Integer.parseInt( snpCColor ) );
            }
            if( !snpGColor.isEmpty() ) {
                SNP_G = new Color( Integer.parseInt( snpGColor ) );
            }
            if( !snpTColor.isEmpty() ) {
                SNP_T = new Color( Integer.parseInt( snpTColor ) );
            }
            if( !snpNColor.isEmpty() ) {
                SNP_N = new Color( Integer.parseInt( snpNColor ) );
            }

        }
    }


    @Override
    public void paintComponent( Graphics graphics ) {
        Rectangle snpRectangle;
        super.paintComponent( graphics );
        Graphics2D g = (Graphics2D) graphics;

        // set rendering hints
        Map<Object, Object> hints = new HashMap<>();
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g.setRenderingHints( hints );

        if( this.snpsLoaded || this.colorChanges ) {

            for( VariantContext vc : snpVcfList ) {
                List<Allele> snpAlleles = vc.getAlleles();
                int leftArea = this.getBoundsInfo().getLogLeft(); //Gives the leftmost position of the area currently visible
                int rightArea = this.getBoundsInfo().getLogRight(); //Gives the rightmost position of the area currently visible
                int snpPosition = vc.getStart();
                // Order to check whether the SNP-Position is lokated in the visible area
                if( snpPosition >= leftArea && snpPosition <= rightArea ) {
                    // TODO aktuelle vs-liste in eine Objektvariable für ToolTipText()
                    // Set the SNP as Rectangle on the appropriate position of the base
                    for( Allele snpAllele : snpAlleles ) {
                        switch( snpAllele.getBaseString() ) {
                            case "A":
                                g.setColor( SNP_A );
                                break;
                            case "C":
                                g.setColor( SNP_C );
                                break;
                            case "G":
                                g.setColor( SNP_G );
                                break;
                            case "T":
                                g.setColor( SNP_T );
                                break;
                            case "N":
                                g.setColor( SNP_N );
                                break;
                        }
                        PhysicalBaseBounds bounds = this.getPhysBoundariesForLogPos( snpPosition );
                        snpRectangle = new Rectangle( (int) bounds.getLeftPhysBound(), 1, (int) bounds.getPhysWidth(), 20 );
                        g.draw( snpRectangle );
                        g.fill( snpRectangle );
                    }
                }
            }
        }
    }


    /**
     * Sets the initial size of the track viewer.
     */
    private void setViewerSize() {
        this.setPreferredSize( new Dimension( 1, height ) );
        this.revalidate();
    }


    @Override
    protected int getMaximalHeight() {
        return height;
    }


    @Override
    //FRAGE: WARUM ZEIGT ES DAS NICHT AN? MUSS ICH DA DEN aBSTRACTvIEWER NOCH IRGENDIWE EINBINDEN?
    //Ändern den Tooltiptext an der aktuellen mausposition --> Infomationen zB für den VCF eintrag zu einem best. SNP
    public void changeToolTipText( int logPos ) {
        //TODO iteration über aktuelle vc (aus paintC.()) und die akteullen rausgeben --> Dafür die Info anzeigen lassen
        //for loop around visibleSNPs
//        List<Allele> snpAlleles = visibleSNPs.getAlternateAlleles();
//        if (snpAlleles.size() <= 1) {
//            Allele snpAllele = snpAlleles.get(0);
//        }

//        String snpChromosome = visibleSNPs.getChr();
//        int snpPosition = visibleSNPs.getStart();

    }


    @Override
    //Wird aufgerufen, wenn sich die Grenzen verändern, in dem aktuellen fenster
    public void boundsChangedHook() {
        paintComponent( this.getGraphics() );
        this.repaint();
    }


    @Override
    //Sortiert einem die SNPs für ein bestimmtes Intervall (muss abgefragt werden)
    // METHODE NÄHER IM TRACKVIEWER() ERKLÄREN LASSEN.
    public void receiveData( Object snpData ) {
        //TODO
//
//            this.snp = covResult.getCoverage();
//            this.cov.setHighestCoverage(0);
//            this.trackInfo.setCoverage(this.cov);
//
//            this.computeAutomaticScaling();
//            this.computeScaleStep();
//            this.covLoaded = true;
//            this.repaint();
//            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//
    }


    @Override
    //Wenn es Abfragen gibt, die übersprungen werden müssen in der receiveData()
    public void notifySkipped() {
        //TODO
    }


    public void setVariants( List<VariantContext> variantList ) {
        this.snpVcfList = variantList;
    }


    /**
     * Automatically detects the most suitable scaling value to fit the coverage
     * to the track viewer.
     * This Method transforms highest coverage to slider value, where the slider
     * values range from 1-200. A scaleFactor of 1 means a 1:1 translation of
     * coverage to pixels. A larger scaleFactor means, that the coverage is
     * shrinked to fit the available painting area.
     */
//    private void computeAutomaticScaling() {
//        if (this.automaticScaling && this.cov != null && this.verticalSlider != null) {
//            double oldScaleFactor = this.scaleFactor;
//            double availablePixels = this.getPaintingAreaInfo().getAvailableForwardHeight();
//            this.scaleFactor = Math.ceil(this.cov.getHighestCoverage() / availablePixels);
//            this.scaleFactor = (this.scaleFactor < 1 ? 1.0 : this.scaleFactor);
//
//            //set the inverse of the value set in verticalZoomLevelUpdated
//            this.verticalSlider.setValue((int) (Math.ceil(Math.sqrt(this.scaleFactor))));
//            if (oldScaleFactor != this.scaleFactor) {
//                this.createCoveragePaths();
//                this.repaint();
//            }
}
