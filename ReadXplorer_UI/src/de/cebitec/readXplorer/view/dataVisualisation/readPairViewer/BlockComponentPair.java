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
package de.cebitec.readXplorer.view.dataVisualisation.readPairViewer;


import de.cebitec.readXplorer.databackend.dataObjects.Mapping;
import de.cebitec.readXplorer.databackend.dataObjects.ReadPair;
import de.cebitec.readXplorer.databackend.dataObjects.ReadPairGroup;
import de.cebitec.readXplorer.util.ColorProperties;
import de.cebitec.readXplorer.util.ReadPairType;
import de.cebitec.readXplorer.util.classification.Classification;
import de.cebitec.readXplorer.util.classification.FeatureType;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.view.dataVisualisation.PaintUtilities;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.PhysicalBaseBounds;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.openide.util.NbBundle;


/**
 * A BlockComponent represents one read pair and displays all mappings of the
 * pair in the currently shown interval of the genome.
 * TODO: think about overlaps in pairs: enlarge layer height!
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class BlockComponentPair extends JComponent implements ActionListener {

    private static final long serialVersionUID = 1324672345;
    private static final float satAndBrightSubtrahend = 0.3f;

    /*
     * In order to be efficient this class holds its main information in 4 arraylists.
     * Therefore it is important to check that the rectangles representing mappings,
     * their corresponding colors and their mappingType are added in the SAME order
     * to the lists, to have access to one mappings data via the same index in
     * all three arrays. The line list is a bit independent, since it is always grey.
     */

    private ArrayList<Rectangle> rectList = new ArrayList<>();
    private ArrayList<Color> colorList = new ArrayList<>();
    private ArrayList<Line2D> lineList = new ArrayList<>();
    private ArrayList<Color> pairColors = new ArrayList<>();

    private BlockPair block;
    private int length;
    private int height;
    private AbstractViewer parentViewer;
    private int phyLeft;
    private int phyRight;
    private float minSatAndBright;
    private static String COPY_SEQUENCE = NbBundle.getMessage( BlockComponentPair.class, "CopyRefSeq" );
    private static String COPY_TOOLTIP = NbBundle.getMessage( BlockComponentPair.class, "CopyAttention" );
    private JPopupMenu copyMenu = new JPopupMenu();
    private ReadPairPopup readPairPopup;
    private static JMenuItem COPY_SEQUENCE_ITEM;


    public BlockComponentPair( BlockPair block, final AbstractViewer parentViewer, int height, float minSaturationAndBrightness ) {
        this.block = block;
        this.height = height;
        this.parentViewer = parentViewer;
        this.minSatAndBright = minSaturationAndBrightness;
        this.initCopySeqItem();
        // component boundaries //
        PhysicalBaseBounds bounds = parentViewer.getPhysBoundariesForLogPos( block.getAbsStart() );
        this.phyLeft = (int) bounds.getLeftPhysBound();
        this.phyRight = (int) parentViewer.getPhysBoundariesForLogPos( block.getAbsStop() ).getRightPhysBound();
        this.length = phyRight - phyLeft;
        this.length = this.length < 3 ? 3 : this.length;

        this.setPopupMenu();
        this.calcMappingBoundaries();

        this.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getButton() == MouseEvent.BUTTON1 ) {
                    if( readPairPopup == null ) {
                        createPopup( e );
                    }
                    readPairPopup.show( e.getComponent(), e.getX(), e.getY() );
                }
                if( e.getButton() == MouseEvent.BUTTON3 ) {
                    copyMenu.show( BlockComponentPair.this, e.getX(), e.getY() );
                }
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
                parentViewer.forwardChildrensMousePosition( e.getX(), BlockComponentPair.this );
            }


            @Override
            public void mouseExited( MouseEvent e ) {
                //not in use
            }


        } );

    }


    /**
     * Calculates the boundaries for each visible mapping and creates the visual
     * content of the BlockComponentPair
     */
    private void calcMappingBoundaries() {
        ReadPairGroup readPairGroup = (ReadPairGroup) this.block.getObjectWithId();
        List<ReadPair> readPairs = readPairGroup.getReadPairs();
        List<Mapping> singleMappings = readPairGroup.getSingleMappings();

        Color blockColor;
        ReadPair readPair;
        Mapping mapping;

        for( int i = 0; i < readPairs.size(); ++i ) {
            readPair = readPairs.get( i );
            mapping = readPair.getVisibleMapping();
            if( !this.inExclusionList( readPair.getReadPairType() ) ) {

                blockColor = this.determineBlockColor( readPair );
                this.pairColors.add( blockColor );
                this.addRectAndItsColor( blockColor, mapping, false );

                if( readPair.hasVisibleMapping2() ) {
                    mapping = readPair.getVisibleMapping2();
                    this.addRectAndItsColor( blockColor, mapping, true );
                }
                this.length = this.length < 6 ? 6 : this.length;
            }
        }

        if( !parentViewer.getExcludedClassifications().contains( FeatureType.SINGLE_MAPPING ) ) {
            for( int i = 0; i < singleMappings.size(); ++i ) {
                mapping = singleMappings.get( i );
                this.addRectAndItsColor( ColorProperties.BLOCK_UNPAIRED, mapping, false );
            }
        }
    }


    /**
     * @param readPairType read pair type to check
     * <p>
     * @return true, if the given read typ is on the exclusion list
     */
    public boolean inExclusionList( ReadPairType readPairType ) {
        List<Classification> excludedFeatureTypes = this.parentViewer.getExcludedClassifications();
        FeatureType typeOfPair;
        if( readPairType == ReadPairType.PERFECT_PAIR
            || readPairType == ReadPairType.PERFECT_UNQ_PAIR ) {
            typeOfPair = FeatureType.PERFECT_PAIR;
        }
        else if( readPairType == ReadPairType.UNPAIRED_PAIR ) {
            typeOfPair = FeatureType.SINGLE_MAPPING;
        }
        else {
            typeOfPair = FeatureType.DISTORTED_PAIR;
        }

        return excludedFeatureTypes.contains( typeOfPair );
    }


    /**
     * Determines the bounds of the rectangle representing the mapping and
     * adjusts its color
     * depending on the mapping type. Also paints the line for a read pair, if
     * both mappings are visible.
     * <p>
     * @param pairColor basic color of the current read pair
     * @param mapping   mapping to create a colored rectangle for
     * @param addLine   true if this is the second mapping of a pair and a
     *                  connecting line is desired, false otherwise
     */
    private void addRectAndItsColor( Color pairColor, Mapping mapping, boolean addLine ) {
        this.colorList.add( this.adjustBlockColor( pairColor, mapping ) );
        Rectangle blockRect = PaintUtilities.calcBlockBoundaries( mapping.getStart(), mapping.getStop(), parentViewer, phyLeft, height );
        this.rectList.add( blockRect );

        if( addLine ) {
            Rectangle prevRect = rectList.get( rectList.size() - 2 );
            int startMapping1 = prevRect.x;
            int startCurMapping = blockRect.x;
            if( startMapping1 < startCurMapping ) {
                this.lineList.add( new Line2D.Double( startMapping1 + prevRect.width, 2, startCurMapping - 1, 2 ) );
            }
            else { //endMapping2 < endMapping1
//                this.lineList.add(new Line2D.Double(rect.x - 1, 2, absStopMapping, 2));
                this.lineList.add( new Line2D.Double( startCurMapping + blockRect.width, 2, startMapping1 - 1, 2 ) );
            }
        }
    }


    /**
     * Adjusts the color of a mapping according to its mapping type.
     * <p>
     * @param blockColor old color of the block for the mapping
     * @param mapping    mapping whose color needs to be adjusted
     * <p>
     * @return new color of the block for the mapping
     */
    private Color adjustBlockColor( Color blockColor, Mapping mapping ) {
        // order of addition to blockList and mappingTypeList has to be correct always!
        float hue = Color.RGBtoHSB( blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), null )[0];
        MappingClass mappingClass = mapping.getMappingClass();
        if( mappingClass == MappingClass.PERFECT_MATCH || mappingClass == MappingClass.SINGLE_PERFECT_MATCH ) {
            blockColor = Color.getHSBColor( hue, minSatAndBright, minSatAndBright );
        }
        else if( mappingClass == MappingClass.BEST_MATCH || mappingClass == MappingClass.SINGLE_BEST_MATCH ) {
            blockColor = Color.getHSBColor( hue, minSatAndBright - satAndBrightSubtrahend, minSatAndBright - satAndBrightSubtrahend );
        }
        else {
            blockColor = Color.getHSBColor( hue, minSatAndBright - satAndBrightSubtrahend * 2, minSatAndBright - satAndBrightSubtrahend * 2 );
        }
        return blockColor;
    }


    /**
     * Creates the popup menu for operating on the mapping (currently only
     * copying the sequence).
     */
    private void setPopupMenu() {
        this.copyMenu.add( COPY_SEQUENCE_ITEM );
    }


    /**
     * Initialize this copy item only once for all instances.
     */
    private void initCopySeqItem() {
        if( COPY_SEQUENCE_ITEM == null ) {
            COPY_SEQUENCE_ITEM = new JMenuItem();
            COPY_SEQUENCE_ITEM.addActionListener( this );
            COPY_SEQUENCE_ITEM.setActionCommand( BlockComponentPair.COPY_SEQUENCE );
            COPY_SEQUENCE_ITEM.setText( BlockComponentPair.COPY_SEQUENCE );
            COPY_SEQUENCE_ITEM.setToolTipText( COPY_TOOLTIP );
        }
    }


    @Override
    protected void paintComponent( Graphics graphics ) {
        super.paintComponent( graphics );
        Graphics2D graphics2D = (Graphics2D) graphics;
//        graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));

        //fill pair background to indicate pair length
        graphics2D.setColor( ColorProperties.BLOCK_BACKGROUND );
        graphics2D.fill( new Rectangle2D.Double( 0, 0, this.length, this.height ) );

        // paint this block's mappings
        for( int i = 0; i < this.rectList.size(); ++i ) {
            graphics2D.setColor( this.colorList.get( i ) );
            graphics2D.fill( this.rectList.get( i ) );
        }

        //paint connecting lines for pairs
        graphics2D.setColor( ColorProperties.CURRENT_POSITION );
        for( int i = 0; i < this.lineList.size(); ++i ) {
            graphics2D.draw( this.lineList.get( i ) );
        }

        //paint a bounding rectangle
        //graphics2D.draw(new Line2D.Double(0, this.height-1, this.length-1, this.height-1));

    }


    /**
     * Determines the color and type of a block.
     * <p>
     * @return the color representing this block
     */
    private Color determineBlockColor( ReadPair readPair ) {

        Color blockColor;
        ReadPairType type = readPair.getReadPairType();
        if( type == ReadPairType.PERFECT_PAIR || type == ReadPairType.PERFECT_UNQ_PAIR ) {
            blockColor = ColorProperties.BLOCK_PERFECT;
        }
        else if( type == ReadPairType.UNPAIRED_PAIR ) {
            blockColor = ColorProperties.BLOCK_UNPAIRED;
        }
        else {
            blockColor = ColorProperties.BLOCK_DIST_SMALL;
        }
        return blockColor;

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


    @Override
    public int getHeight() {
        return height;
    }


    @Override
    public void actionPerformed( ActionEvent e ) {
        if( e.getActionCommand().equals( COPY_SEQUENCE ) ) {
            this.setSequence();
        }
    }


    public void setSequence() {
        JTextField j = new JTextField(); //TODO: return read sequence of currently clicked read, not the pair
        int start = (int) ((ReadPair) block.getObjectWithId()).getStart();
        int stop = (int) ((ReadPair) block.getObjectWithId()).getStop();
        //string first pos is zero
        String readSequence = parentViewer.getReference().getActiveChromSequence( start - 1, stop );
        j.setText( readSequence );
        j.selectAll();
        j.copy();
    }


    /**
     * Creates the popup displaying all information regarding this read pair
     * and allowing to jump to the position of other mappings of the pair.
     */
    private void createPopup( MouseEvent e ) {
        String pairType = this.determineReadPairType( this.block );
        this.readPairPopup = new ReadPairPopup( this.parentViewer, pairType, this.pairColors, this.block );
    }


    /**
     * Determines the type string of the main read pair
     * <p>
     * @param block the block containing the read pair
     * <p>
     * @return the type string
     */
    private String determineReadPairType( BlockPair block ) {
        String type = "Not a read pair object";
        if( block.getObjectWithId() instanceof ReadPairGroup ) {
            List<ReadPair> readPairs = ((ReadPairGroup) block.getObjectWithId()).getReadPairs();
            if( readPairs.size() > 0 ) {
                type = readPairs.get( 0 ).getReadPairType().getTypeString();
            }
            else {
                type = ReadPairType.UNPAIRED_PAIR.getTypeString();
            }
        }
        return type;
    }


    /**
     * @return true, if this component contains rectangles to paint and thus is
     *         paintable;
     *         false otherwise.
     */
    public boolean isPaintable() {
        return !this.rectList.isEmpty();
    }


}
