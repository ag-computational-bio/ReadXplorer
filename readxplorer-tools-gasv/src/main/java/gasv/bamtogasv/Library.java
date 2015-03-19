
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
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.sf.samtools.SAMRecord;


public class Library {

    public String name;
    public ArrayList<GASVPair> firstNreads;
    public boolean pairedTag, mateFound, computedStats;
    public int Lmin, Lmax, counter, total_L, minRead_L, total_C, total_RL, mean, std;
    public TreeMap<Integer, Integer> numConcord;
    public TreeMap<Integer, Integer> lengthHist; /// TreeMap is sorted
    public HashMap<VariantType, Integer> numTmpFilesForVariant; // <variant_type, # of tmp files written>
    public HashMap<VariantType, ArrayList<String>> rowsForVariant; // <variant_type,lines to sort>


    public Library( String n ) {
        name = n;
        mateFound = false;
        total_L = 0;
        total_C = 0;
        total_RL = 0; // counting total read length
        Lmin = Integer.MIN_VALUE;
        Lmax = Integer.MIN_VALUE;
        minRead_L = Integer.MAX_VALUE;
        lengthHist = new TreeMap<Integer, Integer>();
        numConcord = new TreeMap<Integer, Integer>();
        counter = 0;
        firstNreads = new ArrayList<GASVPair>();
        pairedTag = false;
        computedStats = false;

        rowsForVariant = new HashMap<VariantType, ArrayList<String>>();
        numTmpFilesForVariant = new HashMap<VariantType, Integer>();
        VariantType[] varList = VariantType.values();
        for( int v = 0; v < varList.length; v++ ) {
            numTmpFilesForVariant.put( varList[v], 0 );
            clearVariantBuffer( varList[v] );
        }
    }


    // resets numLinesForVariant and rowsForVariant.

    public void clearVariantBuffer( VariantType t ) {
        rowsForVariant.put( t, new ArrayList<String>() );
    }


    public void addLine( VariantType t, String line ) {
        rowsForVariant.get( t ).add( line );
        //System.out.println("Library " + name + " type " + t + " has " + rowsForVariant.get(t).size() + " lines.");
    }


    public void isRecordPaired( SAMRecord s ) {
        if( s.getReadPairedFlag() &&
                 !s.getReadUnmappedFlag() &&
                 !s.getMateUnmappedFlag() ) {
            mateFound = true;
        }
    }


    private float getAverageInsertLength() {
        return (float) (total_L) / total_C;
    }


    private float getAverageReadLength() {
        return (float) (total_RL) / total_C;
    }


    public float getGlobalAvgInsertLength() {
        return getnumConcordGenome() * getAverageInsertLength();
    }


    public float getGlobalAvgReadLength() {
        return getnumConcordGenome() * getAverageReadLength();
    }


    public long getnumConcordGenome() {
        long total_genome = 0L;
        Iterator<Map.Entry<Integer, Integer>> iter = numConcord.entrySet().iterator();
        while( iter.hasNext() ) {
            Map.Entry<Integer, Integer> libtmp = iter.next();
            total_genome += libtmp.getValue().intValue();
        }
        return total_genome;
    }


    public String getConcordDist() {
        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<Integer, Integer>> iter = numConcord.entrySet().iterator();
        while( iter.hasNext() ) {
            Map.Entry<Integer, Integer> libtmp = iter.next();
            buffer.append( libtmp.getKey().toString() + "-" + libtmp.getValue().toString() );
            if( iter.hasNext() ) {
                buffer.append( "\t" );
            }
        }
        return buffer.toString();
    }


}
