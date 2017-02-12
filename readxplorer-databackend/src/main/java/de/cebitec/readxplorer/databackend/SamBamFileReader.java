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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.enums.MappingClass;
import de.cebitec.readxplorer.api.enums.ReadPairType;
import de.cebitec.readxplorer.api.enums.SAMRecordTag;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageAndDiffResult;
import de.cebitec.readxplorer.databackend.dataobjects.CoverageManager;
import de.cebitec.readxplorer.databackend.dataobjects.DiffAndGapResult;
import de.cebitec.readxplorer.databackend.dataobjects.Difference;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.ReadPairGroup;
import de.cebitec.readxplorer.databackend.dataobjects.ReferenceGap;
import de.cebitec.readxplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readxplorer.utils.IndexFileNotificationPanel;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.SamAlignmentBlock;
import de.cebitec.readxplorer.utils.SamUtils;
import de.cebitec.readxplorer.utils.SequenceUtils;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.RuntimeIOException;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static htsjdk.samtools.ValidationStringency.LENIENT;


/**
 * A SamBamFileReader has different methods to read data from a bam or sam file.
 * <p>
 * @author -Rolf Hilker- <rhilker@cebitec.uni-bielefeld.de>
 */
public class SamBamFileReader implements Observable, Observer {

    private static final Logger LOG = LoggerFactory.getLogger( SamBamFileReader.class.getName() );

    /**
     * 0 = Default mapping quality value, if it is unknown.
     */
    public static final int UNKNOWN_MAP_QUAL = 0;
    /**
     * 255 = Default mapping quality value according to SAM spec, if it was not
     * calculated.
     */
    public static final int DEFAULT_MAP_QUAL = 255;
    /**
     * -1 = Replacement of 255 from SAM spec. Enables us to use byte.
     */
    public static final int UNKNOWN_CALCULATED_MAP_QUAL = -1;

    private final File dataFile;
    private final int trackId;
    private final PersistentReference reference;
    private final SamUtils samUtils;
    private SamReader samFileReader;
    private final List<Observer> observers;


    /**
     * A SamBamFileReader has different methods to read data from a bam or sam
     * file.
     * <p>
     * @param dataFile  the file to read from
     * @param trackId   the track id of the track whose data is stored in the
     *                  given file
     * @param reference reference genome used in the bam file
     * <p>
     * @throws RuntimeIOException
     */
    public SamBamFileReader( File dataFile, int trackId, PersistentReference reference ) throws RuntimeIOException {
        this.observers = new ArrayList<>();
        this.dataFile = dataFile;
        this.trackId = trackId;
        this.reference = reference;
        this.samUtils = new SamUtils();

        initializeReader();
    }


    /**
     * Initializes or re-initializes the bam file reader.
     */
    private void initializeReader() {
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        samFileReader = samReaderFactory.open( dataFile );
//        header = samFileReader.getFileHeader().getTextHeader();
        checkIndex();
    }


    /**
     * Checks if the index of the bam file is present or creates it. If it needs
     * to be created, the gui is blocked by a dialog, which waits for the finish
     * signal of the index creation.
     * <p>
     * @return true, if the index already exists, false otherwise
     */
    private void checkIndex() {
        File indexFile = new File( dataFile.getAbsolutePath() + ".bai" );
        if( !samFileReader.hasIndex() && !indexFile.exists() ) { //first time after index creation the hasIndex method does not recognize the new index file
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle( "BAM index missing, recreating it..." );
            progressHandle.start();

            final IndexFileNotificationPanel indexPanel = new IndexFileNotificationPanel();
            final JButton okButton = new JButton( "OK" );
            DialogDescriptor dialogDescriptor = new DialogDescriptor( indexPanel, "BAM index missing!", true, new JButton[]{ okButton }, okButton, DialogDescriptor.DEFAULT_ALIGN, null, null );
            Thread indexThread = new Thread( new Runnable() {

                @Override
                public void run() {
                    boolean success = SamUtils.createBamIndex( dataFile, SamBamFileReader.this );
                    if( !success ) {
                        dialogDescriptor.setMessage( "Bam index creation failed! You can close the dialog and check the file permissions in the folder of the bam file." );
                    }
                    progressHandle.finish();
                    okButton.setEnabled( true );
                }


            } );
            indexThread.start();
            Dialog indexDialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
            okButton.setEnabled( false );
            indexDialog.setVisible( true );
        }
    }


    /**
     * Checks if the reference id exists in the mapping file header
     *
     * @param request The request to send to the mapping file
     *
     * @return <code>true</code> if the reference id exists in the mapping file
     *         sequence dictionary, <code>false</code> otherwise
     */
    private boolean checkRefExists( IntervalRequest request ) {
        String chrom = reference.getChromosome( request.getChromId() ).getName();
        SAMSequenceRecord chromRecord = samFileReader.getFileHeader().getSequence( chrom );
        return chromRecord != null;
    }


    /**
     * Retrieves the mappings from the given interval from the sam or bam file
     * set for this data reader and the reference sequence with the given name.
     * <p>
     * @param request the request to carry out
     * <p>
     * @return the mappings for the given interval
     */
    public Collection<Mapping> getMappingsFromBam( IntervalRequest request ) {

        Collection<Mapping> mappings = new ArrayList<>();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        try {
            checkIndex();

            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome(
                        request.getChromId() ).getName(), request.getTotalFrom(), request.getTotalTo(), false );
                String refSubSeq;
                int id = 0;
//            int numReplicates = 1;

                while( samRecordIterator.hasNext() ) {
                    SAMRecord record = samRecordIterator.next();

                    if( !record.getReadUnmappedFlag() ) {
                        int start = record.getAlignmentStart();
                        int stop = record.getAlignmentEnd();
//            start = start < 0 ? 0 : start;
//            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                        boolean isFwdStrand = !record.getReadNegativeStrandFlag();
                        byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        //only add mappings, which are valid according to the read classification parameters
                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {

                            Mapping mapping = getMappingForValues( mappingClass, numMappingsForRead, id++,
                                                                   start, stop, isFwdStrand, mappingQuality, record.getBaseQualities() );
                            mapping.setAlignmentBlocks( samUtils.getAlignmentBlocks( record.getCigar(), start ) );
                            // We must alway check for Diffs and Gaps even if "classification != MappingClass.PERFECT_MATCH"
                            // because there might still be a split read.
                            if( hasNeededDiffs( request, mappingClass ) ) {

                                //find check alignment via cigar string and add diffs to mapping
                                String cigar = record.getCigarString();
                                if( cigar.contains( "M" ) ) { //TODO check if this check is faster or the version in other methods here
                                    refSubSeq = reference.getChromSequence( request.getChromId(), start, stop );
                                } else {
                                    refSubSeq = null;
                                }
                                createDiffsAndGaps( record, refSubSeq, mapping );
                            }

                            //stuff for trimmed reads
                            Object originalSequence = record.getAttribute( "os" );
                            if( (originalSequence != null) && (originalSequence instanceof String) ) {
                                String ors = (String) originalSequence;
                                ors = ors.replace( "@", record.getReadString() );
                                mapping.setOriginalSequence( ors );
                            }
                            Object trimmedFromLeft = record.getIntegerAttribute( "tl" );
                            if( (trimmedFromLeft != null) && (trimmedFromLeft instanceof Integer) ) {
                                mapping.setTrimmedFromLeft( (Integer) trimmedFromLeft );
                            }
                            Object trimmedFromRight = record.getIntegerAttribute( "tr" );
                            if( (trimmedFromRight != null) && (trimmedFromRight instanceof Integer) ) {
                                mapping.setTrimmedFromRight( (Integer) trimmedFromRight );
                            }

                            mappings.add( mapping );
                        }
                    }

                }
                samRecordIterator.close();
            }

        } catch( NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }

        return mappings;
    }


    /**
     * Checks if diffs and gaps are needed and if the mapping contains some.
     * <p>
     * @param request
     * @param type    <p>
     * @return true, if diffs and gaps are needed and the mapping contains some,
     *         false otherwise
     */
    private boolean hasNeededDiffs( IntervalRequest request, MappingClass type ) {
        return request.isDiffsAndGapsNeeded() &&
               type != MappingClass.SINGLE_PERFECT_MATCH &&
               type != MappingClass.PERFECT_MATCH;
    }


    /**
     * Retrieves the reduced mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name. Diffs and gaps are never included.
     * <p>
     * @param request the request to carry out
     * <p>
     * @return the reduced mappings for the given interval. Diffs and gaps are
     *         never included.
     */
    public Collection<Mapping> getReducedMappingsFromBam( final IntervalRequest request ) {
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();
        Collection<Mapping> mappings = new ArrayList<>();

        try {
            checkIndex();

            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome( request.getChromId() ).getName(), from, to, false );
                while( samRecordIterator.hasNext() ) {
                    final SAMRecord record = samRecordIterator.next();
                    if( !record.getReadUnmappedFlag() ) {
                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        //only add mappings, which are valid according to the read classification paramters
                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {

                            int start = record.getAlignmentStart();
                            int stop = record.getAlignmentEnd();
                            boolean isFwdStrand = !record.getReadNegativeStrandFlag();
                            mappings.add( new Mapping( start, stop, isFwdStrand ) );
                        }
                    }
                }
                samRecordIterator.close();

            }
        } catch( NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }

        return mappings;
    }


    /**
     * Retrieves the read pair mappings from the given interval from the sam or
     * bam file set for this data reader and the reference sequence with the
     * given name.
     * <p>
     * @param request request to carry out
     * <p>
     * @return the coverage for the given interval
     */
    public Collection<ReadPairGroup> getReadPairMappingsFromBam( final IntervalRequest request ) {
        Map<Long, ReadPairGroup> readPairs = new HashMap<>();
        Collection<ReadPairGroup> readPairGroups = new ArrayList<>();

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        try {
            checkIndex();

            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome( request.getChromId() ).getName(), from, to, false );
                int id = 0;
                while( samRecordIterator.hasNext() ) {
                    final SAMRecord record = samRecordIterator.next();

                    if( !record.getReadUnmappedFlag() ) {
                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {

                            int startPos = record.getAlignmentStart(); //in the genome, to get the index: -1
                            int stop = record.getAlignmentEnd();
                            //            start = start < 0 ? 0 : start;
                            //            stop = stop >= refSeq.length() ? refSeq.length() : stop;
                            boolean isFwdStrand = !record.getReadNegativeStrandFlag();
                            Integer pairId = (Integer) record.getAttribute( SAMRecordTag.ReadPairId.toString() );
                            Integer pairType = (Integer) record.getAttribute( SAMRecordTag.ReadPairType.toString() );
                            int mateStart = record.getMateAlignmentStart();
                            boolean bothVisible = mateStart > from && mateStart < to;

                            Mapping mapping = getMappingForValues( mappingClass, numMappingsForRead, id++,
                                                                   startPos, stop, isFwdStrand, mappingQuality, record.getBaseQualities() );
                            mapping.setAlignmentBlocks( samUtils.getAlignmentBlocks( record.getCigar(), startPos ) );
                            if( pairId != null && pairType != null ) { //since both data fields are always written together
                                // add new readPair if not exists
                                long readPairId = pairId;
                                ReadPairType readPairType = ReadPairType.getReadPairType( pairType );
                                if( !readPairs.containsKey( readPairId ) ) {
                                    ReadPairGroup newGroup = new ReadPairGroup();
                                    newGroup.setReadPairId( pairId );
                                    readPairs.put( readPairId, newGroup );
                                } //TODO check where ids are needed
                                Mapping mate;
                                try {
                                    mate = getMappingForValues( MappingClass.COMMON_MATCH, -1, -1, mateStart, -1, !record.getMateNegativeStrandFlag(), new Byte( "0" ), new byte[0] );
                                } catch( IllegalStateException e ) {
                                    mate = getMappingForValues( MappingClass.COMMON_MATCH, -1, -1, mateStart, -1, true, new Byte( "0" ), new byte[0] );
                                } //TODO get mate data from querried records later
                                readPairs.get( readPairId ).addPersistentDirectAccessMapping( mapping, mate, readPairType, bothVisible );
                            }

                            if( hasNeededDiffs( request, mappingClass ) ) {

                                //check alignment via cigar string and add diffs to mapping
                                String cigar = record.getCigarString();
                                String refSubSeq = cigar.contains( "M" ) ? reference.getChromSequence( request.getChromId(), startPos, stop ) : null;
                                createDiffsAndGaps( record, refSubSeq, mapping );
                            }
                        }
                    }
                }
                samRecordIterator.close();
                readPairGroups = readPairs.values();

            }
        } catch( NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }

        return readPairGroups;
    }


    /**
     * Creates a mapping for the given classification and mapping data.
     * <p>
     * @param classification the classification data
     * @param id             unique id of the mapping
     * @param startPos       start position of the mapping
     * @param stop           stop position of the mapping
     * @param isFwdStrand    true, if the mapping is on the fwd strand
     * <p>
     * @return A new mapping with classification information, if classification
     *         is not null. Otherwise isBestMapping is currently always true.
     */
    private Mapping getMappingForValues( MappingClass classification, Integer numMappingsForRead, int id,
                                         int startPos, int stop, boolean isFwdStrand, int mappingQuality, byte[] baseQualities ) {

        int mappingsForRead = numMappingsForRead != null ? numMappingsForRead : -1;
        return new Mapping( id, startPos, stop, trackId, isFwdStrand, 0, 0,
                            classification, mappingQuality, baseQualities, mappingsForRead );
    }


    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * <p>
     * @param request the request to carry out
     * <p>
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResult getReadStartsFromBam( final IntervalRequest request ) {

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        CoverageManager coverage = new CoverageManager( from, to );
        coverage.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>(); //both empty for read starts
        List<ReferenceGap> gaps = new ArrayList<>();

        CoverageAndDiffResult result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
        try {
            checkIndex();
            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome( request.getChromId() ).getName(), from, to, false );
                while( samRecordIterator.hasNext() ) {
                    SAMRecord record = samRecordIterator.next();

                    if( !record.getReadUnmappedFlag() ) {
                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {

                            boolean isFwdStrand;
                            if( readClassParams.isStrandBothOption() ) {
                                isFwdStrand = readClassParams.isStrandBothFwdOption();
                            } else {
                                isFwdStrand = !record.getReadNegativeStrandFlag();
                            }
                            int startPos = isFwdStrand ? record.getAlignmentStart() : record.getAlignmentEnd(); //in the genome, to get the index: -1

                            increaseCoverage( mappingClass, isFwdStrand,
                                              startPos, startPos, coverage );
                        }
                    }
                }
                samRecordIterator.close();
                result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
            }
        } catch( NullPointerException | IllegalArgumentException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }

        return result;
    }


    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * <p>
     * @param request the request to carry out
     * <p>
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResult getCoverageAndReadStartsFromBam( IntervalRequest request ) {

        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        CoverageManager coverage = new CoverageManager( from, to );
        CoverageManager readStarts = new CoverageManager( from, to );
        coverage.incArraysToIntervalSize();
        readStarts.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>();
        List<ReferenceGap> gaps = new ArrayList<>();
        CoverageAndDiffResult result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
        try {
            checkIndex();

            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome( request.getChromId() ).getName(), from, to, false );
                while( samRecordIterator.hasNext() ) {
                    SAMRecord record = samRecordIterator.next();

                    if( !record.getReadUnmappedFlag() ) {
                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {

                            boolean isFwdStrand;
                            if( readClassParams.isStrandBothOption() ) {
                                isFwdStrand = readClassParams.isStrandBothFwdOption();
                            } else if( readClassParams.isStrandOppositeOption() ) {
                                isFwdStrand = record.getReadNegativeStrandFlag();
                            } else {
                                isFwdStrand = !record.getReadNegativeStrandFlag();
                            }

                            // add read start
                            int startPos = isFwdStrand ? record.getAlignmentStart() : record.getAlignmentEnd(); //in the genome, to get the index: -1
                            if( readClassParams.isStrandBothOption() ) {
                                isFwdStrand = readClassParams.isStrandBothFwdOption();
                            } else {
                                isFwdStrand = !record.getReadNegativeStrandFlag();
                            }

                            increaseCoverage( mappingClass, isFwdStrand, startPos, startPos, readStarts );

                            //This enables us to handle split reads correctly.
                            startPos = record.getAlignmentStart();
                            isFwdStrand = !record.getReadNegativeStrandFlag();
                            List<SamAlignmentBlock> alignmentBlocks = samUtils.getAlignmentBlocks( record.getCigar(), startPos );
                            for( SamAlignmentBlock block : alignmentBlocks ) {
                                increaseCoverage( mappingClass, isFwdStrand,
                                                  block.getRefStart(), block.getRefStop(), coverage );
                            }

                            if( hasNeededDiffs( request, mappingClass ) ) {
                                int stop = record.getAlignmentEnd();
                                DiffAndGapResult diffsAndGaps = createDiffsAndGaps( record,
                                                                                    reference.getChromSequence( request.getChromId(), startPos, stop ), null );
                                diffs.addAll( diffsAndGaps.getDiffs() );
                                gaps.addAll( diffsAndGaps.getGaps() );
                            }
                        }
                    }
                }
                samRecordIterator.close();
                result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
                result.setReadStarts( readStarts );

            }
        } catch( NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }

        return result;
    }


    /**
     * Retrieves the coverage for the given interval from the bam file set for
     * this data reader and the reference sequence with the given name. If reads
     * become longer than 1000bp the offset in this method has to be enlarged!
     * <p>
     * @param request the request to carry out
     * <p>
     * @return the coverage for the given interval
     */
    public CoverageAndDiffResult getCoverageFromBam( IntervalRequest request ) {
//        startTime = System.currentTimeMillis();
        int from = request.getTotalFrom();
        int to = request.getTotalTo();
        ParametersReadClasses readClassParams = request.getReadClassParams();

        CoverageManager coverage = new CoverageManager( from, to );
        coverage.incArraysToIntervalSize();

        List<Difference> diffs = new ArrayList<>();
        List<ReferenceGap> gaps = new ArrayList<>();
        CoverageAndDiffResult result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
        try {
            checkIndex();
            
            if( checkRefExists( request ) ) {
                SAMRecordIterator samRecordIterator = samFileReader.query( reference.getChromosome( request.getChromId() ).getName(), from, to, false );
                while( samRecordIterator.hasNext() ) {
                    SAMRecord record = samRecordIterator.next();

                    if( !record.getReadUnmappedFlag() ) {
                        Byte classification = Byte.valueOf( record.getAttribute( SAMRecordTag.ReadClass.toString() ).toString() );
                        MappingClass mappingClass = MappingClass.getFeatureType( classification );
                        Integer numMappingsForRead = (Integer) record.getAttribute( SAMRecordTag.MapCount.toString() );
                        int mappingQuality = record.getMappingQuality();

                        if( isIncludedMapping( mappingClass, numMappingsForRead, mappingQuality, readClassParams ) ) {
                            boolean isFwdStrand = !record.getReadNegativeStrandFlag();
                            //This enables us to handle split reads correctly.
                            int startPos = record.getAlignmentStart(); //in the genome, to get the index: -1
                            List<SamAlignmentBlock> alignmentBlocks = samUtils.getAlignmentBlocks( record.getCigar(), startPos );
                            for( SamAlignmentBlock block : alignmentBlocks ) {
                                increaseCoverage( mappingClass, isFwdStrand,
                                                  block.getRefStart(), block.getRefStop(), coverage );
                            }

                            if( hasNeededDiffs( request, mappingClass ) ) {
                                int stop = record.getAlignmentEnd();
                                DiffAndGapResult diffsAndGaps = createDiffsAndGaps( record,
                                                                                    reference.getChromSequence( request.getChromId(), startPos, stop ), null );
                                diffs.addAll( diffsAndGaps.getDiffs() );
                                gaps.addAll( diffsAndGaps.getGaps() );
                            }
                        }
                    }
                }
                samRecordIterator.close();
                result = new CoverageAndDiffResult( coverage, diffs, gaps, request );
            }

        } catch( NullPointerException | NumberFormatException | SAMException | ArrayIndexOutOfBoundsException e ) {
            notifyObservers( e );
        } catch( BufferUnderflowException e ) {
            //do nothing
            LOG.trace( e.getMessage(), e );
        }
//        finish = System.currentTimeMillis(); //for performance testing
//        System.out.println(Benchmark.calculateDuration(startTime, finish, "get coverage "));
//        System.out.println("start: " + request.getTotalFrom() + ", stop: " + request.getTotalTo());
//        startTime = System.currentTimeMillis();

        return result;
    }


    /**
     * Increases the coverage between the given start and stop position in the
     * given coverageManager for the given mapping class and strand.
     * <p>
     * @param mappingClass mapping class of the current mapping
     * @param isFwdStrand  true, if the mapping is on the fwd strand, false
     *                     otherwise
     * @param startPos     start position of the mapping
     * @param stopPos      stop position of the mapping
     * @param covManager   The coverage manager containing the coverage to
     *                     increase
     */
    private void increaseCoverage( MappingClass mappingClass, boolean isFwdStrand,
                                   int startPos, int stopPos, CoverageManager covManager ) {

        int[] coverageArray = covManager.getCoverage( mappingClass ).getCoverage( isFwdStrand );
        covManager.increaseCoverage( startPos, stopPos, coverageArray );
    }


    /**
     * Counts and returns each difference to the reference sequence for a cigar
     * string and the belonging read sequence. If the operation "M" is not used
     * in the cigar, then the reference sequence can be null (it is not used in
     * this case). If the mapping is also handed over to the method, the diffs
     * and gaps are stored directly in the mapping.
     * <p>
     * @param cigar   the cigar string containing the alignment operations
     * @param start   the start position of the alignment on the chromosome
     * @param readSeq the read sequence belonging to the cigar and without gaps
     * @param refSeq  the reference sequence belonging to the cigar and without
     *                gaps in upper case characters
     * @param mapping if a mapping is handed over to the method it adds the
     *                diffs and gaps directly to the mapping and updates it's
     *                number of differences to the reference. If null is passed,
     *                only the DiffAndGapResult contains all the diff and gap
     *                data.
     * <p>
     * @return DiffAndGapResult containing all the diffs and gaps
     */
    private DiffAndGapResult createDiffsAndGaps( SAMRecord record, String refSeq,
                                                 Mapping mapping ) throws NumberFormatException {

        final Map<Integer, Integer> gapOrderIndex = new HashMap<>();
        final List<Difference> diffs = new ArrayList<>();
        final List<ReferenceGap> gaps = new ArrayList<>();
        int differences = 0;
        final String cigar = record.getCigarString();
        final String readSeq = record.getReadString();
        final boolean isFwdStrand = !record.getReadNegativeStrandFlag();
        final int start = record.getAlignmentStart();
        final byte[] baseQualities = record.getBaseQualities();
        final Byte mappingQuality = (byte) (record.getMappingQuality() >= DEFAULT_MAP_QUAL ? UNKNOWN_CALCULATED_MAP_QUAL : record.getMappingQuality());
        final String[] num = CommonsMappingParser.CIGAR_PATTERN.split( cigar );
        final String[] charCigar = CommonsMappingParser.DIGIT_PATTERN.split( cigar );
        int refPos = 0;
        int readPos = 0;
        for( int i = 1; i < charCigar.length; i++ ) {
            int currentCount = Integer.valueOf( num[i - 1] );
            switch( charCigar[i] ) {
                case "M":
                    //check, count and add diffs for deviating Ms
                    String bases = readSeq.substring( readPos, readPos + currentCount ).toUpperCase(); //bases of the read interval under investigation
                    String refBases = refSeq.substring( refPos, refPos + currentCount ); //bases of the reference corresponding to the read interval under investigation
                    for( int j = 0; j < bases.length(); j++ ) {
                        char base = bases.charAt( j ); //currently visited base
                        if( base != refBases.charAt( j ) ) {
                            differences++;
                            if( !isFwdStrand ) {
                                base = SequenceUtils.getDnaComplement( base );
                            }
                            byte baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                            Difference d = new Difference( refPos + j + start, base, isFwdStrand, 1, baseQuality, mappingQuality );
                            addDiff( mapping, diffs, d );
                        }
                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "=":
                    //only increase position for matches
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "X":
                    //count and create diffs for mismatches
                    differences += currentCount;
                    for( int j = 0; j < currentCount; j++ ) {
                        char base = Character.toUpperCase( readSeq.charAt( readPos + j ) );
                        if( !isFwdStrand ) {
                            base = SequenceUtils.getDnaComplement( base );
                        }
                        byte baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        Difference d = new Difference( refPos + j + start, base, isFwdStrand, 1, baseQuality, mappingQuality );
                        addDiff( mapping, diffs, d );

                    }
                    refPos += currentCount;
                    readPos += currentCount;
                    break;
                case "D":
                    // count and add diff gaps for deletions in reference
                    differences += currentCount;
                    for( int j = 0; j < currentCount; j++ ) {
                        byte baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        Difference d = new Difference( refPos + j + start, '-', isFwdStrand, 1, baseQuality, mappingQuality );
                        addDiff( mapping, diffs, d );
                    }
                    refPos += currentCount;
                    // readPos remains the same
                    break;
                case "I":
                    // count and add reference gaps for insertions
                    differences += currentCount;
                    for( int j = 0; j < currentCount; j++ ) {
                        char base = Character.toUpperCase( readSeq.charAt( readPos + j ) );
                        if( !isFwdStrand ) {
                            base = SequenceUtils.getDnaComplement( base );
                        }
                        byte baseQuality = baseQualities.length == 0 ? -1 : baseQualities[j];
                        ReferenceGap gap = new ReferenceGap( refPos + start, base,
                                                             CommonsMappingParser.getOrderForGap( refPos + start, gapOrderIndex ),
                                                             isFwdStrand, 1, baseQuality, mappingQuality );
                        if( mapping != null ) {
                            mapping.addGenomeGap( gap );
                        } else {
                            gaps.add( gap );
                        }
                    }   //refPos remains the same
                    readPos += currentCount;
                    break;
                case "N": //fallthrough, treatment is equal to "P"
                case "P":
                    //increase ref position for padded and skipped reference bases
                    refPos += currentCount;
                    //readPos remains the same
                    break;
                case "S":
                    //increase read position for soft clipped bases which are present in the read
                    //refPos remains the same
                    readPos += currentCount;
                    break;
                case "H": //just do nothing since the position is not in the reference and not in the read!
                    break;
                default:
                    break;
            }
        }

        if( mapping != null ) {
            mapping.setDifferences( differences );
        }

        return new DiffAndGapResult( diffs, gaps, gapOrderIndex, differences );
    }


    /**
     * Adds a diff either to the mapping, if it is not null, or to the diffs.
     * <p>
     * @param mapping the mapping to which the diff shall be added or
     * <cc>null</cc>.
     * @param diffs   the diffs list to which the diff shall be added.
     * @param diff    the diff to add
     */
    private void addDiff( Mapping mapping, List<Difference> diffs, Difference diff ) {
        if( mapping != null ) {
            mapping.addDiff( diff );
        } else {
            diffs.add( diff );
        }
    }


    /**
     * Closes this reader.
     */
    public void close() {
        try {
            samFileReader.close();
        } catch( IOException ex ) {
            LOG.error( ex.getMessage(), ex );
        }
    }


    @Override
    public void registerObserver( Observer observer ) {
        observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        observers.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : observers ) {
            observer.update( data );
        }
    }


    /**
     * Checks if the classification is valid according to the read class
     * parameters contained in the interval request.
     * <p>
     * @param classification  the classification to check
     * @param readClassParams the request whose parameters are used
     * <p>
     * @return true, if the mapping can be included in further steps, false
     *         otherwise
     */
    private boolean isIncludedMapping( MappingClass mappingClass, Integer numMappingsForRead, int mappingQuality, ParametersReadClasses readClassParams ) {
        boolean isIncludedMapping = (readClassParams.isClassificationAllowed( FeatureType.MULTIPLE_MAPPED_READ ) ||
                                     !readClassParams.isClassificationAllowed( FeatureType.MULTIPLE_MAPPED_READ ) && numMappingsForRead != null && numMappingsForRead == 1) &&
                                    (mappingQuality == UNKNOWN_MAP_QUAL ||
                                     mappingQuality >= readClassParams.getMinMappingQual());
        if( isIncludedMapping ) {
            isIncludedMapping = readClassParams.isClassificationAllowed( mappingClass );
        }
        return isIncludedMapping;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update( Object args ) {
        this.notifyObservers( args );
    }


}
