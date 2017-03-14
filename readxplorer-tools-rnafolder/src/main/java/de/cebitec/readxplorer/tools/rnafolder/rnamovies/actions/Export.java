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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.actions;


//import java.awt.Cursor;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.Movie;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.MoviePane;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.util.ExportAccessory;
import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
 * In this version for ReadXplorer all unused code is commented out, but the
 * original version can easily be restored.
 */

/**
 * Export (extending the MovieAction class is the ExportFilter class of
 * RNAMovies2. Currently for different formats are supported : SVG, JPG, PNG
 * (and (animated) GIF, not here in ReadXplorer). * @author Alexander Kaiser
 * <akaiser@techfak.uni-bielefeld.de>, Jan Krueger
 * <jkrueger@techfak.uni-bielefeld.de> (JavaDoc and (animated) Gif);
 */
public class Export extends MovieAction {

    private static final Logger LOG = LoggerFactory.getLogger( Export.class.getName() );

    private File lastDir = null;
    private final String header;

//    //global variables needed for coordinate transformations
//    private double xTrans = 0.0;
//    private double yTrans = 0.0;
//    private double zoomf = 0.0;

    public Export( MoviePane moviePane, String header ) {//    public Export(RNAMovies movies) {
        super( "Export...", "" );
        this.movies = moviePane; //this.movies = movies;
        this.header = this.prepareHeader( header );
    }


    @Override
    public void actionPerformed( ActionEvent e ) {
        int retval, idx;
//        int i, from, to, tmp, scale, fps;
        int zoom, imageW, imageH;
//        boolean fit,
        boolean trans;
//        ModalProgressMonitor pm;
//        Thread t;
        Movie movie;
//        Cursor oldCursor;
        File f;
        JFileChooser chooser;

        movie = this.movies;//        movie = movies.getMovie();

        if( movie.numFrames() < 1 ) {
            return;
        }

        if( lastDir == null ) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser( lastDir );
        }
        idx = 1; //since in ReadXplorer we only want the fst frame //idx = movies.isRunning() ? 1 : movies.getFrameIdx() + 1;
        final int factor = 5; //to achieve satisfactory image resolution by default
        chooser.setSelectedFile( new File( this.header ) );
        chooser.setAccessory( new ExportAccessory( chooser, idx, idx, movie.numFrames(), movie.getMaxWidth() * factor, movie.getMaxHeight() * factor ) );
        chooser.setFileFilter( new PNGFilter() );
        chooser.setFileFilter( new JPGFilter() );
//        chooser.setFileFilter(new SVGFilter());
//        chooser.setFileFilter(new GIFFilter());

        retval = chooser.showDialog( this.movies, "Export" ); //movies
        if( retval != JFileChooser.APPROVE_OPTION ) {
            return;
        }


//        from = ((ExportAccessory)chooser.getAccessory()).getFromFrame();
//        to = ((ExportAccessory)chooser.getAccessory()).getToFrame();
        zoom = ((ExportAccessory) chooser.getAccessory()).getZoom();
        imageW = ((ExportAccessory) chooser.getAccessory()).getW();
        imageH = ((ExportAccessory) chooser.getAccessory()).getH();
//        fps = ((ExportAccessory) chooser.getAccessory()).getFPS();
//        if(to < from) {
//            tmp = to;
//            to = from;
//            from = tmp;
//        }

        try {
            f = chooser.getSelectedFile();
            lastDir = chooser.getCurrentDirectory();
//            if(to == from) {
            LOG.info( "write a single image" );

            trans = ((ExportAccessory) chooser.getAccessory()).getTransparent();
            if( chooser.getFileFilter() instanceof JPGFilter ) {
                writeJPG( movie, f, 1, zoom, false, imageW, imageH );//from, zoom, false, imageW, imageH);
            } else if( chooser.getFileFilter() instanceof PNGFilter ) {
                writePNG( movie, f, 1, zoom, false, trans, imageW, imageH );//from, zoom, false, trans,imageW,imageH);
//                } else if(chooser.getFileFilter() instanceof SVGFilter) {
//                    writeSVG(movie, f, 1, false);//from, false);
//                } else if (chooser.getFileFilter() instanceof GIFFilter) {
//                    writeGIF(movies, f,from,zoom,trans, imageW, imageH);
            }
//            } else {
//                log.info("write a sequence of images");
//                pm = new ModalProgressMonitor("Creating Images...", "", from, to);
//                t = new Thread(new ExportLoop(from, to, chooser, pm));
//                t.start();
//                oldCursor = movies.getCursor();
//                movies.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//                pm.show(movies);
//                movies.setCursor(oldCursor);
//            }
        } catch( java.io.FileNotFoundException ex ) {
            JOptionPane.showMessageDialog( movies,
                                           "Error writing file:\n" +
                                           ex.getMessage(),
                                           ex.getClass().getName(),
                                           JOptionPane.ERROR_MESSAGE );
        } catch( IOException ex ) {
            JOptionPane.showMessageDialog( movies,
                                           "Error writing file:\n" +
                                           ex.getMessage(),
                                           ex.getClass().getName(),
                                           JOptionPane.ERROR_MESSAGE );
        } catch( Exception ex ) {
            JOptionPane.showMessageDialog( movies,
                                           "Error writing file:\n" +
                                           ex.getMessage(),
                                           ex.getClass().getName(),
                                           JOptionPane.ERROR_MESSAGE );
        }
    }

//    private class ExportLoop implements Runnable {
//
//        private int from, to;
//        private JFileChooser chooser;
//        private ModalProgressMonitor pm;
//
//        public ExportLoop(int from, int to, JFileChooser chooser,
//                ModalProgressMonitor pm) {
//            this.chooser = chooser;
//            this.pm = pm;
//            this.from = from;
//            this.to = to;
//
//        }
//
//        @Override
//        public void run() {
//            int i,e, scale, imageH, imageW, zoom, steps, fps;
//            boolean fit, trans;
//            File f = null;
//            Movie movies;
//            BufferedImage image;
//
//            try {
//                movies = movies.getMovie();
//                from = ((ExportAccessory)chooser.getAccessory()).getFromFrame();
//                to = ((ExportAccessory)chooser.getAccessory()).getToFrame();
//                trans = ((ExportAccessory)chooser.getAccessory()).getTransparent();
//                imageH = ((ExportAccessory)chooser.getAccessory()).getH();
//                imageW = ((ExportAccessory)chooser.getAccessory()).getW();
//                zoom = ((ExportAccessory)chooser.getAccessory()).getZoom();
//                steps = ((ExportAccessory)chooser.getAccessory()).getInterpolationSteps();
//                fps = ((ExportAccessory)chooser.getAccessory()).getFPS();
//                // write an animated gif using AnimatedGIF encoder
//                if (chooser.getFileFilter() instanceof GIFFilter) {
//                    AnimatedGifEncoder ani  = new AnimatedGifEncoder();
//                    f = chooser.getSelectedFile();
//                    if (!(f.toString().endsWith(".gif"))) {
//                        f = new File(f.toString().concat(".gif"));
//                    }
//                    ani.start(f.toString());
//                    ani.setFrameRate(fps);
//                    ani.setRepeat(0);
//                    ani.setSize(imageW,imageH);
//
//                    /* create a BufferedImage as basis */
//                    if(trans) {
//                        image = new BufferedImage(imageW, imageH,
//                                BufferedImage.TYPE_4BYTE_ABGR);
//                    } else {
//                        image = new BufferedImage(imageW, imageH,
//                                BufferedImage.TYPE_3BYTE_BGR);
//                    }
//                    /* get Graphics2D context */
//                    Graphics2D gc = getGraphics2D(movies, image,imageW, imageH,trans, zoom);
//                    /* draw all (interpolated) structures */
//                    for (i = from; i < to && !pm.isCanceled(); ++i) {
//                        for (e = 0; e < steps; ++e ){
//                            pm.setNote("frame "+i+" ("+e+"/"+steps+") of "+to);
//                            //ani.addFrame(movies.getFrame(i-1,steps,e,false,trans,zoom,imageW,imageH));
//                            // transform the old coordinates into the new system
//                            gc.clearRect(-(int)(xTrans/zoomf), -(int)(yTrans/zoomf),(int)(imageW/zoomf),  (int)(imageH/zoomf));
//                            movies.drawFrame(gc,i-1,steps,e);
//                            ani.addFrame(image);
//
//                        }
//                        pm.setProgress(i);
//                    }
//                    //ani.addFrame(movies.getFrame(to-1,false,trans,zoom,imageW,imageH));
//                    // transform the old coordinates into the new system
//                    gc.clearRect(-(int)(xTrans/zoomf), -(int)(yTrans/zoomf),(int)(imageW/zoomf),  (int)(imageH/zoomf));
//                    movies.drawFrame(gc,i-1);
//                    ani.addFrame(image);
//                    pm.setNote("frame "+to+" of "+to);
//                    pm.setProgress(to);
//                    ani.finish();
//                    gc.dispose();
//                } else {
//                    for(i = from; i <= to && !pm.isCanceled(); ++i) {
//                        f = chooser.getSelectedFile();
//                        if(chooser.getFileFilter() instanceof JPGFilter) {
//                            f = writeJPG(movies, f, i, zoom, true, imageW, imageH);
//                        } else if(chooser.getFileFilter() instanceof PNGFilter) {
//                            f = writePNG(movies, f, i, zoom, true, trans,imageW,imageH);
//                        } else if(chooser.getFileFilter() instanceof SVGFilter) {
//                            f = writeSVG(movies, f, i, true);
//                        }
//
//                        pm.setNote(f.getName());
//                        pm.setProgress(i);
//                    }
//                }
//            } catch (java.io.FileNotFoundException ex) {
//                pm.close();
//                JOptionPane.showMessageDialog(movies,
//                        "Error writing file:\n"
//                        + ex.getMessage(),
//                        ex.getClass().getName(),
//                        JOptionPane.ERROR_MESSAGE);
//            } catch (IOException ex) {
//                pm.close();
//                JOptionPane.showMessageDialog(movies,
//                        "Error writing file:\n"
//                        + ex.getMessage(),
//                        ex.getClass().getName(),
//                        JOptionPane.ERROR_MESSAGE);
//            } catch(Exception ex) {
//                pm.close();
//                JOptionPane.showMessageDialog(movies,
//                        "Error writing file:\n"
//                        + ex.getMessage(),
//                        ex.getClass().getName(),
//                        JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    public Graphics2D getGraphics2D(Movie m, Image image, int x, int y, boolean transparent, int zoom) {
//        Graphics2D gc = (Graphics2D)image.getGraphics();
//
//        /* set rendering hints */
//        gc.setRenderingHints(m.getRenderingHints());
//
//        /* set background color */
//        if(transparent){
//            gc.setBackground(new Color(255, 255, 255, 255));
//        }else {
//            gc.setBackground(m.getBackground());
//        }
//
//        /* clear image */
//        gc.clearRect(0, 0, x, y);
//
//        zoomf = (double)x/(double)m.getMaxWidth()* zoom * 0.01;
//
//        /* center structure relative to image */
//        xTrans = (x-m.getMaxWidth()*zoomf)/2;
//        yTrans = (y-m.getMaxHeight()*zoomf)/2;
//        gc.translate(xTrans, yTrans);
//
//        /* set zoom */
//        gc.scale(zoomf, zoomf);
//
//        return gc;
//    }

//    private static String addExt(String filename, String extension){
//        return addExt(filename,extension,extension,false,0);
//    }
//
//    private static String addExt(String filename,
//            String extension,
//            String extexp) {
//        return addExt(filename, extension, extexp, false, 0);
//    }
    private static String addExt( String filename,
                                  String extension,
                                  String extexp,
                                  boolean addNumber,
                                  int number ) {
        Pattern p;
        Matcher m;

        if( extension.charAt( 0 ) == '.' ) {
            extexp = extexp == null ? extension.substring( 1, extension.length() ) : extexp;
        } else {
            extexp = extexp == null ? extension : extexp;
            extension = "." + extension;
        }

        p = Pattern.compile( "\\." + extexp + "$", Pattern.CASE_INSENSITIVE );
        m = p.matcher( filename );

        if( addNumber ) {
            if( m.find() ) {
                return filename.substring( 0, m.start() ).concat( "-" + String.valueOf( number ) ).concat( extension );
            } else {
                return filename.concat( "-" + String.valueOf( number ) ).concat( extension );
            }
        } else if( !m.find() ) {
            return filename.concat( extension );
        } else {
            return filename;
        }
    }

//    public static File writeSVG(Movie movie, File f,
//            int idx,
//            boolean addNumber)
//            throws UnsupportedEncodingException,
//            FileNotFoundException,
//            IOException,
//            SVGGraphics2DIOException {
//        DOMImplementation domImpl;
//        Document document;
//        SVGGraphics2D sg2;
//        FileOutputStream fos;
//        Writer out;
//
//        f = new File(f.getParent(),addExt(f.getName(),"svg",null,addNumber,idx));
//
//        domImpl = GenericDOMImplementation.getDOMImplementation();
//        document = domImpl.createDocument(null, "svg", null);
//        sg2 = new SVGGraphics2D(document);
//
//        movie.drawFrame(sg2, idx - 1);
//        fos = new FileOutputStream(f);
//        out = new OutputStreamWriter(fos, "UTF-8");
//        sg2.stream(out, false);
//        out.close();
//        fos.close();
//        return f;
//    }

    public static File writePNG( Movie movie, File f,
                                 int idx,
                                 int zoom,
                                 boolean addNumber,
                                 boolean trans,
                                 int x,
                                 int y )
            throws IOException {
        f = new File( f.getParent(), addExt( f.getName(), "png", null, addNumber, idx ) );
        writeImage( movie.getFrame( idx - 1, false, trans, zoom, x, y ), "png", f );
        return f;
    }


    public static File writeJPG( Movie movie, File f,
                                 int idx,
                                 int zoom,
                                 boolean addNumber,
                                 int x,
                                 int y )
            throws IOException {
        f = new File( f.getParent(), addExt( f.getName(), "jpg", "jp\\(e\\?\\)g", addNumber, idx ) );
        writeImage( movie.getFrame( idx - 1, false, false, zoom, x, y ), "jpg", f );
        return f;
    }

//    public static File writeGIF(Movie movies, File f,int idx, int zoom, boolean trans,int x, int y) throws IOException {
//        f = new File(f.getParent(),addExt(f.getName(),"gif"));
//        AnimatedGifEncoder ani = new AnimatedGifEncoder();
//        ani.start(new FileOutputStream(f));
//        ani.addFrame(movies.getFrame(idx-1, false, trans, zoom,x,y));
//        ani.finish();
//        return f;
//    }

    private static void writeImage( RenderedImage image,
                                    String format,
                                    File outfile )
            throws IOException {


        if( !outfile.getParentFile().canWrite() ) {

            throw new IOException( outfile.getPath() + " (Permission denied)" );
        }

        Iterator iter = ImageIO.getImageWritersByFormatName( format );
        ImageWriter writer = null;
        if( iter.hasNext() ) {
            writer = (ImageWriter) iter.next();
        }

        // Prepare output file
        ImageOutputStream ios = ImageIO.createImageOutputStream( outfile );
        writer.setOutput( ios );

        // Write the image
        writer.write( image );

        // Cleanup
        ios.flush();
        writer.dispose();
        ios.close();
    }


    /**
     * Guarantees that the header is usable as a filename and does not produce
     * an error.
     * <p>
     * @param header header string to be used as filename
     * <p>
     * @return header usable as filename
     */
    private String prepareHeader( String header ) {
        if( header.contains( ">>" ) ) {
            header = header.replace( ">>", "fwd" );
        } else if( header.contains( "<<" ) ) {
            header = header.replace( "<<", "rev" );
        }
        return header;
    }

//    private static class SVGFilter extends FileFilter {
//        @Override
//        public boolean accept(File f) {
//            return f.getName().endsWith(".svg") || f.isDirectory();
//        }
//        @Override
//        public String getDescription(){
//            return "Scalable Vector Graphics (*.svg)";
//        }
//    }

    private static class PNGFilter extends FileFilter {

        @Override
        public boolean accept( File f ) {
            return f.getName().endsWith( ".png" ) || f.isDirectory();
        }


        @Override
        public String getDescription() {
            return "Portable Network Graphics (*.png)";
        }


    }


    private static class JPGFilter extends FileFilter {

        @Override
        public boolean accept( File f ) {
            return f.getName().endsWith( ".jpg" ) ||
                   f.getName().endsWith( ".jpeg" ) ||
                   f.isDirectory();
        }


        @Override
        public String getDescription() {
            return "Joint Photographic Experts Group (*.jpg, *.jpeg)";
        }


    }

//    private static class GIFFilter extends FileFilter {
//        public boolean accept(File f) {
//            return f.getName().endsWith(".gif") || f.isDirectory();
//        }
//        public String getDescription() {
//            return "(animated) Graphics Interchange Format (*.gif)";
//        }
//    }
}
