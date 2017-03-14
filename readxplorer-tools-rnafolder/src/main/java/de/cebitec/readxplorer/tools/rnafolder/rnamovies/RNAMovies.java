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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies;


import de.cebitec.readxplorer.tools.rnafolder.naview.PairTable;
import de.cebitec.readxplorer.tools.rnafolder.naview.Structure;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.configuration.Configuration;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.configuration.FieldAdapter;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.util.ActionContainer;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.util.ActionXMLHandler;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.util.LineScanner;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.util.ShapeOps;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

//import de.unibi.techfak.bibiserv.biodom.RNAStructML;
//import de.unibi.techfak.bibiserv.biodom.exception.BioDOMException;
//import de.unibi.techfak.bibiserv.rnamovies.thirdparty.AnimatedGifEncoder;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import javax.xml.parsers.DocumentBuilderFactory;
//import org.w3c.dom.Document;

/**
 * RNA Movies
 * <p>
 * @author Alexander Kaiser <akaiser@TechFak.Uni-Bielefeld.DE>
 *
 * RNAStructML,Commandline interface support added by Jan krueger
 * <jkrueger@techfak.uni-bielefeld.de>
 *
 * Functionality limited for ReadXplorer (commented out main method, export and
 * RNAStructML parts) by Rolf Hilker. Also fixed some warnings.
 * <p>
 */
public class RNAMovies extends JPanel implements ActionContainer {

    private static final Logger LOG = LoggerFactory.getLogger( RNAMovies.class.getName() );

    public static final String TITLE = "RNAMovies";
    public static final String VERSION = "2.04";
    public static final String USAGE = TITLE + VERSION + "\n" +
                                       "usage java " + TITLE + VERSION + ".jar [<arguments>] :\n\n" +
                                       "ATTENTION : cmdlineoptions in early beta state!\n\n" +
                                       "-nogui                         :: run RNAMovies without starting the GUI\n" +
                                       "-input <String>                :: set the input filename\n" +
                                       "-output <String>               :: set the output filename\n" +
                                       "[-xml]                         :: determines that the input file is in RNAStructML format\n" +
                                       "-[structure <int>|steps <int>] :: creates ONLY the given structure (single frame)\n" +
                                       "[-size <int>]                  :: set the size of the generated frame (in pixel)\n" +
                                       "[-zoom <int>]                  :: zoom factor inside" +
                                       "-(gif|png|svg|jpg)             :: set the image format\n" +
                                       "[-h[elp]]                      :: print out a usage message\n";


    private static final int SCALE = 15;

    private Configuration config;

    private JMenuBar mb;
    private MoviePane mp;
    private JToolBar tb;


    protected RNAMovies( InputStream configStream, InputStream actionStream ) {
        super( new BorderLayout() );

        FieldAdapter fa;
        XMLReader parser;

        // menu
        mb = new JMenuBar();

        // toolbar
        tb = new JToolBar();
        tb.setFloatable( false );
        tb.setLayout( new FlowLayout() );

        // load configuration
        try {
            config = new Configuration( configStream );
        } catch( IOException e ) {
            LOG.error( e.getMessage() );
            System.exit( 1 );
        } catch( SAXException e ) {
            LOG.error( "Error while parsing config: ".concat( e.getMessage() ) );
            System.exit( 1 );
        }

        // load actions
        try {
            parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler( new ActionXMLHandler( this,
                                                            new Class<?>[]{ this.getClass() },
                                                            new Object[]{ this } ) );
            parser.parse( new InputSource( actionStream ) );
        } catch( IOException e ) {
            LOG.error( e.getMessage() );
            System.exit( 1 );
        } catch( SAXException e ) {
            LOG.error( "Error while parsing action file: ".concat( e.getMessage() ) );
            System.exit( 1 );
        }

        add( mb, BorderLayout.NORTH );

        // movie pane
        mp = new MoviePane( 640, 460 );
        fa = new FieldAdapter( mp.getConfigurable() );
        config.addConfigListener( fa );
        config.getCategory( "animation" ).removeConfigListener( fa );
        config.addConfigListener( mp );
        config.initAll();
        add( mp, BorderLayout.CENTER );

        add( tb, BorderLayout.SOUTH );
    }


    /**
     * Public default constructor, standard menu and configurations are loaded.
     */
    public RNAMovies() {
        this( RNAMovies.class.getResourceAsStream( "config.xml" ),
              RNAMovies.class.getResourceAsStream( "actions.xml" ) );
    }


    /**
     * Set the movie data from a String.
     * <p>
     * @param data A String containing a valid script.
     */
    public void setData( String data ) {
        parseScript( mp, new StringTokenizer( data, "\n" ), false );
    }

//    /**
//     * Set the movie data from a RNAStructML object
//     *
//     * @param rml - A String containing a RNAStructML object
//     *
//     * JK
//     */
//    public void setData(RNAStructML rml){
//        parseRNAStructML(mp,rml,false);
//    }

    /**
     * Set the movie data from a String.
     * <p>
     * @param data   A String containing a valid script.
     * @param center Center structures to the maximal area of the whole script.
     */
    public void setData( String data, boolean center ) {
        parseScript( mp, new StringTokenizer( data, "\n" ), center );
    }

//    /**
//     * Set the movie data from a RNAStructML object
//     *
//     * @param rml - A String containing a RNAStructML object
//     * @param center - Center all structures to the maximal area of the whole script.
//     *
//     * JK
//     */
//    public void setData(RNAStructML rml, boolean center) {
//        parseRNAStructML(mp,rml,center);
//    }

    /**
     * Set the movie data from an InputStream.
     * <p>
     * @param in An InputStream (e.g. FileInputStream) containing a valid
     *           script.
     */
    public void setData( InputStream in ) {
        parseScript( mp, new LineScanner( in ), false );
    }


    /**
     * Set the movie data from an InputStream.
     * <p>
     * @param in     An InputStream (i.e. FileInputStream) containing a valid
     *               script.
     * @param center Center structures to the maximal area of the whole script.
     */
    public void setData( InputStream in, boolean center ) {
        parseScript( mp, new LineScanner( in ), center );
    }


    private static void parseScript( MoviePane mp, Enumeration enumer, boolean center ) {
        parseScript( mp, enumer, center, true );
    }


    private static void parseScript( MoviePane mp, Enumeration enumer, boolean center, boolean gui ) {
        int i, w, h, length;
        int old_length = -1;
        int title_end = -1;
        boolean dcse;
        String name, sequence, structure, helices;
        List<Point2D[]> frames;
        List<Dimension> sizes;
        Dimension maxSize;
        List<PairTable> pairs;
        Structure struc = null;

        if( !enumer.hasMoreElements() ) {
            throw new IllegalArgumentException( "No Movie Data found!" );
        } else {
            name = ((String) enumer.nextElement()).trim();
        }

        if( name.isEmpty() ) {
            throw new IllegalArgumentException( "No Movie Data found!" );
        }

        if( name.charAt( 0 ) == '>' ) {
            dcse = false;
        } else if( name.charAt( 0 ) == '<' ) {
            dcse = true;
        } else {
            throw new IllegalArgumentException( "Data Format Error: Missing '>' or '<' character!" );
        }

        if( !enumer.hasMoreElements() ) {
            throw new IllegalArgumentException( "No Movie Data found!" );
        } else {
            sequence = ((String) enumer.nextElement()).trim();
        }

        length = sequence.length();

        w = h = 0;
        pairs = new ArrayList<>( 350 );
        frames = new ArrayList<>( 350 );
        sizes = new ArrayList<>( 350 );
        while( enumer.hasMoreElements() ) {

            if( struc != null ) {
                old_length = struc.length();
            }

            if( dcse ) {
                structure = ((String) enumer.nextElement());
                if( !enumer.hasMoreElements() ) {
                    throw new IllegalArgumentException( "Error in DCSE structure: missing helix numbering." );
                }
                helices = ((String) enumer.nextElement());
                struc = new Structure( sequence, structure, helices );
            } else {
                structure = ((String) enumer.nextElement()).trim();
                struc = new Structure( sequence, structure );
            }

            if( old_length != -1 && old_length > struc.length() ) {
                throw new IllegalArgumentException( "Length of structures in descending order not allowed!" );
            }

            pairs.add( struc.getPairTable() );
            frames.add( struc.getNormalizedCoordinates( SCALE, SCALE, 0, 0 ) );
            sizes.add( new Dimension( struc.getWidth( SCALE ), struc.getHeight( SCALE ) ) );
            if( struc.getWidth( SCALE ) > w ) {
                w = struc.getWidth( SCALE );
            }
            if( struc.getHeight( SCALE ) > h ) {
                h = struc.getHeight( SCALE );
            }
        }

        if( frames.isEmpty() ) {
            throw new IllegalArgumentException( "No Movie Data found!" );
        }

        if( center ) {
            maxSize = new Dimension( w, h );
            for( i = 0; i < frames.size(); i++ ) {
                ShapeOps.center( frames.get( i ), sizes.get( i ), maxSize );
            }
        }

        title_end = name.indexOf( ' ' );
        mp.setMovie( frames, pairs, name.substring( 1, title_end == -1 ? name.length() : title_end ), sequence, w, h, gui );
        LOG.info( "{0} Structures loaded.", frames.size() );
    }

//    /**
//     * Get all necessary data from RNAStructML and pass it to the
//     * moviepane (for internal use only)
//     *
//     * JK
//     */
//    private static void parseRNAStructML(MoviePane mp, RNAStructML rml, boolean center){
//        parseRNAStructML(mp,rml,center,true);
//    }

//    /**
//     * Get all necessary data from RNAStructML and pass it to the
//     * moviepane (for internal use only)
//     *
//     * JK
//     */
//    private static void parseRNAStructML(MoviePane mp, RNAStructML rml, boolean center,boolean gui){
//        // local used vars
//        int w = 0;
//        int h = 0;
//        List<Point2D[]> frames = new ArrayList<Point2D[]>(350);
//        List<Dimension> sizes = new ArrayList<Dimension>(350);
//        List<PairTable> pairs = new ArrayList<PairTable>(350);
//        String sequence;
//        String name = "";
//
//        // until now only the first structure of a RNAStructML is supported
//        String structureid = rml.getRnastructureIds().get(0);
//        // get Sequence information
//        try {
//            Hashtable<String,Object> sequenceInfo = rml.getSequence(structureid);
//            sequence = (String)(sequenceInfo.get("sequence"));
//            name = (String)(sequenceInfo.get("seqID"));
//        } catch (BioDOMException e){
//            throw new IllegalArgumentException("BioDOM Exception during call of getSequence!");
//        }
//        // get all structures
//        try {
//            List<Hashtable<String,Object>> structuresInfoList = rml.getStructures(structureid);
//            // check if list contains at least one element
//            if (structuresInfoList.size() == 0) {
//                throw new IllegalArgumentException("RNAStructML contains no structure information!");
//            }
//            // iterate over all elements
//            for (int i = 0; i < structuresInfoList.size(); ++i){
//                if (structuresInfoList.get(i).containsKey("structure")){
//                    String dotbracket = (String)structuresInfoList.get(i).get("structure");
//                    if (sequence.length() != dotbracket.length()){
//                        throw new IllegalArgumentException("Length of structure differs from length of sequence!");
//                    }
//                    // the following lines are more or less a copy from parseScript(...)
//                    Structure struc = new Structure(sequence, dotbracket);
//                    pairs.add(struc.getPairTable());
//                    frames.add(struc.getNormalizedCoordinates(SCALE,SCALE,0,0));
//                    sizes.add(new Dimension(struc.getWidth(SCALE),struc.getHeight(SCALE)));
//                    if (struc.getWidth(SCALE) > w){
//                        w = struc.getWidth(SCALE);
//                    }
//                    if (struc.getHeight(SCALE) > h){
//                        h = struc.getHeight(SCALE);
//                    }
//                } else {
//                    throw new IllegalArgumentException("RNAStructML contains no structure information!");
//                }
//            }
//
//
//        } catch (BioDOMException e){
//            throw new IllegalArgumentException("BioDOM Exception during call of getStructures!");
//        }
//
//        if (center){
//            Dimension maxSize = new Dimension(w,h);
//            for (int i = 0; i < frames.size(); ++i){
//                ShapeOps.center(frames.get(i),sizes.get(i),maxSize);
//            }
//        }
//
//        mp.setMovie(frames,pairs,name,sequence,w,h,gui);
//    }
    @Override
    public JToolBar getToolBar() {
        return tb;
    }


    @Override
    public JMenuBar getMenuBar() {
        return mb;
    }


    /**
     * Accessor method for the Movie
     * <p>
     * @return an instance of the Movie
     */
    public Movie getMovie() {
        return mp;
    }


    /**
     * Get an instance of the Configuration
     * <p>
     * @return the current configuration
     */
    public Configuration getConfiguration() {
        return config;
    }

//    /**
//     * This method loads the Main class and puts the main panel on a JFrame.
//     * It is to be run as a thread.
//     */
//    private static void createAndShowGUI() {
//        JFrame frame;
//
//        frame = new JFrame(TITLE);
//        frame.setContentPane(new RNAMovies());
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    /**
//     * The main method of this launcher. Sets the preferred look-and-feel and
//     * starts the above method inside a thread.
//     */
//    public static void main(String args[]) {
//
//        /* parse arguments */
//        Hashtable<String,Object> params = parseParameter(args);
//
//
//
//        if (params.containsKey("nogui")) {
//            log.info("Launching RNA Movies in commandline mode.");
//            MoviePane movie = new MoviePane();
//            /*attach data from file to movie*/
//            if (params.containsKey("xml")) {
//                try {
//                    /* as RNAStructML */
//                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//                    dbf.setNamespaceAware(true);
//                    Document  dom = dbf.newDocumentBuilder().parse((String)params.get("input"));
//                    RNAStructML rml = new RNAStructML(dom);
//                    parseRNAStructML(movie,rml,false,false);
//                } catch (ParserConfigurationException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                } catch (SAXException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                } catch(IOException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                } catch (BioDOMException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                }
//            } else {
//                /* as MovieScript or DSCE */
//                try {
//                    InputStream ini = new FileInputStream((String)params.get("input"));
//                    parseScript(movie,new LineScanner(ini),false,false);
//                } catch (FileNotFoundException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                }
//            }
//            if(movie.numFrames() < 1) {
//                return;
//            }
//            /* outputfile */
//            File out = new File((String)params.get("output"));
//            /* size */
//            int x = (Integer)params.get("size");
//            float mx = ((float)movie.getMaxWidth()/(float)movie.getMaxHeight());
//            int y = Math.round((float)x/mx);
//
//
//            /* transparent background */
//            boolean trans =false;
//            if (params.containsKey("trans")) {
//                trans = true;
//            }
//            /* zoom factor */
//            int zoom = 85;
//            if (params.containsKey("zoom")) {
//                zoom = (Integer)params.get("zoom");
//            }
//            /* export only one structure */
//            if (params.containsKey("structure")) {
//                try {
//                    int structure = (Integer)(params.get("structure"));
//                    if (params.containsKey("gif")) {
//                        Export.writeGIF(movie,out,structure-1,zoom,trans,x,y);
//                    } else if (params.containsKey("png")){
//                        Export.writePNG(movie,out,structure-1,zoom,false,trans,x,y);
//                    } else if (params.containsKey("jpg")){
//                        Export.writeJPG(movie,out,structure-1,zoom,false,x,y);
//                    } else if (params.containsKey("svg")){
//                        Export.writeSVG(movie,out,structure-1,trans);
//                    }
//                }catch (IOException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                }
//            } else {
//                /*export movie as XX*/
//                try {
//                    int steps = 10;
//                    if (params.containsKey("steps")) {
//                        steps = (Integer)params.get("steps");
//                    }
//                    int fps = 10;
//                    if (params.containsKey("fps")) {
//                        fps = (Integer)params.get("fps");
//                    }
//                    if (params.containsKey("gif")){
//                        AnimatedGifEncoder ani  = new AnimatedGifEncoder();
//                        ani.start((String)params.get("output"));
//                        ani.setFrameRate(fps);
//                        ani.setRepeat(0);
//                        ani.setSize(x,y);
//                        /* create a BufferedImage as basis */
//                        BufferedImage image = null;
//                        for (int i = 0; i < movie.numFrames(); ++i ){
//                            for (int e = 0; e < steps; ++e ){
//                                if(trans) {
//                                    image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
//                                } else {
//                                    image = new BufferedImage(x,y,BufferedImage.TYPE_3BYTE_BGR);
//                                }
//                                Graphics2D gc = (Graphics2D)image.getGraphics();
//
//                                /* set rendering hints */
//                                gc.setRenderingHints(movie.getRenderingHints());
//
//                                /* set background color */
//                                if(trans){
//                                    gc.setBackground(new Color(255, 255, 255, 255));
//                                }else {
//                                    gc.setBackground(movie.getBackground());
//                                }
//
//                                /* clear image */
//                                gc.clearRect(0, 0, x, y);
//                                double zoomf = (double)x/(double)movie.getMaxWidth()* zoom * 0.01;
//                                /* center structure relative to image */
//                                double xTrans = (x-movie.getMaxWidth()*zoomf)/2;
//                                double yTrans = (y-movie.getMaxHeight()*zoomf)/2;
//                                gc.translate(xTrans, yTrans);
//                                /* draw structure on graphics context */
//                                if (i == movie.numFrames()-1) {
//                                    movie.drawFrame(gc,i);
//                                    e = steps;
//                                } else {
//
//                                    movie.drawFrame(gc,i,steps,e);
//                                }
//                                ani.addFrame(image);
//                            }
//                        }
//                        ani.finish();
//                    } else {
//                        for (int i = 0; i < movie.numFrames(); ++i ){
//                            if (params.containsKey("png")){
//                                Export.writePNG(movie,out,i,zoom,false,trans,x,y);
//                            } else if (params.containsKey("jpg")){
//                                Export.writeJPG(movie,out,i,zoom,false,x,y);
//                            } else if (params.containsKey("svg")){
//                                Export.writeSVG(movie,out,i,trans);
//                            }
//                        }
//                    }
//                } catch (IOException e){
//                    log.error(e.getMessage());
//                    System.exit(1);
//                }
//            }
//        } else {
//            log.info("Launching RNA Movies.");
//            try {
//                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//            } catch(Exception e1) {
//                log.error("Could not load javax.swing.plaf.metal.MetalLookAndFeel, trying cross platform Look and Feel.");
//                try {
//                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//                } catch(Exception e2) {
//                    log.error("Critical: Could not load cross platform Look and Feel.");
//                    System.exit(1);
//                }
//            }
//
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {createAndShowGUI();}
//            });
//        }
//    }
//
//    /** static method parse supported Inputparameter
//     *  into a Hashtable
//     *  -nogui             :: run RNAMovies without starting the GUI
//     *  -input <String>    :: set the input filename
//     *  -output <String>   :: set the output filename
//     *  [-xml]             :: determines that the input file is in RNAStructML format
//     *  -[structure <int>|steps <int> ]:: creates ONLY the given structure (single frame)
//     *  [-zoom <int>]      :: zoom factor inside
//     *  [-size <int>]      :: set the size of the generated frame (in pixel)
//     *  -(gif|png|svg|jpg) :: set the image format
//     *  [-trans]           :: set transparent background (not for jpg)
//     *  [-zoom]            :: set zoom factor of structure within image
//     *  [-help] [-h]       :: print out a usage message
//     */
//    private static Hashtable<String,Object> parseParameter(String args[]) {
//        Hashtable<String,Object> prop = new Hashtable<String,Object>();
//        prop.put("input", new String());
//        prop.put("output", new String());
//        prop.put("xml", new Boolean(true));
//        prop.put("size", new Integer(0));
//        prop.put("structure", new Integer(0));
//        prop.put("steps", new Integer(10));
//        prop.put("zoom", new Integer(0));
//        prop.put("gif", new Boolean(true));
//        prop.put("png", new Boolean(true));
//        prop.put("svg", new Boolean(true));
//        prop.put("jpg", new Boolean(true));
//        prop.put("help", new Boolean(true));
//        prop.put("h", new Boolean(true));
//        prop.put("nogui", new Boolean(true));
//        prop.put("trans",new Boolean(true));
//        Hashtable ret = new Hashtable();
//        String key = "";
//
//        for (int i = 0; i < args.length; ++i) {
//            String current = args[i];
//
//            // found key
//            if (current.startsWith("-")) {
//                // remove --
//                key = current.replaceAll("-", "");
//
//                // check - maybe boolean
//                if ((key != null) && (prop.get(key) != null)) {
//                    Class c = (prop.get(key)).getClass();
//                    if ((c.getName()).equals("java.lang.Boolean")) {
//                        ret.put(key, new Boolean(true));
//                    }
//                    log.info("Found "+key+" as Boolean");
//                }
//
//            } else { // found value
//                // check if current key exists in prop
//                if (prop.get(key) != null) {
//
//                    // get Class of value
//                    Class c = (prop.get(key)).getClass();
//                    if ((c.getName()).equals("java.lang.Integer")) {
//                        ret.put(key, new Integer(Integer.parseInt(current)));
//                    } else if ((c.getName()).equals("java.lang.String")) {
//                        ret.put(key, current);
//                    } else if ((c.getName()).equals("java.lang.Double")) {
//                        ret.put(key, new Double(Double.parseDouble(current)));
//                    }
//                }
//                key = null;
//            }
//        }
//        /* print usage message to STDOUT and exit */
//        if (ret.containsKey("h") || ret.containsKey("help")){
//            System.out.println(USAGE);
//            System.exit(0);
//
//        }
//        /* check parameter dependencies */
//        if (ret.containsKey("nogui") &&
//                !ret.containsKey("input") && !ret.containsKey("output") &&
//                !((ret.containsKey("gif") || ret.containsKey("png") || ret.containsKey("jpg") || ret.containsKey("svg")))){
//            System.out.println(USAGE);
//            System.exit(0);
//        }
//
//        log.info(ret.toString());
//        return ret;
//    }

}
