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

package de.cebitec.readxplorer.ui.datavisualisation.alignmentviewer;


import de.cebitec.readxplorer.api.Classification;
import de.cebitec.readxplorer.api.constants.Colors;
import de.cebitec.readxplorer.databackend.SamBamFileReader;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.Difference;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.ObjectWithId;
import de.cebitec.readxplorer.databackend.dataobjects.ReferenceGap;
import de.cebitec.readxplorer.ui.datavisualisation.GenomeGapManager;
import de.cebitec.readxplorer.ui.datavisualisation.PaintUtilities;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.PhysicalBaseBounds;
import de.cebitec.readxplorer.ui.dialogmenus.MenuItemFactory;
import de.cebitec.readxplorer.ui.dialogmenus.RNAFolderI;
import de.cebitec.readxplorer.utils.ColorUtils;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import de.cebitec.readxplorer.utils.SequenceUtils;
import de.cebitec.readxplorer.utils.sequence.GenomicRange;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

import static de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer.PROP_MOUSEPOSITION_CHANGED;
import static java.util.logging.Level.SEVERE;


/**
 * A <code>BlockComponent</code> represents a read alignment as a colored
 * rectangle and has knowledge of all important information of the alignment.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class BlockComponent extends JComponent {

    private static final Logger LOG = Logger.getLogger( BlockComponent.class.getName() );


    private static final long serialVersionUID = 1324672345;
    private final BlockI block;
    private final int length;
    private final int height;
    private final AbstractViewer parentViewer;
    private final GenomeGapManager gapManager;
    private final int absLogBlockStart;
    private final int absLogBlockStop;
    private final int phyLeft;
    private int phyRight;
    private final String toolTipInfoPart;
    private final List<Rectangle> rectList;
    private final List<BrickData> brickDataList;
    private Color blockColor;
    private final boolean showBaseQualities;
    private final Map<Classification, Color> classToColorMap;


    /**
     * A <code>BlockComponent</code> represents a read alignment as a colored
     * rectangle and has knowledge of all important information of the
     * alignment.
     * <p>
     * @param block             The block representing a read alignment
     *                          (mapping)
     * @param parentViewer      The parent viewer in which this block shall be
     *                          shown
     * @param gapManager        The gap manager for this alignment
     * @param height            The height of this block
     * @param showBaseQualities <code>true</code> if each base of the alignment
     *                          is shaded by base quality, <code>false</code>
     *                          otherwise
     */
    public BlockComponent( BlockI block, final AbstractViewer parentViewer, GenomeGapManager gapManager,
                           int height,
                           boolean showBaseQualities ) {

        super();
        this.classToColorMap = ColorUtils.updateMappingClassColors();
        this.rectList = new ArrayList<>();
        this.brickDataList = new ArrayList<>();
        this.blockColor = Colors.COMMON_MATCH;
        this.block = block;
        this.height = height;
        this.parentViewer = parentViewer;
        this.absLogBlockStart = block.getStart();
        this.absLogBlockStop = block.getStop();
        this.showBaseQualities = showBaseQualities;
        this.gapManager = gapManager;

        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( absLogBlockStart );
        this.phyLeft = (int) bounds.getLeftPhysBound();
        // if there is a gap at the end of this block, phyRight shows the right bound of the gap (in viewer)
        // thus forgetting about every following matches, diffs, gaps whatever....
        this.phyRight = (int) parentViewer.getPhysBoundariesForLogPos( absLogBlockStop ).getRightPhysBound();
        int numOfGaps = this.gapManager.getNumOfGapsAt( absLogBlockStop );
        int offset = (int) (numOfGaps * bounds.getPhysWidth());
        phyRight += offset;
        this.length = phyRight - phyLeft;

        this.calcSubComponents();
        toolTipInfoPart = this.initToolTipTextInfoPart();

        this.addListeners();
    }


    /**
     * Adds all necessary listeners to the component.
     */
    private void addListeners() {
        this.parentViewer.addPropertyChangeListener( new PropertyChangeListener() {

            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                if( evt.getPropertyName().equals( PROP_MOUSEPOSITION_CHANGED ) ) {
                    setToolTipText( createToolTipText() );
                }
            }


        } );

        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getButton() == MouseEvent.BUTTON3 ) {
                    JPopupMenu popUp = new JPopupMenu();
                    MenuItemFactory menuItemFactory = new MenuItemFactory();

                    final String mappingSequence = getSequence();
                    //add copy option
                    popUp.add( menuItemFactory.getCopyItem( mappingSequence ) );
                    //add copy translated sequence option
                    popUp.add( menuItemFactory.getCopyTranslatedItem( mappingSequence ) );
                    //add copy position option
                    popUp.add( menuItemFactory.getCopyPositionItem( parentViewer.getCurrentMousePos() ) );
                    //add center current position option
                    popUp.add( menuItemFactory.getJumpToPosItem( parentViewer.getBoundsInformationManager(), parentViewer.getCurrentMousePos() ) );
                    //add calculate secondary structure option
                    final RNAFolderI rnaFolderControl = Lookup.getDefault().lookup( RNAFolderI.class );
                    if( rnaFolderControl != null ) {
                        popUp.add( menuItemFactory.getRNAFoldItem( rnaFolderControl, mappingSequence, this.getHeader() ) );
                    }

                    popUp.show( e.getComponent(), e.getX(), e.getY() );
                }
            }


            /**
             * Creates the header for the highlighted sequence.
             * <p>
             * @return the header for the sequence
             */
            private String getHeader() {
                Mapping mapping = (Mapping) BlockComponent.this.block.getObjectWithId();
                final String strand = mapping.isFwdStrand() ? ">>" : "<<";
                Map<Integer, String> trackNames = ProjectConnector.getInstance().getOpenedTrackNames();
                String name = "Reference seq from ";
                if( trackNames.containsKey( mapping.getTrackId() ) ) {
                    name += trackNames.get( mapping.getTrackId() );
                }
                return name + " " + strand + " from " + absLogBlockStart + "-" + absLogBlockStop;
            }


            @Override
            public void mousePressed( MouseEvent e ) {
                //not in use
            }


            @Override
            public void mouseReleased( MouseEvent e ) {
                //not in use
            }


            @Override
            public void mouseEntered( MouseEvent e ) {
                parentViewer.forwardChildrensMousePosition( e.getX(), BlockComponent.this );
            }


            @Override
            public void mouseExited( MouseEvent e ) {
                //not in use
            }


        } );
    }


    /**
     * @return The reference sequence
     */
    public String getSequence() {
        int start = ((GenomicRange) block.getObjectWithId()).getStart();
        int stop = ((GenomicRange) block.getObjectWithId()).getStop();
        //string first pos is zero
        String readSequence = parentViewer.getReference().getActiveChromSequence( start - 1, stop );
        return readSequence;
    }


    /**
     * @return Creates the tool tip text for this mapping block.
     */
    private String createToolTipText() {
        StringBuilder sb = new StringBuilder( 150 );

        sb.append( "<html><table>" );
        sb.append( createTableRow( "Current position", String.valueOf( parentViewer.getCurrentMousePos() ) ) );
        sb.append( toolTipInfoPart );

        return sb.toString();
    }


    /**
     * @return Initializes the tool tip text for this mapping block for the
     *         first time.
     */
    private String initToolTipTextInfoPart() {
        StringBuilder sb = new StringBuilder( 150 );
        Mapping mapping = ((Mapping) block.getObjectWithId());

        sb.append( createTableRow( "Start", String.valueOf( mapping.getStart() ) ) );
        sb.append( createTableRow( "Stop", String.valueOf( mapping.getStop() ) ) );
//        this.appendReadnames(mapping, sb); //no readnames are stored anymore: RUN domain excluded
        sb.append( createTableRow( "Mismatches", String.valueOf( mapping.getDifferences() ) ) );
        if( mapping.getAlignmentBlocks().size() > 1 ) {
            sb.append( createTableRow( "Alignment blocks", this.printBlocks( mapping.getAlignmentBlocks() ) ) );
        }
        int mappingQual = mapping.getMappingQuality() == -1 ? SamBamFileReader.DEFAULT_MAP_QUAL : mapping.getMappingQuality();
        sb.append( createTableRow( "Mapping quality (Phred)", String.valueOf( mappingQual ) ) );
        sb.append( createTableRow( "Base qualities (Phred)", this.generateBaseQualString( mapping.getBaseQualities() ) ) );
        this.appendDiffs( mapping, sb );
        this.appendGaps( mapping, sb );

        if( mapping.isUnique() ) {
            sb.append( createTableRow( "Unique", "yes" ) );
        } else {
            sb.append( createTableRow( "Unique", "no" ) );
        }
        sb.append( createTableRow( "Number of mappings for read", mapping.getNumMappingsForRead() + "" ) );
        if( mapping.getOriginalSequence() != null ) {
            sb.append( createTableRow( "Original (full) sequence", mapping.getOriginalSequence() ) );
        }
        if( mapping.getTrimmedFromLeft() > 0 ) {
            sb.append( createTableRow( "Trimmed chars from left", mapping.getTrimmedFromLeft() + "" ) );
        }
        if( mapping.getTrimmedFromRight() > 0 ) {
            sb.append( createTableRow( "Trimmed chars from right", mapping.getTrimmedFromRight() + "" ) );
        }

        sb.append( "</table></html>" );

        return sb.toString();
    }


    /**
     * Prints the alignment blocks into a String (e.g. (15, 22), (45, 70)).
     * <p>
     * @param alignmentBlocks The list of alignment blocks
     * <p>
     * @return The string representing the alignment blocks
     */
    private String printBlocks( List<SamAlignmentBlock> alignmentBlocks ) {
        StringBuilder builder = new StringBuilder( 10 );
        for( SamAlignmentBlock aBlock : alignmentBlocks ) {
            builder.append( "(" );
            builder.append( aBlock.getRefStart() );
            builder.append( ", " );
            builder.append( aBlock.getRefStop() );
            builder.append( "), " );
        }
        builder.delete( builder.length() - 2, builder.length() - 1 );
        return builder.toString();
    }


    /**
     * @param baseQualities The array of phred scaled base qualities to convert
     * <p>
     * @return A String representation of the phred scaled base qualities in the
     *         array.
     */
    private String generateBaseQualString( byte[] baseQualities ) {
        String baseQualString = "[";
        int aThird = baseQualities.length / 4;
        int current = aThird;
        for( int i = 0; i < baseQualities.length; i++ ) {
            baseQualString += baseQualities[i] + ",";
            if( i > current ) {
                baseQualString += "<br>";
                current += aThird;
            }
        }
        if( baseQualString.endsWith( "," ) ) {
            baseQualString = baseQualString.substring( 0, baseQualString.length() - 1 ) + "]";
        } else if( baseQualString.endsWith( "<br>" ) ) {
            baseQualString = baseQualString.substring( 0, baseQualString.length() - 5 ) + "]";
        } else if( baseQualString.length() == 1 ) {
            baseQualString = "";
        }
        return baseQualString;
    }


    private void appendDiffs( Mapping mapping, StringBuilder sb ) {
        boolean printLabel = true;
        for( Difference d : mapping.getDiffs().values() ) {
            String key = "";
            if( printLabel ) {
                key = "Differences to reference";
                printLabel = false;
            }
            sb.append( createTableRow( key, d.getBase() + " at " + d.getPosition() ) );
        }
    }


    private void appendGaps( Mapping mapping, StringBuilder sb ) {
        boolean printLabel = true;
        for( Integer pos : mapping.getGenomeGaps().keySet() ) {
            String key = "";
            if( printLabel ) {
                key = "Reference insertions";
                printLabel = false;
            }
            StringBuilder tmp = new StringBuilder( 10 );
            for( ReferenceGap g : mapping.getGenomeGapsAtPosition( pos ) ) {
                tmp.append( g.getBase() ).append( ", " );
            }
            tmp.deleteCharAt( tmp.toString().lastIndexOf( ',' ) );
            tmp.append( " at " ).append( pos );
            String value = tmp.toString();
            sb.append( createTableRow( key, value ) );
        }

    }


    private String createTableRow( String key, String value ) {
        if( !key.isEmpty() ) {
            key += ":";
        }
        return "<tr><td align=\"right\">" + key + "</td><td align=\"left\">" + value + "</td>";
    }


    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setFont( new Font( Font.MONOSPACED, Font.BOLD, 11 ) );

        // paint this block's background
        graphics2D.setColor(Colors.BLOCK_BACKGROUND );
        graphics2D.fillRect( 0, 0, length, height );

        //paint SamAlignmentBlocks (for split read mappings)
        for( Rectangle rectangle : rectList ) {
            graphics2D.setColor( this.blockColor );
            graphics2D.fill( rectangle );
        }

        //paint all diffs and gaps = bricks
        for( BrickData brick : brickDataList ) {
            graphics2D.setColor( brick.getBrickColor() );
            graphics2D.fill( brick.getRectangle() );
            if( parentViewer.isInMaxZoomLevel() && height >= AlignmentViewer.DEFAULT_BLOCK_HEIGHT ) {
                int labelWidth = graphics.getFontMetrics().stringWidth( brick.toString() );
                int labelX = brick.getLabelCenter() - labelWidth / 2;
                graphics2D.setColor(Colors.BRICK_LABEL );
                graphics2D.drawString( brick.toString(), labelX, height );
            }
        }
    }


    /**
     * Calculates all subcomponents of this block component.
     */
    private void calcSubComponents() {

        Mapping mapping = (Mapping) block.getObjectWithId();
        this.calcAlignmentBlocks( block.getObjectWithId() );

        // only count Bricks, that are no genome gaps.
        //Used for determining location of brick in viewer
        int brickCount = 0;
        int gapCount = 0;
        for( Iterator<Brick> it = block.getBrickIterator(); it.hasNext(); ) {
            Brick brick = it.next();

            if( brick != Brick.MATCH || showBaseQualities ) {
                // get start of brick
                int logBrickStart = absLogBlockStart + brickCount;
                PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( logBrickStart );
                int x1 = (int) bounds.getLeftPhysBound() - phyLeft;
                int labelCenter = ((int) bounds.getPhyMiddle() - phyLeft);

                // if Brick before was a gap, this brick has the same position
                // in the genome as the gap. This forces the viewer to map to
                // the same position, which would lead to this Brick being painted
                // at the same location as the gap. So increase values manually
                if( gapCount > 0 ) {
                    x1 += bounds.getPhysWidth() * gapCount;
                    labelCenter += bounds.getPhysWidth() * gapCount;
                }
                Rectangle rectangle = new Rectangle( x1, 0, (int) Math.ceil( bounds.getPhysWidth() ), height );

                Color brickColor = null;
                if( brick != Brick.MATCH ) {
                    brickColor = this.determineMismatchBrickColor( brick );

                    switch( brick ) {
                        case FOREIGN_GENOMEGAP:
                        case GENOMEGAP_A:
                        case GENOMEGAP_C:
                        case GENOMEGAP_G:
                        case GENOMEGAP_T:
                        case GENOMEGAP_N:
                            --brickCount;
                            ++gapCount;
                            break;
                        default:
                            gapCount = 0;
                    }

                } else {
                    if( mapping.getBaseQualities().length > brickCount ) {
                        brickColor = ColorUtils.getAdaptedColor( mapping.getBaseQualities()[brickCount], SequenceUtils.MAX_PHRED, blockColor );
                    }
                    gapCount = 0;
                }

                if( brickColor != null ) {
                    this.brickDataList.add( new BrickData( brick, rectangle, brickColor, labelCenter ) );
                }
            } else {
                gapCount = 0;
            }

            ++brickCount;
        }
    }


    /**
     * Calculates the alignment blocks to paint for the given mapping.
     * <p>
     * @param objectWithId The ObjectWithId, which should be a Mapping
     */
    private void calcAlignmentBlocks( ObjectWithId objectWithId ) {
        ObjectWithId persObj = objectWithId;
        if( persObj instanceof Mapping ) {
            this.blockColor = this.determineBlockColor();
            Mapping mapping = (Mapping) persObj;

            if( mapping.getAlignmentBlocks().isEmpty() ) {
                Rectangle blockRect = PaintUtilities.calcBlockBoundaries(
                        mapping.getStart(), mapping.getStop(), parentViewer, phyLeft, height );
                this.rectList.add( blockRect );
            } else {
                for( SamAlignmentBlock aBlock : mapping.getAlignmentBlocks() ) {
                    Rectangle blockRect = PaintUtilities.calcBlockBoundaries(
                            aBlock.getRefStart(), aBlock.getRefStop(), parentViewer, phyLeft, height );
                    this.rectList.add( blockRect );
                }
            }
        }
    }


    /**
     * Determines the color of a block.
     * <p>
     * @return The color of the block.
     */
    private Color determineBlockColor() {
        Mapping m = ((Mapping) block.getObjectWithId());
        return this.classToColorMap.get( m.getMappingClass() );
    }


    /**
     * Determines the color of a brick, if it deviates from the reference.
     * Matches are not taken into account in this method.
     * <p>
     * @param brick the non-matching brick (base) whose color is needed
     * <p>
     * @return the color of the non-matching brick
     */
    private Color determineMismatchBrickColor( Brick brick ) {
        Color c;
        switch( brick ) {
            case BASE_A: //fallthrough
            case BASE_C: //fallthrough
            case BASE_G: //fallthrough
            case BASE_T: //fallthrough
            case BASE_N: //fallthrough
            case READGAP: //fallthrough
            case GENOMEGAP_A: //fallthrough
            case GENOMEGAP_C: //fallthrough
            case GENOMEGAP_G: //fallthrough
            case GENOMEGAP_T: //fallthrough
            case GENOMEGAP_N:
                c = Colors.MISMATCH_BACKGROUND;
                break;
            case SKIPPED:
                c = Colors.SKIPPED;
                break;
            case FOREIGN_GENOMEGAP:
                c = Colors.ALIGNMENT_FOREIGN_GENOMEGAP;
                break;
            case TRIMMED:
                c = Colors.TRIMMED;
                break;
            case UNDEF:
                c = Colors.MISMATCH_BACKGROUND;
                LOG.log( SEVERE, "found unknown brick type {0}", brick );
                break;
            default:
                c = Colors.MISMATCH_BACKGROUND;
                LOG.log( SEVERE, "found unknown brick type {0}", brick );
        }

        return c;
    }


    /**
     * @return the left physical boundary (pixel) of the block.
     */
    public int getPhyStart() {
        return phyLeft;
    }


    /**
     * @return the physical width (pixel) of the block.
     */
    public int getPhyWidth() {
        return length;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return height;
    }


}
