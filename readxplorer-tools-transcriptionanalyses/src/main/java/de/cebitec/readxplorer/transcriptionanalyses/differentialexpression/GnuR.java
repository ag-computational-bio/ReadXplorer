/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


import de.cebitec.readxplorer.utils.Properties;
import java.io.File;
import org.openide.util.NbPreferences;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


/**
 * Calls Gnu R.
 *
 * @author kstaderm
 */
public class GnuR extends RConnection {
    /**
     * The Cran Mirror used to receive additional packages.
     */
    private String cranMirror;

    /**
     * Creates a new instance of the class and initiates the cranMirror.
     */
    public GnuR() throws RserveException {
        setDefaultCranMirror();
    }

    /**
     * Clears up the memory of an runnig R instance. This should the called
     * every time before a new computation is startet. Doing so you can be sure
     * that no previous result is interfering with the new computation.
     */
    public void clearGnuR() throws RserveException {
        this.eval( "rm(list = ls(all = TRUE))" );
    }


    /**
     * Saves the memory of the current R instance to the given file.
     *
     * @param saveFile File the memory image should be saved to
     */
    public void saveDataToFile( File saveFile ) throws RserveException {
        String path = saveFile.getAbsolutePath();
        path = path.replace( "\\", "/" );
        this.eval( "save.image(\"" + path + "\")" );
    }


    private void setDefaultCranMirror() throws RserveException {
        cranMirror = NbPreferences.forModule( Object.class ).get( Properties.CRAN_MIRROR, "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/" );
        this.eval( "{r <- getOption(\"repos\"); r[\"CRAN\"] <- \"" + cranMirror + "\"; options(repos=r)}" );
    }


    /**
     * Loads the specified Gnu R package. If not installed the method will try
     * to download and install the package.
     *
     * @param packageName
     */
    public void loadPackage( String packageName ) throws PackageNotLoadableException, RserveException {
        REXP result = this.eval( "library(" + packageName + ')' );
        if( result == null ) {
            this.eval( "install.packages(\"" + packageName + "\")" );
            result = this.eval( "library(" + packageName + ')' );
            if( result == null ) {
                throw new PackageNotLoadableException( packageName );
            }
        }
    }


    public static class PackageNotLoadableException extends Exception {

        public PackageNotLoadableException( String packageName ) {
            super( "The Gnu R package " + packageName + " can't be loaded automatically. Please install it manually!" );
        }


    }


    public static class JRILibraryNotInPathException extends Exception {

        public JRILibraryNotInPathException() {
            super( "JRI native library can't be found in the PATH. Please add it to the PATH and try again." );
        }


    }


    public static class UnknownGnuRException extends Exception {

        public UnknownGnuRException( Exception e ) {
            super( "An unknown exception occurred in GNU R while processing your data. "
                   + "This caused an " + e.getClass().getName() + " on the Java side of the programm.", e );
        }


    }

    
    @Override
    public REXP eval(String cmd) throws RserveException {
        ProcessingLog.getInstance().logGNURoutput( "> " + cmd + "\n" );
        return super.eval(cmd);
    }

    @Override
    public REXP eval(REXP what, REXP where, boolean resolve) throws REngineException {
        
        return super.eval(what, where, resolve);
    }

    @Override
    public void assign(String sym, REXP rexp) throws RserveException {
        super.assign(sym, rexp);
    }

    @Override
    public void assign(String sym, String ct) throws RserveException {
        super.assign(sym, ct);
    }
    
    @Override
    public void assign(String symbol, REXP value, REXP env) throws REngineException {
        super.assign(symbol, value, env);
    }
    
    
    
//    @Override
//    public synchronized REXP eval( String string ) {
//        return eval( string, true );
//    }
//
//    
//
//    @Override
//    public synchronized REXP eval( String string, boolean bln ) {
//        ProcessingLog.getInstance().logGNURoutput( "> " + string + "\n" );
//        return super.eval( string, bln );
//    }
//
//
//    @Override
//    public boolean assign( String string, String string1 ) {
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + string1 + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, string1 );
//    }
//
//
//    @Override
//    public boolean assign( String string, REXP rexp ) {
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + rexp.asString() + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, rexp );
//    }
//
//
//    @Override
//    public boolean assign( String string, double[] doubles ) {
//        StringBuilder sb = new StringBuilder( doubles.length * 20 ).append( '[' );
//        for( int i = 0; i < doubles.length; i++ ) {
//            sb.append( doubles[i] ).append( ';' );
//        }
//        sb.deleteCharAt( sb.length() - 1 );
//        sb.append( ']' );
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, doubles );
//    }
//
//
//    @Override
//    public boolean assign( String string, int[] ints ) {
//        StringBuilder sb = new StringBuilder( ints.length * 12 ).append( '[' );
//        for( int i = 0; i < ints.length; i++ ) {
//            sb.append( ints[i] ).append( ';' );
//        }
//        sb.deleteCharAt( sb.length() - 1 );
//        sb.append( ']' );
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, ints );
//    }
//
//
//    @Override
//    public boolean assign( String string, boolean[] blns ) {
//        StringBuilder sb = new StringBuilder( blns.length * 6 ).append( '[' );
//        for( int i = 0; i < blns.length; i++ ) {
//            sb.append( blns[i] ).append( ';' );
//        }
//        sb.deleteCharAt( sb.length() - 1 );
//        sb.append( ']' );
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, blns );
//    }
//
//
//    @Override
//    public boolean assign( String string, String[] strings ) {
//        StringBuilder sb = new StringBuilder( strings.length * 20 ).append( '[' );
//        for( String string1 : strings ) {
//            sb.append( string1 ).append( ';' );
//        }
//        sb.deleteCharAt( sb.length() - 1 );
//        sb.append( ']' );
//        ProcessingLog.getInstance().logGNURoutput( "> assign: \"" + sb.toString() + "\" to variable \"" + string + "\"\n" );
//        return super.assign( string, strings );
//    }


    /**
     * Store an SVG file of a given plot using this GnuR instance.
     * <p>
     * @param file           File to store the plot in
     * @param plotIdentifier String identifying the data to plot
     * <p>
     * @throws
     * de.cebitec.readxplorer.differentialExpression.GnuR.PackageNotLoadableException
     * @throws IllegalStateException
     */
    public void storePlot( File file, String plotIdentifier ) throws PackageNotLoadableException, IllegalStateException, RserveException {
        if( this == null ) {
            throw new IllegalStateException( "Shutdown was already called!" );
        }
        this.loadPackage( "grDevices" );
        String path = file.getAbsolutePath();
        path = path.replace( "\\", "\\\\" );
        this.eval( "svg(filename=\"" + path + "\")" );
        this.eval( plotIdentifier );
        this.eval( "dev.off()" );
    }
}
