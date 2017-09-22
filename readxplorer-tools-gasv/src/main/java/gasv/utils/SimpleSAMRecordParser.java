
package gasv.utils;


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
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.openide.windows.InputOutput;


public class SimpleSAMRecordParser {

    private static final InputOutput IO = GASVCaller.IO;

    private static final SamReaderFactory SAMREADERFACTORY
            = SamReaderFactory.makeDefault()
                    .enable( SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS )
                    .validationStringency( ValidationStringency.LENIENT );


    public static void main( String[] args ) throws IOException {

        if( args.length != 2 ) {
            printUsage();
            return;
        }
        String bamfile = args[0];
        String outfile = args[1];

        int count = 0;
        try( BufferedWriter writer = new BufferedWriter( new FileWriter( outfile ) );
             final SamReader samReader = SAMREADERFACTORY.open( new File( bamfile ) ); ) {

            writer.write( "Name\tSAMFlag\tChr\tStart\tEnd\tNegativeStrand?\tQuality\tCIGAR\tNM\n" );
            for( SAMRecord s : samReader ) {
                /*
                 * Iterator<SAMTagAndValue> iter = s.getAttributes().iterator();
                 * while(iter.hasNext()) { SAMTagAndValue val = iter.next();
                 * IO.getOut().println(val.tag+" " +val.value); }
                 */

                writer.write( s.getReadName() + "\t" + s.getFlags() + "\t" + s.getReferenceName() + "\t" +
                              s.getAlignmentStart() + "\t" + s.getAlignmentEnd() + "\t" +
                              s.getReadNegativeStrandFlag() + "\t" + s.getMappingQuality() + "\t" +
                              s.getCigarString() + "\t" + s.getAttribute( "NM" ) + "\n" );
                if( count % 500000 == 0 ) {
                    IO.getOut().println( "  record " + count + "..." );
                }
                count++;
            }

        }

    }


    public static void printUsage() {
        IO.getOut().println( "USAGE: java -jar SimpleSAMRecordParser.jar <input_bam_file> <output_file>" );
        IO.getOut().println( "\t<input_bam_file> is the input file" );
        IO.getOut().println( "\t<output_file> is a tab-delimited file with the following columns:" );
        IO.getOut().println( "\t\t<name>\tRecord name" );
        IO.getOut().println( "\t\t<flag>\tSAM Flag" );
        IO.getOut().println( "\t\t<chr>\tChomosome (or reference) name" );
        IO.getOut().println( "\t\t<start>\talignment start (softclipped, according to Picard)" );
        IO.getOut().println( "\t\t<end>\talignment end (softclipped, according to Picard)" );
        IO.getOut().println( "\t\t<neg>\ttrue if the alignment is on the negative strand, false otherwise" );
        IO.getOut().println( "\t\t<qual>\tmapping quality" );
        IO.getOut().println( "\t\t<cigar>\tCIGAR string" );
        IO.getOut().println( "\t\t<NM>\tEdit distance to the reference (excluding clipping)" );

    }


}
