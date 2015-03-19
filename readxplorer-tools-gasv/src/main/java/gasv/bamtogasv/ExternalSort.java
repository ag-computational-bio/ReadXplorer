
package gasv.bamtogasv;


/**
 * Copyright 2010,2012 Benjamin Raphael, Suzanne Sindi, Hsin-Ta Wu, Anna Ritz,
 * Luke Peng, Layla Oesper
 * <p>
 * This file is part of GASV.
 * <p>
 * gasv is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * GASV is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * gasv. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 */
import bio.comp.jlu.readxplorer.tools.gasv.GASVCaller;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.windows.InputOutput;


/**
 * Implements External Sort to sort large files.
 * <p>
 * @author Anna
 * <p>
 * adapted from
 * http://www.codeodor.com/index.cfm/2007/5/14/Re-Sorting-really-BIG-files---the-Java-source-code/1208
 */
public class ExternalSort {

    private static final InputOutput IO = GASVCaller.IO;

    public final int NUM_LINES = 10000000;
    //public final int NUM_LINES = 3; // testing purposes
    public String infile, outfile;
    public int[] sortorder;
    public int numFiles;
    public String header;
    public boolean firstLine;
    public boolean ESPfile;


    public static void main( String[] args ) {
        ExternalSort sorter = null;
        try {
            sorter = new ExternalSort( args );
        } catch( NumberFormatException e ) {
            if( args.length != 2 ) {
                printUsage();
                System.exit( -1 );
            }
            try {
                sorter = new ExternalSort( args[0], args[1] );
            } catch( Exception e2 ) {
                printUsage();
                System.exit( -1 );
            }
        }
        sorter.split();
        sorter.merge();
    }


    public static void printUsage() {
        IO.getOut().println( "USAGE: java ExternalSort <file_to_sort> <primaryCol> <secondaryCol>...\n" +
                             "\t<file_to_sort> is the file to sort. \n" +
                             "\t<primaryCol> <secondaryCol> ... is a list of signed integers indicating the sorting order.  " +
                             "Ties in the primary column are broken by the secondary column and so on.  Positive intgers indicate increasing " +
                             "order and negative integers indicate decreasing order.  All columns to sort on MUST be " +
                             "numbers (examples are 50,-0.234,1E-14). \n" +
                             "OUTPUT: <file_to_sort>.sorted" );
        IO.getOut().println( "ESP USAGE: Java ExternalSort <file_to_sort> ESP\n" +
                             "Sorts the ESP file according to GASV specifications.\n" +
                             "OUTPUT: <file_to_sort>.sorted" );
    }


    public ExternalSort( int[] so ) {
        infile = null;
        outfile = null;
        sortorder = so;
        numFiles = 0;
        header = null;
        firstLine = true;
    }


    public ExternalSort( String str ) throws Exception {
        if( !str.equalsIgnoreCase( "ESP" ) ) {
            throw new Exception( "String " + str + " has to be \"ESP\"" );
        }
        ESPfile = true;
        infile = null;
        outfile = null;
        int[] esporder = { 2, 6, 3, 7 };
        sortorder = esporder;
        header = null;
        firstLine = false;
    }


    public ExternalSort( String filename, String str ) throws Exception {
        if( !str.equalsIgnoreCase( "ESP" ) ) {
            throw new Exception( "String " + str + " has to be \"ESP\"" );
        }
        ESPfile = true;
        infile = filename;
        outfile = filename + ".sorted";
        int[] esporder = { 2, 6, 3, 7 };
        sortorder = esporder;
        header = null;
        firstLine = false;
    }


    public ExternalSort( String in, int[] so ) {
        infile = in;
        outfile = in + ".sorted";
        sortorder = so;
        numFiles = 0;
        header = "";
        firstLine = true;
        ESPfile = false;
    }


    public ExternalSort( String[] args ) throws NumberFormatException {
        infile = args[0];
        outfile = infile + ".sorted";
        sortorder = new int[args.length - 1];
        for( int i = 1; i < args.length; i++ ) {
            sortorder[i - 1] = Integer.parseInt( args[i] );
        }
        numFiles = 0;
        firstLine = true;
        ESPfile = false;
        header = "";
    }


    public void split() {
        IO.getOut().println( "Reading file in chunks of " + NUM_LINES + ", sorting, and writing them to temp files." );
        try {
            BufferedReader in = new BufferedReader( new FileReader( infile ) );
            ArrayList<SortElement> rows = new ArrayList<SortElement>();
            String line = "";
            while( line != null ) {
                rows.clear();
                IO.getOut().println( "  file #" + numFiles );

                // read next NUM_LINES lines.
                for( int i = 0; i < NUM_LINES; i++ ) {
                    line = in.readLine();

                    // if we're at the end, we're done.
                    if( line == null ) {
                        break;
                    }

					// check to see if the first line has a header.
                    // It has a header if the value is NOT parsed as a double.
                    if( firstLine ) {
                        SortElement e = new SortElement( line );
                        if( header.equals( "" ) ) {
                            rows.add( e );
                        }
                        firstLine = false;
                    } else {
                        rows.add( new SortElement( line ) );
                    }

                }
                // sort the rows.  SortElement knows how to sort according to the sortorder[] array.
                Collections.sort( rows );

                // write to disk
                BufferedWriter bw = new BufferedWriter( new FileWriter( infile + "_tmp" + numFiles + ".txt" ) );
                for( int i = 0; i < rows.size(); i++ ) {
                    bw.append( rows.get( i ).line + "\n" );
                }
                bw.close();

                numFiles++;
            }
            in.close();

            IO.getOut().println( "there are " + numFiles + " files." );
        } catch( Exception ex ) {
            IO.getOut().println( "ERROR: Something went wrong when splitting & sorting the files." );
            ex.printStackTrace();
            System.exit( -1 );
        }
    }


    public void sort( ArrayList<String> lines, String outputname ) throws IOException {
        ArrayList<SortElement> rows = new ArrayList<SortElement>();
        SortElement e;

        for( int i = 0; i < lines.size(); i++ ) {

            // First, create SortElement from lines.get(i)
            e = new SortElement( lines.get( i ) );
            if( !e.line.equals( "" ) && !e.line.contains( "null" ) ) {
                rows.add( e );
            }

            // then, set lines.get(i) to null.
            lines.set( i, null );
        }
        // finally, set lines arraylist to null.
        lines = null;

        // sort the ArrayList of SortElements.
        Collections.sort( rows );

        // write to disk
        BufferedWriter bw = new BufferedWriter( new FileWriter( outputname ) );
        for( int i = 0; i < rows.size(); i++ ) {
            bw.write( rows.get( i ).line + "\n" );
        }
        bw.close();
    }


    public void merge() {
        IO.getOut().println( "Merging files..." );
        try {
            ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
            ArrayList<SortElement> filerows = new ArrayList<SortElement>();
            BufferedWriter out = new BufferedWriter( new FileWriter( outfile ) );

            if( !header.equals( "" ) ) {
                out.write( header + "\n" );
            }

            boolean someFileStillHasRows = false;
            for( int i = 0; i < numFiles; i++ ) {
                mergefbr.add( new BufferedReader( new FileReader( infile + "_tmp" + i + ".txt" ) ) );

                // get the first row
                String line = mergefbr.get( i ).readLine();
                if( line != null ) {
                    filerows.add( new SortElement( line ) );
                    someFileStillHasRows = true;
                } else {
                    filerows.add( null );
                }
            }

            SortElement row;
            SortElement min;
            int minIndex;
            while( someFileStillHasRows ) {
                min = null;
                minIndex = -1;

                // check which one is min
                for( int i = 0; i < filerows.size(); i++ ) {
                    row = filerows.get( i );
                    if( row != null && (min == null || row.compareTo( min ) < 0) ) {
                        min = row;
                        minIndex = i;
                    }
                }

                if( minIndex < 0 ) // ALL rows are null
                {
                    someFileStillHasRows = false;
                } else {
                    // write to the sorted file
                    out.append( filerows.get( minIndex ).line + "\n" );

                    // get another row from the file that had the min
                    String line = mergefbr.get( minIndex ).readLine();
                    if( line != null ) {
                        filerows.set( minIndex, new SortElement( line ) );
                    } else {
                        filerows.set( minIndex, null );
                    }
                }
            }

            // at this point, all filerows should be null.
            for( int i = 0; i < filerows.size(); i++ ) {
                if( filerows.get( i ) != null ) {
                    IO.getOut().println( "ERROR: minIndex <= 0 and found row not null \"" + filerows.get( i ).line + "\"" );
                    System.exit( -1 );
                }
            }

            // close all the files
            out.close();
            for( int i = 0; i < mergefbr.size(); i++ ) {
                mergefbr.get( i ).close();
            }

            // delete all intermediate files.
            try {
                for( int i = 0; i < numFiles; i++ ) {
                    new File( infile + "_tmp" + i + ".txt" ).delete();
                }
            } catch( Exception e ) {
                IO.getOut().println( "WARNING: cannot delete temporary file" );
            }
        } catch( Exception ex ) {
            IO.getOut().println( "ERROR: Something wrong happened when merging the files." );
            ex.printStackTrace();
            System.exit( -1 );
        }
    }


    public void merge( ArrayList<String> files, String outputfile ) {
        try {
            ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
            ArrayList<SortElement> filerows = new ArrayList<SortElement>();
            BufferedWriter out = new BufferedWriter( new FileWriter( outputfile ) );

            boolean someFileStillHasRows = false;
            for( int i = 0; i < files.size(); i++ ) {
                mergefbr.add( new BufferedReader( new FileReader( files.get( i ) ) ) );

                // get the first row
                String line = mergefbr.get( i ).readLine();
                if( line != null ) {
                    filerows.add( new SortElement( line ) );
                    someFileStillHasRows = true;
                } else {
                    filerows.add( null );
                }
            }

            SortElement row;
            SortElement min;
            int minIndex;
            while( someFileStillHasRows ) {
                min = null;
                minIndex = -1;

                // check which one is min
                for( int i = 0; i < filerows.size(); i++ ) {
                    row = filerows.get( i );
                    if( row != null && (min == null || row.compareTo( min ) < 0) ) {
                        min = row;
                        minIndex = i;
                    }
                }

                if( minIndex < 0 ) // ALL rows are null
                {
                    someFileStillHasRows = false;
                } else {
                    // write to the sorted file
                    out.append( filerows.get( minIndex ).line + "\n" );

                    // get another row from the file that had the min
                    String line = mergefbr.get( minIndex ).readLine();
                    if( line != null ) {
                        filerows.set( minIndex, new SortElement( line ) );
                    } else {
                        filerows.set( minIndex, null );
                    }
                }
            }

            // at this point, all filerows should be null.
            for( int i = 0; i < filerows.size(); i++ ) {
                if( filerows.get( i ) != null ) {
                    IO.getOut().println( "ERROR: minIndex <= 0 and found row not null \"" + filerows.get( i ).line + "\"" );
                    System.exit( -1 );
                }
            }

            // close all the files
            out.close();
            for( int i = 0; i < mergefbr.size(); i++ ) {
                mergefbr.get( i ).close();
            }

            // delete all intermediate files.
            try {
                for( int i = 0; i < files.size(); i++ ) {
                    new File( files.get( i ) ).delete();
                }
            } catch( Exception e ) {
                IO.getOut().println( "WARNING: cannot delete temporary file." );
            }
        } catch( Exception ex ) {
            IO.getOut().println( "ERROR: Something wrong happened when merging the files." );
            ex.printStackTrace();
            System.exit( -1 );
        }

    }


    /**
     * This nested class is a sort element. It uses the sortorder array to
     * determine order.
     * <p>
     * @author Anna
     * <p>
     */
    class SortElement implements Comparable<SortElement> {

        String[] row;
        String line;

        // TODO: could write a getOrigLIne() function that puts the row array back together


        public SortElement( String str ) {
            line = str;
            row = str.split( "\\s+" );

            if( ESPfile ) {
				// If first read is on the negative strand, flip
                // coordinates for sorting.
                if( row[4].equals( "-" ) || row[4].equalsIgnoreCase( "MINUS" ) ) {
                    String tmp = row[2];
                    row[2] = row[3];
                    row[3] = tmp;
                }

				// If second read is on the negative strand, flip
                // coordinates for sorting.
                if( row[8].equals( "-" ) || row[8].equalsIgnoreCase( "MINUS" ) ) {
                    String tmp = row[6];
                    row[6] = row[7];
                    row[7] = tmp;
                }
            }
            if( firstLine ) {
                try {
                    Double.parseDouble( row[Math.abs( sortorder[0] ) - 1] );
                } catch( Exception e ) {
                    IO.getOut().println( "  Header Line Detected: \"" + line + "\"" );
                    header = line;
                }
            }
        }


        @Override
        public int compareTo( SortElement se ) {
            int index = -1;
            String order = "";
            for( int i = 0; i < sortorder.length; i++ ) {
                try {
                    index = sortorder[i];
                    if( index > 0 ) { // sort increasing
                        order = "increasing";
                        if( Double.parseDouble( row[index - 1] ) < Double.parseDouble( se.row[index - 1] ) ) {
                            return -1;
                        }
                        if( Double.parseDouble( row[index - 1] ) > Double.parseDouble( se.row[index - 1] ) ) {
                            return 1;
                        }
                    } else { // sort decreasing
                        order = "decreasing";
                        index = index * -1;
                        if( Double.parseDouble( row[index - 1] ) < Double.parseDouble( se.row[index - 1] ) ) {
                            return 1;
                        }
                        if( Double.parseDouble( row[index - 1] ) > Double.parseDouble( se.row[index - 1] ) ) {
                            return -1;
                        }
                    }
                } catch( NumberFormatException e ) {
                    IO.getOut().println( "ERROR: column " + index + " (" + order + ") did not parse as a double when comparing the following (1-based):" );
                    IO.getOut().println( "\"" + line + "\"" );
                    IO.getOut().println( "\"" + se.line + "\"" );
                    e.printStackTrace();
                    System.exit( -1 );
                }
            }
            // everything's the same - return 0.
            return 0;
        }


    }

}
