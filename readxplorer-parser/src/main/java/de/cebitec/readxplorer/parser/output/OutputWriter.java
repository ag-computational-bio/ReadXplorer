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

package de.cebitec.readxplorer.parser.output;


/**
 * Contains different output parsers.
 *
 * @author Rolf Hilker
 */
public class OutputWriter {

    private OutputWriter() {
    }


    /**
     * Generates a string formatted in fasta format.
     * <p>
     * @param sequence         the sequence to be stored
     * @param headerParameters the strings to be contained in the header line of
     *                         the fasta
     * <p>
     * @return the sequence string formatted in fasta format
     */
    public static String generateFasta( String sequence, String... headerParameters ) {

        StringBuilder sb = new StringBuilder( sequence.length() + (sequence.length()/80)*4 + headerParameters.length*50 );

        sb.append( '>' );
        for( int i = 0; i < headerParameters.length; ++i ) {
            sb.append( headerParameters[i] ).append( ' ' );
        }
        sb.append( "\r\n" );

        final int lineLength = 80;
        final int seqLength = sequence.length();
        int i = 0;
        while( i < seqLength ) {
            int end = i + lineLength;

            if( end > seqLength ) {
                end = i + (seqLength - i);
            }
            sb.append( sequence.substring( i, end ) ).append( "\r\n" );
            i += lineLength;
        }

        return sb.toString();

    }


}
