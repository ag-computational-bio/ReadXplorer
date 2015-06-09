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

package de.cebitec.readxplorer.databackend.connector;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.Strand;
import de.cebitec.readxplorer.databackend.FieldNames;
import de.cebitec.readxplorer.databackend.SQLStatements;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The reference genome connector is responsible for the connection to a
 * reference genome.
 *
 * @author ddoppmeier, rhilker
 */
public class ReferenceConnector {

    private static final Logger LOG = Logger.getLogger( ReferenceConnector.class.getName() );

    private final int refGenID;
    private final Connection con;
    private final List<PersistentTrack> associatedTracks;


    /**
     * The reference genome connector is responsible for the connection to a
     * reference genome.
     * <p>
     * @param refGenID id of the associated reference genome
     */
    ReferenceConnector( int refGenID ) {

        ProjectConnector projectConnector = ProjectConnector.getInstance();

        this.refGenID = refGenID;
        this.con = projectConnector.getConnection();
        this.associatedTracks = new ArrayList<>();

    }


    /**
     * @return Fetches the reference genome of the reference associated with
     *         this connector. If it was called once, it is kept in memory and does not
     *         need to be fetched from the DB again.
     */
    public PersistentReference getRefGenome() {

        LOG.log( Level.INFO, "Loading reference genome with id  \"{0}\" from database", refGenID );
        PersistentReference reference = null;

        try( final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_SINGLE_GENOME ) ) {
            fetch.setLong( 1, refGenID );

            try( final ResultSet rs = fetch.executeQuery() ) {
                if( rs.next() ) {
                    String name = rs.getString( FieldNames.REF_GEN_NAME );
                    String description = rs.getString( FieldNames.REF_GEN_DESCRIPTION );
                    Timestamp time = rs.getTimestamp( FieldNames.REF_GEN_TIMESTAMP );
                    File fastaFile = new File( rs.getString( FieldNames.REF_GEN_FASTA_FILE ) );

                    reference = new PersistentReference( refGenID, name, description, time, fastaFile );
                }
            }

        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return reference;

    }


    /**
     * @return All chromosomes of this reference without their sequence.
     */
    public Map<Integer, PersistentChromosome> getChromosomesForGenome() {

        LOG.log( Level.INFO, "Loading chromosomes for reference with id  \"{0}\" from database", refGenID );

        Map<Integer, PersistentChromosome> chromosomes = new HashMap<>();
        try( final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_CHROMOSOMES ) ) {
            fetch.setLong( 1, refGenID );

            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.CHROM_ID );
                    int chromNumber = rs.getInt( FieldNames.CHROM_NUMBER );
                    String name = rs.getString( FieldNames.CHROM_NAME );
                    int chromLength = rs.getInt( FieldNames.CHROM_LENGTH );

                    chromosomes.put( id, new PersistentChromosome( id, chromNumber, refGenID, name, chromLength ) );
                }
            }

        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return chromosomes;

    }


    /**
     * @param chromId the id of the chromosome to fetch
     * <p>
     * @return One chromosome of this reference without its sequence.
     */
    public PersistentChromosome getChromosomeForGenome( final int chromId ) {

        LOG.log( Level.INFO, "Loading chromosome for reference with id  \"{0}\" from database", refGenID );

        PersistentChromosome chrom = null;
        try( final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_CHROMOSOME ) ) {
            fetch.setLong( 1, chromId );

            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    int chromNumber = rs.getInt( FieldNames.CHROM_NUMBER );
                    String name = rs.getString( FieldNames.CHROM_NAME );
                    int chromLength = rs.getInt( FieldNames.CHROM_LENGTH );

                    chrom = new PersistentChromosome( chromId, chromNumber, refGenID, name, chromLength );
                }
            }

        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return chrom;

    }


    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference.
     *
     * @param from        start position of the region of interest
     * @param to          end position of the region of interest
     * @param featureType type of features to retrieve from the db. Either
     *                    FeatureType.ANY or a specified type
     * @param chromId     chromosome id of the features of interest
     * <p>
     * @return the list of all features found in the interval of interest
     */
    public List<PersistentFeature> getFeaturesForRegion( final int from, final int to, final FeatureType featureType, final int chromId ) {

        List<PersistentFeature> features = new ArrayList<>();
        try {
            final PreparedStatement fetch;
            if( featureType == FeatureType.ANY ) {
                fetch = con.prepareStatement( SQLStatements.FETCH_FEATURES_FOR_CHROM_INTERVAL );
                fetch.setLong( 1, chromId );
                fetch.setInt( 2, from );
                fetch.setInt( 3, to );
            } else {
                fetch = con.prepareStatement( SQLStatements.FETCH_SPECIFIED_FEATURES_FOR_CHROM_INTERVAL );
                fetch.setLong( 1, chromId );
                fetch.setInt( 2, from );
                fetch.setInt( 3, to );
                fetch.setInt( 4, featureType.getType() );
            }

            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.FEATURE_ID );
                    String parentIds = rs.getString( FieldNames.FEATURE_PARENT_IDS );
                    parentIds = parentIds.equals( "0" ) ? "" : parentIds;
                    String ecnum = rs.getString( FieldNames.FEATURE_EC_NUM );
                    String locus = rs.getString( FieldNames.FEATURE_LOCUS_TAG );
                    String product = rs.getString( FieldNames.FEATURE_PRODUCT );
                    int start = rs.getInt( FieldNames.FEATURE_START );
                    int stop = rs.getInt( FieldNames.FEATURE_STOP );
                    boolean isFwdStrand = rs.getInt( FieldNames.FEATURE_STRAND ) == Strand.Forward.getType();
                    FeatureType type = FeatureType.getFeatureType( rs.getInt( FieldNames.FEATURE_TYPE ) );
                    String gene = rs.getString( FieldNames.FEATURE_GENE );

                    features.add( new PersistentFeature( id, chromId, parentIds, ecnum, locus, product, start, stop, isFwdStrand, type, gene ) );
                }
            }
            fetch.close();

        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return features;

    }


    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference.
     *
     * @param from         start position of the region of interest
     * @param to           end position of the region of interest
     * @param featureTypes list of features used to retrieve from the db.
     * @param chromId      chromosome id of the features of interest
     * <p>
     * @return the list of all features found in the interval of interest
     */
    public List<PersistentFeature> getFeaturesForRegion( final int from, final int to, final Set<FeatureType> featureTypes, final int chromId ) {

        List<PersistentFeature> features = new ArrayList<>();
        for( FeatureType featureType : featureTypes ) {
            features.addAll( getFeaturesForRegion( from, to, featureType, chromId ) );
        }
        return features;

    }


    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference including all parent-children relationships between the
     * features.
     * <br>An Example:
     * <br>Gene is included, CDS is excluded:
     * <br>All CDS belonging to a gene are preserved in the feature hierarchy of
     * that gene, while all CDS from the result list level are discarded
     * <p>
     * @param from        start position of the region of interest
     * @param to          end position of the region of interest
     * @param featureType type of features to retrieve from the db. Either
     *                    FeatureType.ANY or a specified type
     * @param chromId     chromosome id of the features of interest
     * <p>
     * @return the list of all features found in the interval of interest
     *         including their parent and children relationships
     */
    public List<PersistentFeature> getFeaturesForRegionInclParents( final int from, final int to, final FeatureType featureType, final int chromId ) {

        List<PersistentFeature> features = getFeaturesForRegion( from, to, FeatureType.ANY, chromId );
        PersistentFeature.Utils.addParentFeatures( features );
        return PersistentFeature.Utils.filterFeatureTypes( features, featureType );

    }


    /**
     * Fetches all features which at least partly overlap a given region of the
     * reference including all parent-children relationships between the
     * features.
     * <br>An Example:
     * <br>Gene is included, CDS is excluded:
     * <br>All CDS belonging to a gene are preserved in the feature hierarchy of
     * that gene, while all CDS from the result list level are discarded
     * <p>
     * @param from         start position of the region of interest
     * @param to           end position of the region of interest
     * @param featureTypes list of features used to retrieve from the db.
     * @param chromId      chromosome id of the features of interest
     * <p>
     * @return the list of all features found in the interval of interest
     *         including their parent and children relationships
     */
    public List<PersistentFeature> getFeaturesForRegionInclParents( final int from, final int to, final Set<FeatureType> featureTypes, final int chromId ) {

        List<PersistentFeature> features = new ArrayList<>();
        for( FeatureType featureType : featureTypes ) {
            features.addAll( getFeaturesForRegionInclParents( from, to, featureType, chromId ) );
        }
        return features;

    }


    /**
     * Fetches all features which are completely located within a given region
     * of the reference.
     *
     * @param left    start position of the region of interest
     * @param right   end position of the region of interest
     * @param chromId chromosome id of the features of interest
     * <p>
     * @return the list of all features found in the interval of interest
     */
    public List<PersistentFeature> getFeaturesForClosedInterval( final int left, final int right, final int chromId ) {

        List<PersistentFeature> features = new ArrayList<>();
        try( final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_FEATURES_FOR_CLOSED_GENOME_INTERVAL ) ) {

            fetch.setInt( 1, chromId );
            fetch.setInt( 2, left );
            fetch.setInt( 3, right );
            fetch.setInt( 4, left );
            fetch.setInt( 5, right );

            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.FEATURE_ID );
                    String parentIds = rs.getString( FieldNames.FEATURE_PARENT_IDS );
                    String ecnum = rs.getString( FieldNames.FEATURE_EC_NUM );
                    String locus = rs.getString( FieldNames.FEATURE_LOCUS_TAG );
                    String product = rs.getString( FieldNames.FEATURE_PRODUCT );
                    int start = rs.getInt( FieldNames.FEATURE_START );
                    int stop = rs.getInt( FieldNames.FEATURE_STOP );
                    boolean isFwdStrand = rs.getInt( FieldNames.FEATURE_STRAND ) == Strand.Forward.getType();
                    FeatureType type = FeatureType.getFeatureType( rs.getInt( FieldNames.FEATURE_TYPE ) );
                    String gene = rs.getString( FieldNames.FEATURE_GENE );

                    features.add( new PersistentFeature( id, chromId, parentIds, ecnum, locus, product, start, stop, isFwdStrand, type, gene ) );
                }
            }

        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return features;

    }


    /**
     * @return the tracks associated to this reference connector.
     */
    public List<PersistentTrack> getAssociatedTracks() {

        try( final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_TRACKS_FOR_GENOME ) ) {
            associatedTracks.clear();
            fetch.setLong( 1, refGenID );

            try( final ResultSet rs = fetch.executeQuery() ) {
                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.TRACK_ID );
                    String description = rs.getString( FieldNames.TRACK_DESCRIPTION );
                    Timestamp date = rs.getTimestamp( FieldNames.TRACK_TIMESTAMP );
                    int refGenomeID = rs.getInt( FieldNames.TRACK_REFERENCE_ID );
                    String filePath = rs.getString( FieldNames.TRACK_PATH );
                    int readPairId = rs.getInt( FieldNames.TRACK_READ_PAIR_ID );
                    associatedTracks.add( new PersistentTrack( id, filePath, description, date, refGenomeID, readPairId ) );
                }
            }
        } catch( SQLException ex ) {
            LOG.log( Level.SEVERE, null, ex );
        }

        return Collections.unmodifiableList( associatedTracks );

    }


    /**
     * Calculates and returns the names of all tracks belonging to this
     * reference hashed to their track id.
     *
     * @return the names of all tracks of this reference hashed to their track
     *         id.
     */
    public Map<Integer, String> getAssociatedTrackNames() {

        getAssociatedTracks(); //ensures the tracks are already in the list

        Map<Integer, String> namesList = new HashMap<>( associatedTracks.size() );
        for( PersistentTrack track : associatedTracks ) {
            namesList.put( track.getId(), track.getDescription() );
        }
        return namesList;

    }


    /**
     * Checks if this reference genome has at least one feature of any of the
     * given types.
     *
     * @param typeList the feature type list to check
     * <p>
     * @return true, if this reference genome has at least one feature of the
     *         given type, false otherwise
     */
    public boolean hasFeatures( final List<FeatureType> typeList ) {

        for( FeatureType featureType : typeList ) {
            if( hasFeatures( featureType ) ) {
                return true;
            }
        }
        return false;

    }


    /**
     * Checks if this reference genome has at least one feature of the given
     * type.
     *
     * @param type the feature type to check
     * <p>
     * @return true, if this reference genome has at least one feature of the
     *         given type, false otherwise
     */
    public boolean hasFeatures( final FeatureType type ) {

        for( final PersistentChromosome chromosome : getChromosomesForGenome().values() ) {
            int currentID = chromosome.getId();
            try {
                final PreparedStatement fetch;
                if( type == FeatureType.ANY ) {
                    fetch = con.prepareStatement( SQLStatements.CHECK_IF_FEATURES_EXIST );
                    fetch.setLong( 1, currentID );
                } else {
                    fetch = con.prepareStatement( SQLStatements.CHECK_IF_FEATURES_OF_TYPE_EXIST );
                    fetch.setLong( 1, currentID );
                    fetch.setLong( 2, type.getType() );
                }
                ResultSet rs = fetch.executeQuery();
                if( rs.next() ) {
                    //If at least one entry exists we can exit early.
                    rs.close();
                    return true;
                }
            } catch( SQLException ex ) {
                LOG.log( Level.SEVERE, null, ex );
                return false;
            }
        }
        //Tried all chromosomes, no entry found
        return false;

    }


}
