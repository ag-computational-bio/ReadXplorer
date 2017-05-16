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

package de.cebitec.readxplorer.ui.datavisualisation.referenceviewer;


import de.cebitec.readxplorer.api.constants.Colors;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.ui.dialogmenus.MenuItemFactory;
import de.cebitec.readxplorer.utils.SequenceUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the content of a feature and takes care of the painting process.
 * Also contains its popup menu.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class JFeature extends JComponent {

    private static final Logger LOG = LoggerFactory.getLogger( JFeature.class.getName() );

    private static final long serialVersionUID = 347348234;
    public static final int NORMAL_HEIGHT = 12;
    public static final int PARENT_FEATURE_HEIGHT = 8;
    public static final byte BORDER_NONE = 0;
    public static final byte BORDER_LEFT = -1;
    public static final byte BORDER_RIGHT = 1;
    public static final byte BORDER_BOTH = 2;
    private final short border;
    private final int height;
    private final Font font;
    private final PersistentFeature feature;
    private final Dimension size;
    private Color color;


    /**
     * Contains the content of a feature and takes care of the painting process.
     * Also contains its popup menu.
     * <p>
     * @param feature   the feature to display
     * @param length    length of the feature on the screen
     * @param refViewer the reference viewer on which the feature is displayed
     * @param border    value among JFeature.BORDER_NONE, JFeature.BORDER_LEFT,
     *                  JFeature.BORDER_RIGHT, JFeature.BORDER_BOTH
     */
    public JFeature( final PersistentFeature feature, double length, final ReferenceViewer refViewer, short border ) {
        super();
        this.feature = feature;
        this.height = NORMAL_HEIGHT;
        this.size = new Dimension( (int) length, height );
        this.setSize( size );
        this.font = new Font( Font.MONOSPACED, Font.PLAIN, 10 );
        this.color = this.determineColor( feature );
        this.border = border;

        this.addListeners( refViewer );
        this.setToolTipText( createToolTipText() );
    }


    /**
     * @return The underlying persistent feature
     */
    public PersistentFeature getPersistentFeature() {
        return feature;
    }


    /**
     * @return The tool tip text with detailed information for this feature
     */
    private String createToolTipText() {
        StringBuilder sb = new StringBuilder( 100 );
        sb.append( "<html>" );
        sb.append( "<table>" );

        sb.append( createTableRow( "Locus", feature.getLocus() ) );
        sb.append( createTableRow( "Type", feature.getType().toString() ) );
        sb.append( createTableRow( "Strand", (feature.isFwdStrand() ? "forward" : "reverse") ) );
        sb.append( createTableRow( "Start", String.valueOf( feature.getStart() ) ) );
        sb.append( createTableRow( "Stop", String.valueOf( feature.getStop() ) ) );
        if( feature.getProduct() != null && !feature.getProduct().isEmpty() ) {
            sb.append( createTableRow( "Product", feature.getProduct() ) );
        }
        if( feature.getEcNumber() != null && !feature.getEcNumber().isEmpty() ) {
            sb.append( createTableRow( "EC no.", feature.getEcNumber() ) );
        }

        sb.append( "</table>" );
        sb.append( "</html>" );
        return sb.toString();
    }


    /**
     * A table row for the tool tip
     *
     * @param label The label to display
     * @param value The value to display
     *
     * @return The string for the tool tip
     */
    private String createTableRow( String label, String value ) {
        return "<tr><td align=\"right\"><b>" + label + ":</b></td><td align=\"left\">" + value + "</td></tr>";
    }


    /**
     * @param selected true if this feature is selected, false otherwise
     */
    public void setSelected( boolean selected ) {
        if( selected ) {
            color = Colors.SELECTED_FEATURE;
        } else {
            color = this.determineColor( feature );
        }
        this.repaint();
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void paintComponent( Graphics graphics ) {
        Graphics2D g = (Graphics2D) graphics;

        // draw the rectangle
        g.setColor( color );
        if( feature.getNodeChildren().isEmpty() ) {
            g.fillRect( 0, 0, this.getSize().width, this.height );
            g.setColor( Colors.EXON_BORDER );
            g.drawRect( 0, 0, this.getSize().width - 1, this.height - 1 );
            //paint border in feature color, if feature is larger than screen at that border
            g.setColor( color );
            this.overpaintBorder( g, 0, this.height - 1 );
        } else { //features with sub features have a smaller height
            g.fillRect( 0, (NORMAL_HEIGHT - PARENT_FEATURE_HEIGHT) / 2, this.getSize().width, PARENT_FEATURE_HEIGHT );
            g.setColor( Colors.EXON_BORDER );
            g.drawRect( 0, (NORMAL_HEIGHT - PARENT_FEATURE_HEIGHT) / 2, this.getSize().width - 1, PARENT_FEATURE_HEIGHT - 1 );
            g.setColor( color );
            this.overpaintBorder( g, (NORMAL_HEIGHT - PARENT_FEATURE_HEIGHT) / 2 + 1, PARENT_FEATURE_HEIGHT );
        }

        // draw the locus of the feature inside the rectangle
        g.setColor( Colors.FEATURE_LABEL );
        g.setFont( font );
        FontMetrics fm = g.getFontMetrics();

        int fontY = this.getHeight() / 2 - 2 + fm.getMaxAscent() / 2;
        if( feature.hasLocus() ) {
            String label = this.determineLabel( feature.getLocus(), fm );
            g.drawString( label, 5, fontY );
        }

    }


    /**
     * Overpaints the border of the feature again with a line, if it is larger
     * than the screen and continues at the border.
     * <p>
     * @param g  graphics object to paint on
     * @param y1 first y value of the line to draw
     * @param y2 second y value of the line to draw
     */
    private void overpaintBorder( Graphics2D g, int y1, int y2 ) {
        switch( this.border ) {
            case JFeature.BORDER_BOTH:
                g.drawLine( 0, y1, 0, y2 );
                g.drawLine( this.getSize().width - 1, y1, this.getSize().width - 1, y2 );
                break;
            case JFeature.BORDER_LEFT:
                g.drawLine( 0, y1, 0, y2 );
                break;
            case JFeature.BORDER_RIGHT:
                g.drawLine( this.getSize().width - 1, y1, this.getSize().width - 1, y2 );
                break;
            default:
                break;
        }
    }


    /**
     * Determine the label of the feature
     *
     * @param text The original text to display
     * @param fm   The font metrics to use
     *
     * @return The reworked feature label string
     */
    private String determineLabel( String text, FontMetrics fm ) {
        // cut down the string if it extends the width of this component
        if( fm.stringWidth( text ) > this.getWidth() - 10 ) {
            while( fm.stringWidth( text + "..." ) > this.getWidth() - 10 && text.length() > 0 ) {
                text = text.substring( 0, text.length() - 1 );
            }
            text += "...";
        }
        return text;
    }


    /**
     * Set the color a feature is displayed with. Depends on the feature type.
     * <p>
     * @param feature the feature
     * <p>
     * @return the color for this feature
     */
    private Color determineColor( PersistentFeature feature ) {
        Color c;

        switch( feature.getType() ) {
            case CDS:
                c = Colors.CDS;
                break;
            case MRNA:
                c = Colors.MRNA;
                break;
            case MISC_RNA:
                c = Colors.MISC_RNA;
                break;
            case REPEAT_UNIT:
                c = Colors.REPEAT_UNIT;
                break;
            case RRNA:
                c = Colors.RRNA;
                break;
            case SOURCE:
                c = Colors.SOURCE;
                break;
            case TRNA:
                c = Colors.TRNA;
                break;
            case GENE:
                c = Colors.GENE;
                break;
            case MIRNA:
                c = Colors.MI_RNA;
                break;
            case EXON:
                c = Colors.EXON;
                break;
            case UNDEFINED:
                c = Colors.UNDEF_FEATURE;
                break;
            case FIVE_UTR:
                c = Colors.FIVE_UTR;
                break;
            case THREE_UTR:
                c = Colors.THREE_UTR;
                break;
            case NC_RNA:
                c = Colors.NC_RNA;
                break;
            case RBS:
                c = Colors.RBS;
                break;
            case MINUS_THIRTYFIVE:
                c = Colors.MINUS_THIRTYFIVE;
                break;
            case MINUS_TEN:
                c = Colors.MINUS_TEN;
                break;
            case TRANSCRIPT:
                c = Colors.TRANSCRIPT;
                break;
            default:
                LOG.error( "Found unknown type for feature {0}", feature.getType() );
                c = Colors.UNDEF_FEATURE;
        }

        return c;
    }


    /**
     * Add all necessary listeners.
     *
     * @param refViewer The viewer in which the feature is displayed
     */
    private void addListeners( final ReferenceViewer refViewer ) {
        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getButton() == MouseEvent.BUTTON1 ) {
                    refViewer.setSelectedFeature( JFeature.this );
                }
                showPopUp( e );
            }


            @Override
            public void mousePressed( MouseEvent e ) {
                showPopUp( e );
            }


            @Override
            public void mouseReleased( MouseEvent e ) {
            }


            @Override
            public void mouseEntered( MouseEvent e ) {
            }


            @Override
            public void mouseExited( MouseEvent e ) {
            }


            private void showPopUp( MouseEvent e ) {
                if( (e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger()) ) {
                    final Lookup.Result<ReferenceViewer> resultReferenceView = Utilities.actionsGlobalContext().lookupResult( ReferenceViewer.class );
                    final ReferenceViewer viewer = resultReferenceView.allInstances().iterator().next();

                    JPopupMenu popUp = new JPopupMenu();

                    //add thumbnail view options
                    final IThumbnailView thumb = Lookup.getDefault().lookup( IThumbnailView.class );
                    if( thumb != null ) {
                        thumb.showPopUp( feature, refViewer, e, popUp );
                    }

                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    String selFeatureSequence = viewer.getReference().getActiveChromSequence( feature.getStart(), feature.getStop() );
                    if( !feature.isFwdStrand() ) {
                        selFeatureSequence = SequenceUtils.getReverseComplement( selFeatureSequence );
                    }
                    //add copy option
                    popUp.add( menuItemFactory.getCopyItem( selFeatureSequence ) );
                    //add copy translated feature sequence option
                    popUp.add( menuItemFactory.getCopyTranslatedItem( selFeatureSequence ) );
                    //add copy position option
                    popUp.add( menuItemFactory.getCopyPositionItem( refViewer.getCurrentMousePos() ) );
                    //add center current position option
                    popUp.add( menuItemFactory.getJumpToPosItem( refViewer.getBoundsInformationManager(), refViewer.getCurrentMousePos() ) );
                    //add store as fasta file option
                    popUp.add( menuItemFactory.getStoreFastaItem( selFeatureSequence, refViewer.getReference().getName(), feature ) );
                    //add store transalted sequence as fasta file option
                    popUp.add( menuItemFactory.getStoreTranslatedFeatureFastaItem( selFeatureSequence, refViewer.getReference().getName(), feature ) );

                    popUp.show( e.getComponent(), e.getX(), e.getY() );
                }
            }


        } );

        this.addMouseMotionListener( new MouseMotionListener() {

            @Override
            public void mouseDragged( MouseEvent e ) {
            }


            @Override
            public void mouseMoved( MouseEvent e ) {
                refViewer.forwardChildrensMousePosition( e.getX(), JFeature.this );
            }


        } );
    }


}
