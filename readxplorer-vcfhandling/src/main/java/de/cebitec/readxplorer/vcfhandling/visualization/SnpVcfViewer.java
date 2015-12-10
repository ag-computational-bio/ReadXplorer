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

package de.cebitec.readxplorer.vcfhandling.visualization;


import de.cebitec.readxplorer.databackend.ThreadListener;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PhysicalBaseBounds;
import de.cebitec.readxplorer.ui.datavisualisation.basepanel.BasePanel;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
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
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Margarita Steinhauer
 */
public class SnpVcfViewer extends AbstractViewer implements ThreadListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger( SnpVcfViewer.class.getName() );

    private List<VariantContext> snpVcfList;

    private static final int VIEWER_HEIGHT = 70;
    private final int labelMargin;
    private final int scaleFactor;
    private final boolean snpsLoaded;
    private final List<VariantContext> visibleSNPs;


    private static Color snpA = new Color( 178, 34, 34 );
    private static Color snpC = new Color( 106, 90, 205 );
    private static Color snpG = new Color( 0, 0, 128 );
    private static Color snpT = new Color( 255, 140, 0 );
    private static Color snpN = new Color( 0, 0, 0 );
    private final boolean colorChanges = false;
    private final boolean automaticScaling = false;
    private final JSlider verticalSlider = null;


    /**
     * Create a new panel to show SNP information
     * <p>
     * @param boundsManager manager for component bounds
     * @param basePanel     serves as basis for other visual components
     * @param refGen        reference genome
     * @param trackCon      database connection to one track, that is displayed
     * @param combineTracks true, if the coverage of the tracks contained in the
     *                      track connector should be combined.
     */
    public SnpVcfViewer( BoundsInfoManager boundsManager, BasePanel basePanel, PersistentReference refGen ) {
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
                SnpVcfViewer.this.setColors( pref );
                repaint();
            }


        } );
        this.setViewerSize();

    }


    /**
     * Sets the Colors for each SNP.
     * <p>
     * @param pref
     */
    private void setColors( Preferences pref ) {
        boolean uniformColouration = pref.getBoolean( "uniformDesired", false );
        if( uniformColouration ) {
            String colourRGB = pref.get( "uniformColour", "" );
            //TODO check if this method is used anywhere
            if( !colourRGB.isEmpty() ) {
                snpA = new Color( Integer.parseInt( colourRGB ) );
                snpC = new Color( Integer.parseInt( colourRGB ) );
                snpG = new Color( Integer.parseInt( colourRGB ) );
                snpT = new Color( Integer.parseInt( colourRGB ) );
                snpN = new Color( Integer.parseInt( colourRGB ) );

            }
        } else {
            String snpAColor = pref.get( "SNP: A", "" );
            String snpCColor = pref.get( "SNP: C", "" );
            String snpGColor = pref.get( "SNP: G", "" );
            String snpTColor = pref.get( "SNP: T", "" );
            String snpNColor = pref.get( "SNP: N", "" );

            if( !snpAColor.isEmpty() ) {
                snpA = new Color( Integer.parseInt( snpAColor ) );
            }
            if( !snpCColor.isEmpty() ) {
                snpC = new Color( Integer.parseInt( snpCColor ) );
            }
            if( !snpGColor.isEmpty() ) {
                snpG = new Color( Integer.parseInt( snpGColor ) );
            }
            if( !snpTColor.isEmpty() ) {
                snpT = new Color( Integer.parseInt( snpTColor ) );
            }
            if( !snpNColor.isEmpty() ) {
                snpN = new Color( Integer.parseInt( snpNColor ) );
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
                                g.setColor( snpA );
                                break;
                            case "C":
                                g.setColor( snpC );
                                break;
                            case "G":
                                g.setColor( snpG );
                                break;
                            case "T":
                                g.setColor( snpT );
                                break;
                            case "N":
                                g.setColor( snpN );
                                break;
                            default:
                                LOG.info( "Encountered unknown SNP nucleotide." );
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
        this.setPreferredSize( new Dimension( 1, VIEWER_HEIGHT ) );
        this.revalidate();
    }


    @Override
    protected int getMaximalHeight() {
        return VIEWER_HEIGHT;
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
     * to the track viewer. This Method transforms highest coverage to slider
     * value, where the slider values range from 1-200. A scaleFactor of 1 means
     * a 1:1 translation of coverage to pixels. A larger scaleFactor means, that
     * the coverage is shrinked to fit the available painting area.
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
