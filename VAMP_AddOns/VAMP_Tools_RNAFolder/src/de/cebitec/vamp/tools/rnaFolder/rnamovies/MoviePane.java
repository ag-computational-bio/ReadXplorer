package de.cebitec.vamp.tools.rnaFolder.rnamovies;

import de.cebitec.vamp.tools.rnaFolder.rnamovies.util.ShapeOps;
import de.cebitec.vamp.tools.rnaFolder.naview.PairTable;
import de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration.ConfigChangedEvent;
import de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration.ConfigListener;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

import static java.awt.RenderingHints.*;

/**
 * This is where everything happens. First of all this class is a JComponent,
 * but it also has the capability of running itself as a thread and doing the
 * animation.
 *
 * @author Alexander Kaiser <akaiser@techfak.uni-bielefeld.de>,
 *         Jan Krueger <jkrueger@techfak.uni-bielefeld.de>
 *
 * Some warnings fixed and "zoomFit" method (setting of "ws" and "hs") adapted by Rolf Hilker.
 *
 */
public class MoviePane extends JComponent implements Runnable, Movie, ConfigListener {

    private Color background = Color.WHITE;
    private Color textColor = Color.BLACK;

    private Image backbuffer = null;

    /* read write lock for protecting the backbuffer */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock buffRLock = rwl.readLock();
    private final Lock buffWLock = rwl.writeLock();

    /*Mutex lock for protecting the current state */
    private final Lock stateLock = new ReentrantLock();

    private boolean start_stop = false;
    private boolean loop = false;
    private ShapeOps so = new ShapeOps();
    private Point2D center;
    private RenderingHints rh;
    private Font f = null;
    private Thread t = null;
    int zoom = 100;
    private long frame = 0;
    private boolean interpolate = true;
    private int interpolations;
    private int speed = 1000;
    private int size = 0;
    private int step = 0;
    private double xtrans = 0;
    private double ytrans = 0;
    private int i = 0;
    private int width = 0;
    private int height = 0;
    private String chars = null;
    private String title = "";
    private int title_width = 0;
    private List<Point2D[]> frames = null;
    private List<PairTable> pairs = null;

    private static final Logger log = Logger.getLogger("MoviePane");

    /**
     * Constructs a new MoviePane; this constructor should only
     * used if only the export facilities would be used, e.g.
     * for commandline interface.
     */
    protected MoviePane(){
        setRenderingHints();
    }

    /**
     * Constructs a new MoviePane.
     * @param d The initial dimension of this MoviePane.
     */
    protected MoviePane(int width, int height) {
        super();
        InputStream in;
        MouseInputAdapter mia;
        addMouseWheelListener(new ZoomListener());
        mia = new TranslationListener();
        addMouseListener(mia);
        addMouseMotionListener(mia);

        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        center = new Point2D.Double(0.5*width, 0.5*height);

        setRenderingHints();

        try {
            in = getClass().getResource("fonts/cour.pfa").openStream();
            f = Font.createFont(Font.TYPE1_FONT, in);
            f = f.deriveFont(Font.BOLD, 12f);
        } catch(IOException e) {
            log.severe(e.getMessage());
        } catch(FontFormatException e) {
            log.severe(e.getMessage());
        }
    }

    private void setRenderingHints(){
        rh = new RenderingHints(KEY_RENDERING, VALUE_RENDER_SPEED);
        rh.put(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        rh.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        rh.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
        rh.put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
        rh.put(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);
        rh.put(KEY_DITHERING, VALUE_DITHER_DISABLE);
    }

    protected Object getConfigurable() {
        return this.so;
    }

    /**
     * Set the movie data to this MoviePane.
     * @param frames A list of structures represented as an array of points.
     * @param pairs A list of pair tables.
     * @param title The movie title.
     * @param chars The underlying genomic sequence.
     * @param maxWidth The width of this movie (ie the width of the pixmap).
     * @param maxHeight The height of this movie (ie the height of the pixmap)
     */
    @Override
    public void setMovie(List<Point2D[]> frames,
            List<PairTable> pairs,
            String title,
            String chars,
            int maxWidth, int maxHeight) {
        setMovie(frames,pairs,title,chars,maxWidth,maxHeight,true);
    }


    /**
     * Set the movie data to this MoviePane.
     * @param frames A list of structures represented as an array of points.
     * @param pairs A list of pair tables.
     * @param title The movie title.
     * @param chars The underlying genomic sequence.
     * @param maxWidth The width of this movie (ie the width of the pixmap).
     * @param maxHeight The height of this movie (ie the height of the pixmap)
     * @param boolean - true if we work in GUI mode).
     */
    @Override
    public void setMovie(List<Point2D[]> frames,
            List<PairTable> pairs,
            String title,
            String chars,
            int maxWidth, int maxHeight, boolean gui) {

        if(frames.size() != pairs.size())
            throw new IllegalArgumentException("Illegal argument(s) passed "
                    + "to setMovie");
        if (gui) {
            stop();

            buffWLock.lock();
            stateLock.lock();
            try {
                setMovieData(frames,pairs,title,chars,maxWidth,maxHeight);
                title_width = getFontMetrics(getFont()).stringWidth(title);
            } finally {
                buffWLock.unlock();
                stateLock.unlock();
                drawIt();
                repaint();
            }
        } else {
            setMovieData(frames,pairs,title,chars,maxWidth,maxHeight);
        }
    }

    private void setMovieData(List<Point2D[]> frames,
            List<PairTable> pairs,
            String title,
            String chars,
            int maxWidth, int maxHeight){

        xtrans = 0.0;
        ytrans = 0.0;
        size = frames.size();
        width = maxWidth;
        height = maxHeight;
        center = new Point2D.Double(0.5*width, 0.5*height);

        i = step = 0;
        this.frames = frames;
        this.pairs = pairs;
        this.chars = chars;
        this.title = title;
    }



    @Override
    public void run() {
        long old, curr, tot;

        do {
            old = System.currentTimeMillis();

            stateLock.lock();
            try {
                if(++step >= interpolations) {
                    step = 0;
                    start_stop = (i != size - 2 || loop);
                    i = ++i >= size ? 0 : i;
                }
            } finally {
                stateLock.unlock();
            }

            drawIt();
            repaint();

            curr = System.currentTimeMillis();
            tot = curr - old;
            if(frame > tot) {
                try {
                    t.sleep(frame - tot);
                } catch(InterruptedException e) {
                    log.severe(e.getMessage());
                }
            }
        } while(this.start_stop);
    }

    /**
     * Paint the offscreen pixmap to the screen.
     */
    @Override
    public synchronized void paintComponent(Graphics g) {

        if(backbuffer == null
                || backbuffer.getWidth(this) != getWidth()
                || backbuffer.getHeight(this) != getHeight()) {
            backbuffer = createImage(getWidth(), getHeight());
            drawIt();
        }

        buffRLock.lock();
        try {
            g.drawImage(backbuffer, 0, 0, this);
        } finally {
            buffRLock.unlock();
        }
    }

    /** @see de.unibi.bibiserv.rnamovies.Movie.getZoom() */
    @Override
    public int getZoom() {
        return zoom;
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.setZoom() */
    @Override
    public void setZoom(int zoom) {
        if(zoom <= 0)
            return;

        buffWLock.lock();
        try {
            this.zoom = zoom;
        } finally {
            buffWLock.unlock();
            drawIt();
            repaint();
        }
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.zoomFit() */
    @Override
    public void zoomFit() {
        int ws, hs;
        float zoomf = 1.0f;

        xtrans = 0.0;
        ytrans = 0.0;
        so.rotAngle = 0.0;

        final int borderFactor = 8;
        ws = this.getWidth() - this.getWidth()/borderFactor;
        hs = this.getHeight()- this.getHeight()/borderFactor;
        if(width < height) {
            zoomf = ws/((float)width);
            if(hs > zoomf*height)
                zoom = (int)Math.round(zoomf*100);
            else
                zoom = (int)Math.round(100*hs/((float)height));
        } else {
            zoomf = hs/((float)height);
            if(ws > zoomf*width)
                zoom = (int)Math.round(zoomf*100);
            else
                zoom = (int)Math.round(100*ws/((float)width));
        }

        zoom = zoom > MAX_ZOOM ? MAX_ZOOM : (zoom < MIN_ZOOM ? MIN_ZOOM : zoom);

        drawIt();
        repaint();
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.numFrames */
    @Override
    public int numFrames() {
        return size;
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.getFrameIdx */
    @Override
    public int getFrameIdx() {
        return i;
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.getBackground */
    @Override
    public Color getBackground() {
        return background;
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.getRenderingHints */
    @Override
    public RenderingHints getRenderingHints() {
        return rh;
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.drawFrame */
    @Override
    public void drawFrame(Graphics2D gc, int idx) {
        drawFrame(gc, idx, 0, 0);
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.drawFrame */
    @Override
    public void drawFrame(Graphics2D gc, int idx, int numSteps, int step) {
        int j, n;

        if(size == 0 || idx < 0 || idx >= size)
            throw new IllegalArgumentException("No such frame: " + idx + ".");
        if(step > numSteps || step < 0 || numSteps < 0)
            throw new IllegalArgumentException("Invalid interpolation options: " + numSteps + ", " + step + ".");

        gc.setFont(f);
        if(numSteps == 0) {
            so.draw(gc, chars, frames.get(idx), center, pairs.get(idx));
        } else {
            j = idx == size - 1 ? size - 1 : idx + 1;
            n = step <= interpolations/2 ? idx : (i == size - 1 ? size - 1 : idx + 1);
           /* so.drawInterpolate(gc, chars,
                    frames.get(idx), frames.get(j),
                    center, pairs.get(n),
                    step, interpolations);*/
            so.drawInterpolate(gc, chars,
                    frames.get(idx), frames.get(j),
                    center, pairs.get(n),
                    step, numSteps);
        }
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.getFrame */
    @Override
    public BufferedImage getFrame(int idx,
            boolean fit,
            boolean transparent,
            int scale,
            int x,
            int y) {
        return getFrame(idx, 0, 0, fit, transparent, scale,x,y);
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.getFrame */
    @Override
    public BufferedImage getFrame(int idx,
            int numSteps,
            int step,
            boolean fit,
            boolean transparent,
            int zoom,
            int x,
            int y) {
        int j, n;
        double zoomf;
        Dimension d;
        Point2D origin;
        BufferedImage image;
        Graphics2D gc;

        if(size == 0 || idx < 0 || idx >= size)
            throw new IllegalArgumentException("No such frame: " + idx + ".");

        /*zoomf = 0.01 * scale; */
        /* JK : calculate zoomF */
        zoomf = (double)x/(double)getMaxWidth()* zoom * 0.01;


        if(fit) {
            d = new Dimension();
            origin = new Point2D.Double();
            so.getBounds(frames.get(idx), center, d, origin);
        } else {
            d = new Dimension(x,y);
            origin = new Point2D.Double(0.0, 0.0);
        }

        if(transparent) {
       /*image = new BufferedImage((int)(zoomf*d.getWidth()),
                                 (int)(zoomf*d.getHeight()),
                                 BufferedImage.TYPE_4BYTE_ABGR);*/
            image = new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
            gc = (Graphics2D)image.getGraphics();
            gc.setBackground(new Color(255, 255, 255, 255));
        } else {
     /* image = new BufferedImage((int)(zoomf*d.getWidth()),
                                (int)(zoomf*d.getHeight()),
                                BufferedImage.TYPE_3BYTE_BGR);*/
            image = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
            gc = (Graphics2D)image.getGraphics();
            gc.setBackground(background);
        }

        gc.setRenderingHints(rh);
        gc.setFont(f);
        /* gc.clearRect(0, 0, (int)(zoomf*d.getWidth()), (int)(zoomf*d.getHeight()));*/
        /* JK : clear complete graphics context */
        gc.clearRect(0,0,x,y);
        /*gc.translate(-zoomf*origin.getX(), -zoomf*origin.getY());*/
        /* JK : center structure */
        gc.translate((d.getWidth()-getMaxWidth()*zoomf)/2,(d.getHeight()-getMaxHeight()*zoomf)/2);
        gc.scale(zoomf, zoomf);
        if(numSteps == 0) {
            so.draw(gc, chars, frames.get(idx), center, pairs.get(idx));
        } else {
            j = idx == size - 1 ? size - 1 : idx+1;
            n = step <= interpolations/2 ? idx : (i == size - 1 ? size - 1 : idx + 1);
            so.drawInterpolate(gc, chars,
                    frames.get(idx), frames.get(j),
                    center, pairs.get(n),
                    step, interpolations);
        }
        gc.dispose();

        return image;
    }

    @Override
    public boolean isRunning() {
        return this.start_stop;
    }

    /**
     * Start to playback this movie.
     */
    @Override
    public void start() {
        if(this.start_stop || size <= 1 || (!loop && i == size - 1))
            return;

        try {
            if(t != null)
                t.join();

            this.start_stop = true;

            t = new Thread(this);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        } catch(InterruptedException e) {
            log.severe(e.getMessage());
        }
    }

    /**
     * Stop the movie and wait until the thread is finished.
     */
    @Override
    public void stop() {
        if(!this.start_stop) {
            if(size > 0)
                gotoFrame(0);
            return;
        }

        this.start_stop = false;

        try {
            if(t != null)
                t.join();
        } catch(InterruptedException e) {
            log.severe(e.getMessage());
        }
    }
    /** @see de.unibi.bibiserv.rnamovies.Movie.reset */
    @Override
    public void reset() {
        xtrans = 0.0;
        ytrans = 0.0;
        zoom = 100;
        so.rotAngle = 0.0;

        drawIt();
        repaint();
    }

    /** @see de.unibi.bibiserv.rnamovies.Movie.gotoFrame */
    @Override
    public void gotoFrame(int idx) {
        if(size == 0 || idx < 0 || idx >= size)
            throw new IllegalArgumentException("No such frame: " + idx + ".");

        stateLock.lock();
        try {
            step = 0;
            i = idx;
        } finally {
            stateLock.unlock();
            drawIt();
            repaint();
        }
    }

    /**
     * Skip back to last stable structure.
     */
    @Override
    public void bskip() {
        if(size <= 1)
            return;

        stateLock.lock();
        try {
            if(step == 0 || i == size - 1)
                i = --i < 0 ? (loop ? size - 1 : 0) : i;
            else
                step = 0;
        } finally {
            stateLock.unlock();
            drawIt();
            repaint();
        }
    }

    /**
     * Skip one structure.
     */
    @Override
    public void fskip() {
        if(size <= 1)
            return;

        stateLock.lock();
        try {
            step = 0;
            i = ++i >= size ? (loop ? 1 : size - 1) : i;
        } finally {
            stateLock.unlock();
            drawIt();
            repaint();
        }
    }

    /**
     * Draw the structure onto the pixmap.
     */
    private void drawIt() {
        int j, n;
        double zoomf;
        Graphics2D gc = null;

        if(backbuffer == null)
            return;

        if(size < 1) {
            gc = (Graphics2D)backbuffer.getGraphics();
            gc.setBackground(background);
            gc.clearRect(0, 0, getWidth(), getHeight());
            gc.dispose();
            return;
        }

        buffWLock.lock();
        stateLock.lock();
        try {
            gc = (Graphics2D)backbuffer.getGraphics();
            gc.setBackground(background);
            gc.clearRect(0, 0, getWidth(), getHeight());
            gc.setRenderingHints(rh);
            gc.setFont(f);


            // Paint on the backbuffer
            gc.setColor(textColor);
            gc.drawString((i + 1) + "/" + size, 5, getHeight() - 5);
            gc.drawString(zoom + "%", getWidth() - 35, getHeight() - 5);
            gc.drawString(title, (getWidth() - title_width)/2, getHeight() - 5);
            zoomf = 0.01 * zoom;
            gc.translate(((double)getWidth() - zoomf*width)/2.0 + xtrans,
                    ((double)getHeight() - zoomf*height)/2.0 + ytrans);
            gc.scale(zoomf, zoomf);

            j = i == size - 1 ? size - 1 : i + 1;
            n = step <= interpolations/2 ? i : (i == size - 1 ? size - 1 : i + 1);
            so.drawInterpolate(gc, chars,
                    frames.get(i), frames.get(j),
                    center, pairs.get(n),
                    step, interpolations);
            gc.dispose();
        } finally {
            buffWLock.unlock();
            stateLock.unlock();
        }
    }
    /** @see de.unibi.bibiserv.rnamoviws.getMaxWidth */
    @Override
    public int getMaxWidth() {
        return width;
    }
    /** @see de.unibi.bibiserv.rnamoviws.getMaxheigth */
    @Override
    public int getMaxHeight() {
        return height;
    }

    /** @see de.unibi.bibiserv.rnamovies.configuration.ConfigListener */
    @Override
    public void configurationChanged(ConfigChangedEvent e) {
        switch(e.getId()) {
            case 106:
                textColor = (Color)e.getValue();
                break;
            case 105:
                background = (Color)e.getValue();
                break;
            case 1:
                stateLock.lock();
                try {
                    speed = 2000 - ((BoundedRangeModel)e.getValue()).getValue();
                    interpolations = interpolate ? (int)Math.floor(FPMS * speed) : 1;
                    if(step >= interpolations) {
                        step = 0;
                        start_stop = (i != size - 2 || loop);
                        i = ++i >= size ? 0 : i;
                    }
                    frame = speed / interpolations;
                } finally {
                    stateLock.unlock();
                }
                break;
            case 2:
                stateLock.lock();
                try {
                    interpolate = ((Boolean)e.getValue()).booleanValue();
                    interpolations = interpolate ? (int)Math.floor(FPMS * speed) : 1;
                    if(step >= interpolations) {
                        step = 0;
                        start_stop = (i != size - 1 || loop);
                    }
                    frame = speed / interpolations;
                } finally {
                    stateLock.unlock();
                }
                break;
            case 0:
                stateLock.lock();
                try {
                    loop = ((Boolean)e.getValue()).booleanValue();
                } finally {
                    stateLock.unlock();
                }
                break;
            default:
        }
        drawIt();
        repaint();
    }

    private class ZoomListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int newZoom;

            newZoom = zoom - e.getWheelRotation();
            if(-e.getWheelRotation() > 0 && newZoom <= MAX_ZOOM)
                setZoom(newZoom);
            if(-e.getWheelRotation() < 0 && newZoom >= MIN_ZOOM)
                setZoom(newZoom);
        }
    }

    private class TranslationListener extends MouseInputAdapter {
        private int xprev, yprev;
        private double theta_prev;
        private boolean translate, rotate;
        private Cursor hand = new Cursor(Cursor.HAND_CURSOR);
        private Cursor old = null;

        @Override
        public void mousePressed(MouseEvent e) {
            double cx, cy, zoomf;

            switch(e.getButton()) {
                case MouseEvent.BUTTON1:
                    old = getCursor();
                    setCursor(hand);
                    xprev = e.getX();
                    yprev = e.getY();
                    translate = true;
                    break;
                case MouseEvent.BUTTON3:
                    zoomf = 0.01 * zoom;
                    cx = zoomf*center.getX();
                    cy = zoomf*center.getY();
                    cx += 0.5*((double)getWidth() - zoomf*width) + xtrans;
                    cy += 0.5*((double)getHeight() - zoomf*height) + ytrans;
                    theta_prev = Math.atan2(e.getY() - cy, e.getX() - cx);
                    rotate = true;
                    break;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            double cx, cy, zoomf;
            double theta;

            if(translate) {
                xtrans += e.getX() - xprev;
                ytrans += e.getY() - yprev;

                drawIt();
                repaint();

                xprev = e.getX();
                yprev = e.getY();
            } else if(rotate) {
                zoomf = 0.01 * zoom;
                cx = zoomf*center.getX();
                cy = zoomf*center.getY();
                cx += 0.5*((double)getWidth() - zoomf*width) + xtrans;
                cy += 0.5*((double)getHeight() - zoomf*height) + ytrans;
                theta = Math.atan2(e.getY() - cy, e.getX() - cx);
                so.rotAngle += theta - theta_prev;

                drawIt();
                repaint();

                theta_prev = theta;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            switch(e.getButton()) {
                case MouseEvent.BUTTON1:
                    if(old != null)
                        setCursor(old);
                    translate = false;
                    break;
                case MouseEvent.BUTTON3:
                    rotate = false;
                    break;
            }
        }
    }
}
