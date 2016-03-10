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
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The reference genome connector is responsible for the connection to a
 * reference genome.
 *
 * @author ddoppmeier, rhilker
 */
public class ReferenceConnector {

    private static final Logger LOG = LoggerFactory.getLogger( ReferenceConnector.class.getName() );

    private final int refId;
    private final List<PersistentTrack> associatedTracks;


    /**
     * The reference genome connector is responsible for the connection to a
     * reference genome.
     * <p>
     * @param refGenID id of the associated reference genome
     */
    ReferenceConnector( int refGenID ) {

        this.refId = refGenID;
        this.associatedTracks = new ArrayList<>();

    }


    /**
     * @return Fetches the reference genome of the reference associated with
     *         this connector. If it was called once, it is kept in memory and
     *         does not need to be fetched from the DB again.
     *
     * @throws DatabaseException An exception during data queries. It has
     *                           already been logged.
     */
    public PersistentReference getRefGenome() throws DatabaseException {

        LOG.info( "Loading reference genome with id " + refId + " from database" );

        try( Connection con = ProjectConnector.getInstance().getConnection();
             final PreparedStatement pStmtFetch = con.prepareStatement( SQLStatements.FETCH_SINGLE_GENOME ) ) {
            pStmtFetch.setLong( 1, refId );

            try( final ResultSet rs = pStmtFetch.executeQuery() ) {
                if( rs.next() ) {
                    String name = rs.getString( FieldNames.REF_GEN_NAME );
                    String description = rs.getString( FieldNames.REF_GEN_DESCRIPTION );
                    Timestamp time = rs.getTimestamp( FieldNames.REF_GEN_TIMESTAMP );
                    File fastaFile = new File( rs.getString( FieldNames.REF_GEN_FASTA_FILE ) );
                    return new PersistentReference( refId, name, description, time, fastaFile );
                } else {
                    return null;
                }
            }

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            throw new DatabaseException( "Could not fetch reference!", ex.getMessage(), ex );
        }

    }


    /**
     * @return All chromosomes of this reference without their sequence.
     */
    public Map<Integer, PersistentChromosome> getChromosomesForGenome() {

        LOG.info( "Loading chromosomes for reference with id " + refId + " from database", refId );
        Map<Integer, PersistentChromosome> chromosomes = new HashMap<>();
        try( Connection con = ProjectConnector.getInstance().getConnection();
             final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_CHROMOSOMES ) ) {
            fetch.setLong( 1, refId );
            try( final ResultSet rs = fetch.executeQuery() ) {

                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.CHROM_ID );
                    int chromNumber = rs.getInt( FieldNames.CHROM_NUMBER );
                    String name = rs.getString( FieldNames.CHROM_NAME );
                    int chromLength = rs.getInt( FieldNames.CHROM_LENGTH );

                    chromosomes.put( id, new PersistentChromosome( id, chromNumber, refId, name, chromLength ) );
                }
            }

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch chromosomes!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
        }
        return chromosomes;
    }


    /**
     * @param chromId the id of the chromosome to fetch
     * <p>
     * @return One chromosome of this reference without its sequence.
     *
     * @throws DatabaseException An exception during data queries. An exception
     *                           during data queries. It has already been
     *                           logged.
     */
    public PersistentChromosome getChromosomeForGenome( final int chromId ) throws DatabaseException {

        LOG.info( "Loading chromosome for reference with id " + refId + " from database", refId );
        try( Connection con = ProjectConnector.getInstance().getConnection();
             final PreparedStatement fetch = con.prepareStatement( SQLStatements.FETCH_CHROMOSOME ) ) {

            fetch.setLong( 1, chromId );
            try( final ResultSet rs = fetch.executeQuery() ) {
                if( rs.next() ) {
                    int chromNumber = rs.getInt( FieldNames.CHROM_NUMBER );
                    int chromLength = rs.getInt( FieldNames.CHROM_LENGTH );
                    String name = rs.getString( FieldNames.CHROM_NAME );
                    return new PersistentChromosome( chromId, chromNumber, refId, name, chromLength );
                } else {
                    LOG.error( "Could not find chromosome in database!" );
                    throw new DatabaseException( "Could not find chromosome in database!", new RuntimeException() );
                }
            }

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            throw new DatabaseException( "Could not fetch chromosome!", ex.getMessage(), ex );
        }
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
        try( Connection con = ProjectConnector.getInstance().getConnection() ) {
            final PreparedStatement pStmtFetchFeatures;
            if( featureType == FeatureType.ANY ) {
                pStmtFetchFeatures = con.prepareStatement( SQLStatements.FETCH_FEATURES_FOR_CHROM_INTERVAL );
                pStmtFetchFeatures.setLong( 1, chromId );
                pStmtFetchFeatures.setInt( 2, from );
                pStmtFetchFeatures.setInt( 3, to );
            } else {
                pStmtFetchFeatures = con.prepareStatement( SQLStatements.FETCH_SPECIFIED_FEATURES_FOR_CHROM_INTERVAL );
                pStmtFetchFeatures.setLong( 1, chromId );
                pStmtFetchFeatures.setInt( 2, from );
                pStmtFetchFeatures.setInt( 3, to );
                pStmtFetchFeatures.setInt( 4, featureType.getType() );
            }

            try( final ResultSet rs = pStmtFetchFeatures.executeQuery() ) {
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
            pStmtFetchFeatures.close();

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch features for reagion!" );
        } catch( DatabaseException ex ) {
            ErrorHelper.getHandler().handle( ex );
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
    public List<PersistentFeature> getFeaturesForRegionInclParents( final int from, final int to,
                                                                    final FeatureType featureType,
                                                                    final int chromId ) {

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
    public List<PersistentFeature> getFeaturesForRegionInclParents( final int from, final int to,
                                                                    final Set<FeatureType> featureTypes,
                                                                    final int chromId ) {

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
        try( Connection con = ProjectConnector.getInstance().getConnection();
             PreparedStatement pStmtFetchFeatures = con.prepareStatement( SQLStatements.FETCH_FEATURES_FOR_CLOSED_GENOME_INTERVAL ) ) {

            pStmtFetchFeatures.setInt( 1, chromId );
            pStmtFetchFeatures.setInt( 2, left );
            pStmtFetchFeatures.setInt( 3, right );
            pStmtFetchFeatures.setInt( 4, left );
            pStmtFetchFeatures.setInt( 5, right );

            try( final ResultSet rs = pStmtFetchFeatures.executeQuery() ) {
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
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch features for closed interval!" );
        } catch( DatabaseException e ) {
            ErrorHelper.getHandler().handle( e );
        }

        return features;
    }


    /**
     * @return the tracks associated to this reference connector.
     */
    public List<PersistentTrack> getAssociatedTracks() {

        try( Connection con = ProjectConnector.getInstance().getConnection();
             PreparedStatement pStmtFetch = con.prepareStatement( SQLStatements.FETCH_TRACKS_FOR_GENOME ) ) {

            List<PersistentTrack> tmpAssociatedTracks = new ArrayList<>();
            pStmtFetch.setLong( 1, refId );
            try( final ResultSet rs = pStmtFetch.executeQuery() ) {
                while( rs.next() ) {
                    int id = rs.getInt( FieldNames.TRACK_ID );
                    String description = rs.getString( FieldNames.TRACK_DESCRIPTION );
                    Timestamp date = rs.getTimestamp( FieldNames.TRACK_TIMESTAMP );
                    int refGenomeID = rs.getInt( FieldNames.TRACK_REFERENCE_ID );
                    String filePath = rs.getString( FieldNames.TRACK_PATH );
                    int readPairId = rs.getInt( FieldNames.TRACK_READ_PAIR_ID );
                    tmpAssociatedTracks.add( new PersistentTrack( id, filePath, description, date, refGenomeID, readPairId ) );
                }
            }

            // if everything is ok, copy new tracks
            associatedTracks.clear();
            associatedTracks.addAll( tmpAssociatedTracks );

        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Could not fetch tracks from the database!" );
        } catch( DatabaseException e ) {
            ErrorHelper.getHandler().handle( e );
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

        try( Connection con = ProjectConnector.getInstance().getConnection() ) {
            for( PersistentChromosome chromosome : getChromosomesForGenome().values() ) {

                int chromId = chromosome.getId();
                final PreparedStatement pStmtFetch;
                if( type == FeatureType.ANY ) {
                    pStmtFetch = con.prepareStatement( SQLStatements.CHECK_IF_FEATURES_EXIST );
                    pStmtFetch.setLong( 1, chromId );
                } else {
                    pStmtFetch = con.prepareStatement( SQLStatements.CHECK_IF_FEATURES_OF_TYPE_EXIST );
                    pStmtFetch.setLong( 1, chromId );
                    pStmtFetch.setLong( 2, type.getType() );
                }
                try( ResultSet rs = pStmtFetch.executeQuery() ) {
                    boolean hasFeature = rs.next();
                    pStmtFetch.close();
                    if( hasFeature ) { // if at least one entry exists we can exit early.
                        return true;
                    }
                }
            }
        } catch( SQLException ex ) {
            LOG.error( ex.getMessage(), ex );
            ErrorHelper.getHandler().handle( ex, "Check for feature has failed!" );
        } catch( DatabaseException e ) {
            ErrorHelper.getHandler().handle( e );
        }

        //Tried all chromosomes, no entry found or exception occurred
        return false;
    }


}
