
package de.cebitec.readXplorer.transcriptomeAnalyses.main;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.RPKMvalue;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JOptionPane;


/**
 * This class provides all methods for RPKM value calculation.
 *
 * @author jritter
 */
public class RPKMValuesCalculation {

    private final List<RPKMvalue> rpkmValues;
    /**
     * Key: featureID , Value: PersistentFeature
     */
    private final Map<Integer, PersistentFeature> allRegionsInHash;
    private final StatisticsOnMappingData stats;
    private final int[][] forwardStarts, reverseStarts;
    private final double mm, mc;
    private final int trackId;


    /**
     * This class provides all methods for RPKM value calculation.
     *
     * @param persFeatures All persistent genome features.
     * @param stats        StatisticsOnMappingData instance.
     * @param trackId      Currently used Track id.
     */
    public RPKMValuesCalculation( Map<Integer, PersistentFeature> persFeatures, StatisticsOnMappingData stats, int trackId ) {
        this.allRegionsInHash = persFeatures;
        this.rpkmValues = new ArrayList<>();
        this.stats = stats;
        this.forwardStarts = this.stats.getForwardReadStarts();
        this.reverseStarts = this.stats.getReverseReadStarts();
        this.mm = this.stats.getMappingsPerMillion();
        this.mc = this.stats.getMappingCount();
        this.trackId = trackId;

    }


    /**
     * This method calculates two kind of RPKM values.
     *
     * @param refGenome Persistent reference genome.
     */
    public void calculationExpressionValues( PersistentReference refGenome, File referenceFile ) {
        Map<String, Integer[]> staticRegions = null;
        if( referenceFile != null ) {
            staticRegions = parseStaticRegionFile( referenceFile );
        }

        Map<Integer, PersistentFeature> allRegionsSorted = new TreeMap<>( this.allRegionsInHash );
        HashMap<Integer, PersistentChromosome> chromosomes = (HashMap<Integer, PersistentChromosome>) refGenome.getChromosomes();

        for( PersistentFeature feature : allRegionsSorted.values() ) {
            if( feature.getType() != FeatureType.CDS ) {
                continue;
            }

            int start;
            int stop;
            if( staticRegions == null ) {
                start = feature.getStart();
                stop = feature.getStop();
            }
            else {
                if( staticRegions.containsKey( feature.getLocus() ) ) {
                    start = staticRegions.get( feature.getLocus() )[0];
                    stop = staticRegions.get( feature.getLocus() )[1];
                }
                else {
                    start = feature.getStart();
                    stop = feature.getStop();
                }
            }

            boolean isFwd = feature.isFwdStrand();
            int chromId = feature.getChromId();
            int chromNo = chromosomes.get( chromId ).getChromNumber();

            RPKMvalue rpkm;

            if( isFwd ) {
//                System.out.println("Feature fwd: " + feature.getLocus());
                rpkm = this.calculateRpkmValue( chromNo, chromId, start, stop, forwardStarts, this.mm );
                rpkm.setFeature( feature );
                if( staticRegions != null ) {
                    if( staticRegions.containsKey( feature.getLocus() ) ) {
                        rpkm.setLongestKnownUtrLength( staticRegions.get( feature.getLocus() )[2] );
                    }
                }
                rpkmValues.add( rpkm );
            }
            else {
//                System.out.println("Feature rev: " + feature.getLocus());
                rpkm = this.calculateRpkmValue( chromNo, chromId, start, stop, reverseStarts, this.mm );
                rpkm.setFeature( feature );
                if( staticRegions != null ) {
                    if( staticRegions.containsKey( feature.getLocus() ) ) {
                        rpkm.setLongestKnownUtrLength( staticRegions.get( feature.getLocus() )[2] );
                    }
                }
                rpkmValues.add( rpkm );
            }
        }
    }


    /**
     * It determines two kinds of RPKM valuse.
     * <p>
     * @param start  Startposition of analyzed feature.
     * @param stop   Stopposition of analyzed feature.
     * @param starts Array containing all mapping start counts on position i.
     *               mappings.
     * @param mm     number of mapped reads per million.
     *
     * @return RPKMvalue.
     */
    private RPKMvalue calculateRpkmValue( int chromNo, int chromId, int start, int stop, int[][] starts, double mm ) {

        start++;
        stop++;
        int length = stop - start;
        length++;
        List<Double> logdata = new ArrayList<>();
        double rpkm;
        double logRpkm = 0;
        double sumLog = 0;

        // Here we count the occurrences of start position which are not empty (count)
        // and add up the number of mappings per start (sum)
        // Also generate an array with logarithmic values (number of mappings per start)
        int count = 0;
        int readCounts = 0;
        for( int i = start - 1; i < stop; i++ ) {
            int j = starts[chromNo - 1][i];
            if( j != 0 ) {
                count++;
                readCounts += j;
                Integer integer = j;
                double logValue = Math.log( integer.doubleValue() );
                sumLog += logValue;
                logdata.add( logValue );
            }
        }
        String resultRpkm = String.format( "%2.2f", (double) readCounts / (double) length * 1000 / mm );
        rpkm = Double.valueOf( resultRpkm.replaceAll( ",", "." ) );
        // ===================================================================
        double meanLog = mean( logdata );
        if( sumLog > 0 ) {
            String resultLogRpkm = String.format( "%2.2f", (double) Math.exp( meanLog ) * ((double) count) / ((double) length) * 1000 / mm );
            logRpkm = Double.valueOf( resultLogRpkm.replaceAll( ",", "." ) );
        }
        // ==================================================================
        return new RPKMvalue( null, rpkm, logRpkm, readCounts, this.trackId, chromId );
    }


    /**
     * the array double[] m MUST BE SORTED
     *
     */
    private double median( List<Double> m ) {
        int length = m.size();
//        System.out.println("Length: " + length);
        int middle = length / 2;
//        System.out.println("Middle: " + middle);
        if( length % 2 == 1 ) {
            return m.get( middle );
        }
        else {
            return (m.get( middle - 1 ) + m.get( middle )) / 2.0;
        }
    }


    public List<RPKMvalue> getRpkmValues() {
        return rpkmValues;
    }


    /**
     *
     * @param m
     *          <p>
     * @return
     */
    private double mean( List<Double> m ) {
        double sum = 0;
        for( Double m1 : m ) {
            sum += m1;
        }
        return sum / m.size();
    }


    /**
     *
     * @param referenceFile
     *                      <p>
     * @return
     */
    private Map<String, Integer[]> parseStaticRegionFile( File referenceFile ) {
        Map<String, Integer[]> resultMap = new TreeMap<>();

        try( BufferedReader br = new BufferedReader( new FileReader( referenceFile ) ) ) {
            String line = br.readLine();

            while( line != null && !line.isEmpty() && !line.startsWith( "#" ) ) {
                // skip header line !
                String[] split = line.split( "\t" );
                Integer[] startStop = new Integer[3];
                startStop[0] = Integer.valueOf( split[1] );
                startStop[1] = Integer.valueOf( split[2] );

                // split UTR column and get the corresponding (longest) utr
                String[] utrs = split[4].split( "-" );
                int longestUtr = 0;
                for( String utr : utrs ) {
                    if( Integer.valueOf( utr ) > longestUtr ) {
                        longestUtr = Integer.valueOf( utr );
                    }
                }

                startStop[2] = longestUtr;
                resultMap.put( split[0], startStop );

                line = br.readLine();
            }

        }
        catch( FileNotFoundException ex ) {
            JOptionPane.showMessageDialog( null, "The reference file could not be opend or does not exists." + ex.toString(), "Problem with filehandling", JOptionPane.CLOSED_OPTION );
        }
        catch( IOException ex ) {
            JOptionPane.showMessageDialog( null, "Problems with the reference file." + ex.toString(), "Problem with IO!", JOptionPane.CLOSED_OPTION );
        }

        return resultMap;
    }


}
