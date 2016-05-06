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


import de.cebitec.readxplorer.api.FileException;
import de.cebitec.readxplorer.databackend.connector.DatabaseException;
import de.cebitec.readxplorer.databackend.connector.MultiTrackConnector;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.utils.FastaUtils;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.util.RuntimeIOException;
import java.awt.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.cebitec.readxplorer.databackend.Bundle.MSG_FileReset;
import static de.cebitec.readxplorer.databackend.Bundle.MSG_IncompleteFileReset;
import static de.cebitec.readxplorer.databackend.Bundle.MSG_WrongFileChosen;
import static de.cebitec.readxplorer.databackend.Bundle.MSG_WrongTrackFileChosen;
import static de.cebitec.readxplorer.databackend.Bundle.TITLE_FileReset;
import static de.cebitec.readxplorer.databackend.Bundle.TITLE_IncompleteFileReset;


/**
 * A class for GUI Components to safely fetching files within ReadXplorer.
 *
 * @author kstaderm, rhilker
 */
@Messages( { "TITLE_FileReset=Reset track file path",
             "MSG_FileReset=If you do not reset the track file location, it cannot be opened" } )
public class SaveFileFetcherForGUI {

    private static final Logger LOG = LoggerFactory.getLogger( SaveFileFetcherForGUI.class.getName() );
    private static final ProjectConnector CONNECTOR = ProjectConnector.getInstance();


    /**
     * A class for GUI Components to safely fetching files within ReadXplorer.
     */
    public SaveFileFetcherForGUI() {
    }


    /**
     * Returns the Persistent^Connector for the given track. If the track is
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     * <p>
     * @param track Track the TrackConnector should be received for.
     * <p>
     * @return TrackConnector for the Track handed over
     *
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     *                                              be resolved.
     * @throws DatabaseException                    An exception during data
     *                                              queries. It has already been
     *                                              logged.
     */
    public TrackConnector getTrackConnector( PersistentTrack track ) throws UserCanceledTrackPathUpdateException, DatabaseException {
        TrackConnector tc = null;

        try {
            tc = CONNECTOR.getTrackConnector( track );
        } catch( FileNotFoundException e ) {
            PersistentTrack newTrack = getNewFilePath( track );
            if( newTrack != null ) {
                try {
                    tc = CONNECTOR.getTrackConnector( newTrack );
                } catch( FileNotFoundException ex ) {
                    Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                    LOG.error( currentTimestamp + ": Unable to open chosen track file: " + newTrack.getFilePath() );
                }
            } else {
                //If the new path is not set by the user throw exception notifying about this
                throw new UserCanceledTrackPathUpdateException();
            }
        }
        return tc;
    }


    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     * <p>
     * @param tracks        List of tracks the TrackConnector should be received
     *                      for.
     * @param combineTracks boolean if the Tracks should be combined or not.
     * <p>
     * @return TrackConnector for the list of Tracks handed over. CAUTION:
     *         tracks are removed if their path cannot be resolved and the user
     *         refuses to set a new one.
     *
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     *                                              be resolved.
     * @throws DatabaseException                    An exception during data
     *                                              queries. It has already been
     *                                              logged.
     */
    public TrackConnector getTrackConnector( List<PersistentTrack> tracks, boolean combineTracks ) throws UserCanceledTrackPathUpdateException, DatabaseException {
        TrackConnector tc = null;
        try {
            tc = CONNECTOR.getTrackConnector( tracks, combineTracks );
        } catch( FileNotFoundException e ) {
            //we keep track about the number of tracks with unresolved path errors.
            int unresolvedTracks = 0;
            for( int i = 0; i < tracks.size(); ++i ) {
                PersistentTrack track = tracks.get( i );
                if( !(new File( track.getFilePath() )).exists() ) {
                    PersistentTrack newTrack = getNewFilePath( track );
                    //Everything is fine, path is set correctly
                    if( newTrack != null ) {
                        tracks.set( i, newTrack );
                    } else {
                        //User canceled path update, add an unresolved track
                        unresolvedTracks++;
                        //And remove the track with wrong path from the list of processed tracks.
                        tracks.remove( i );
                    }
                }
            }
            //All track paths are tested, if no path can be resolved an exception is thrown.
            if( unresolvedTracks == tracks.size() ) {
                throw new UserCanceledTrackPathUpdateException();
            }
            try {
                tc = CONNECTOR.getTrackConnector( tracks, combineTracks );
            } catch( FileNotFoundException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( currentTimestamp + ": Unable to open at least one of the tracks: " + ex.getMessage() );
            }
        }
        return tc;
    }


    /**
     * In case a direct access track was moved to another place this method
     * first tries to find the track in the current directory used for resetting
     * track file paths and if it cannot be found it calls the
     * <tt>openResetFilePathDialog</tt> method to open a dialog for resetting
     * the file path to the current location of the file.
     * <p>
     * @param track the track whose path has to be reseted
     * <p>
     * @return the track connector for the updated track or null, if it did not
     *         work
     * <p>
     * @author rhilker, kstaderm
     */
    private PersistentTrack getNewFilePath( PersistentTrack track ) {
        PersistentTrack newTrack;
        File oldTrackFile = new File( track.getFilePath() );
        String basePath = NbPreferences.forModule( Object.class ).get( "ResetTrack.Filepath", "." );
        newTrack = this.checkFileExists( basePath, oldTrackFile, track );
        if( newTrack == null ) {
            basePath = new File( CONNECTOR.getDbLocation() ).getParentFile().getAbsolutePath();
            newTrack = this.checkFileExists( basePath, oldTrackFile, track );
        }
        if( newTrack == null ) {
            newTrack = this.openResetFilePathDialog( track );
        }
        return newTrack;
    }


    /**
     * Checks if a file exists and creates a new track, if it exists.
     * <p>
     * @param basePath     Base path without file name
     * @param oldTrackFile the old track file to replace
     * @param track        the old track to replace
     * <p>
     * @return the new track, if the file exists, null otherwise
     *
     * @author rhilker, kstaderm
     */
    private PersistentTrack checkFileExists( String basePath, File oldTrackFile, PersistentTrack track ) {
        PersistentTrack newTrack = null;
        String name = oldTrackFile.getName();

        /* If this is true than we are working with a DB created on windows. In
         * this case Java will return not the name of the file, but the complete
         * windows path still containing \\ as path seperator. If this is the
         * case we have to manually split the name to get the real filename. AND
         * YES: two backslashed in the contains clause and four in the split
         * method are intended and CORRECT!
         */
        if( name.contains( "\\" ) ) {
            //We split at the windows path seperator
            String[] split = name.split( "\\\\" );
            //The last entry of the split array must be the real filename
            name = split[split.length - 1];
        }

        File newTrackFile = new File( basePath + File.separator + name );
        
        if( newTrackFile.exists() && newTrackFile.isFile() ) {
            newTrack = new PersistentTrack( track.getId(),
                                            newTrackFile.getAbsolutePath(), track.getDescription(), track.getTimestamp(),
                                            track.getRefGenID(), track.getReadPairId() );
            try {
                SamBamFileReader reader = new SamBamFileReader( newTrackFile, track.getId(), CONNECTOR.getRefGenomeConnector( track.getRefGenID() ).getRefGenome() );
                try {
                    CONNECTOR.resetTrackPath( newTrack );
                } catch( DatabaseException ex ) {
                    ErrorHelper.getHandler().handle( ex, TITLE_FileReset() );
                }
            } catch( RuntimeIOException | DatabaseException e ) {
                //nothing to do, we return a null track
                LOG.trace( e.getMessage(), e );
            }
        }
        return newTrack;
    }


    /**
     * In case a track was moved to another place and cannot be found this
     * method opens a dialog for resetting the file path to the current location
     * of the file.
     * <p>
     * @param track the track whose path has to be resetted
     * <p>
     * @return the track connector for the updated track or null, if it did not
     *         work
     *
     * @author rhilker, kstaderm
     */
    @Messages( { "MSG_WrongTrackFileChosen=You did not choose a \"bam\" file, please select a bam file to proceed." } )
    private PersistentTrack openResetFilePathDialog( PersistentTrack track ) {
        PersistentTrack newTrack = null;
        ResetFilePanel resetPanel = new ResetFilePanel( track.getFilePath() );
        DialogDescriptor dialogDescriptor = new DialogDescriptor( resetPanel, "Reset File Path" );
        Dialog resetFileDialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        resetFileDialog.setVisible( true );

        if( dialogDescriptor.getValue().equals( DialogDescriptor.OK_OPTION ) ) {
            if( resetPanel.getNewFileLocation() != null ) {
                File selectedFile = new File( resetPanel.getNewFileLocation() );
                if( selectedFile.exists() && selectedFile.isFile() && selectedFile.getName().endsWith( ".bam" ) ) {
                    try {
                        newTrack = new PersistentTrack( track.getId(),
                                                        resetPanel.getNewFileLocation(), track.getDescription(), track.getTimestamp(),
                                                        track.getRefGenID(), track.getReadPairId() );
                        CONNECTOR.resetTrackPath( newTrack );
                        try {
                            TrackConnector trackConnector = CONNECTOR.getTrackConnector( newTrack );
                        } catch( FileNotFoundException ex ) {
                            LOG.error( ex.getMessage(), ex );
                            ErrorHelper.getHandler().handle( new FileNotFoundException( MSG_FileReset() ), TITLE_FileReset() );
                        }
                    } catch( DatabaseException ex ) {
                        ErrorHelper.getHandler().handle( ex, TITLE_FileReset() );
                    }
                } else if( !selectedFile.getName().endsWith( ".bam" ) ) {
                    LOG.warn( MSG_WrongTrackFileChosen() );
                    ErrorHelper.getHandler().handle( new IllegalArgumentException( MSG_WrongTrackFileChosen() ), TITLE_FileReset() );
                    this.openResetFilePathDialog( track );
                }
            }
        } else {
            LOG.warn( MSG_FileReset() );
            ErrorHelper.getHandler().handle( new FileException( MSG_FileReset(), null ), TITLE_FileReset() );
        }
        return newTrack;
    }


    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     * <p>
     * @param track The track for which the TrackConnector should be received.
     * <p>
     * @return A multi track connector for this track
     *
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     *                                              be resolved.
     *
     * @throws DatabaseException                    An exception during data
     *                                              queries. It has already been
     *                                              logged.
     */
    public MultiTrackConnector getMultiTrackConnector( PersistentTrack track ) throws UserCanceledTrackPathUpdateException, DatabaseException {
        MultiTrackConnector mtc = null;
        try {
            mtc = CONNECTOR.getMultiTrackConnector( track );
        } catch( FileNotFoundException e ) {
            PersistentTrack newTrack = getNewFilePath( track );
            if( newTrack != null ) {
                try {
                    mtc = CONNECTOR.getMultiTrackConnector( newTrack );
                } catch( FileNotFoundException ex ) {
                    Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                    LOG.error( currentTimestamp + ": Unable to open chosen track file: " + newTrack.getFilePath() );
                }
            } else {
                //If the new path is not set by the user throw exception notifying about this
                throw new UserCanceledTrackPathUpdateException();
            }
        }
        return mtc;
    }


    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     * <p>
     * @param tracks List of tracks the TrackConnector should be received for.
     * <p>
     * @return A multi track connector for these tracks
     *
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     *                                              be resolved.
     * @throws DatabaseException                    An exception during data
     *                                              queries. It has already been
     *                                              logged.
     */
    public MultiTrackConnector getMultiTrackConnector( List<PersistentTrack> tracks ) throws UserCanceledTrackPathUpdateException, DatabaseException {
        MultiTrackConnector mtc = null;
        try {
            mtc = CONNECTOR.getMultiTrackConnector( tracks );
        } catch( FileNotFoundException e ) {
            //we keep track about the number of tracks with unresolved path errors.
            int unresolvedTracks = 0;
            for( int i = 0; i < tracks.size(); ++i ) {
                PersistentTrack track = tracks.get( i );
                if( !(new File( track.getFilePath() )).exists() ) {
                    PersistentTrack newTrack = getNewFilePath( track );
                    //Everything is fine, path is set correctly
                    if( newTrack != null ) {
                        tracks.set( i, newTrack );
                    } else {
                        //User canceled path update, add an unresolved track
                        unresolvedTracks++;
                        //And remove the track with wrong path from the list of processed tracks.
                        tracks.remove( i );
                    }
                }
            }
            //All track paths are tested, if no path can be resolved an exception is thrown.
            if( unresolvedTracks == tracks.size() ) {
                throw new UserCanceledTrackPathUpdateException();
            }
            try {
                mtc = CONNECTOR.getMultiTrackConnector( tracks );
            } catch( FileNotFoundException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( currentTimestamp + ": Unable to open at least one of the tracks: " + ex.getMessage() );
            }
        }
        return mtc;
    }


    /**
     * Shows a message saying that the track path selection failed.
     */
    @Messages( { "MSG_IncompleteFileReset=You did not complete the track path selection. Corresponding viewers cannot be opened and " +
                 "analyses are canceled.",
                 "TITLE_IncompleteFileReset=Error resolving path to track" } )
    public static void showPathSelectionErrorMsg() {
        ErrorHelper.getHandler().handle( new FileException( MSG_IncompleteFileReset(), null ), TITLE_IncompleteFileReset() );
    }


    /**
     * Checks if the fasta file belonging to a reference exists and if not, it
     * tries to get a new path from the user. Missing fasta indices are
     * automatically recreated.
     * <p>
     * @param ref The reference whose fasta path has to be resetted
     * <p>
     * @return The indexed fasta file
     * <p>
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     *                                              be resolved.
     */
    public IndexedFastaSequenceFile checkRefFile( PersistentReference ref ) throws UserCanceledTrackPathUpdateException {
        File fastaFile = ref.getFastaFile();
        IndexedFastaSequenceFile indexedRefFile;
        FastaUtils fastaUtils = new FastaUtils(); //TODO observers are empty, add observers!
        if( fastaFile.exists() && fastaFile.isFile() ) {
            //check for index and recreate it with notification, if necessary
            try( IndexedFastaSequenceFile testRefIndexFile = new IndexedFastaSequenceFile( fastaFile ) ) {
                indexedRefFile = testRefIndexFile;
                try { //check if all entries in the file are valid, otherwise delete and recreate index
                    for( PersistentChromosome chrom : ref.getChromosomes().values() ) {
                        //just iterate them to see if they exist
                        indexedRefFile.getSubsequenceAt( chrom.getName(), 1, 2 );
                    }
                } catch( SAMException | NullPointerException e ) {
                    try {
                        LOG.info( "Reference fasta index " + ref.getName() + " corrupted. Will be recreated..." );
                        indexedRefFile.close();
                        fastaUtils.deleteIndexFile( fastaFile );
                        fastaUtils.recreateMissingIndex( fastaFile );
                    } catch( IOException ex ) {
                        String msg = fastaFile.getAbsolutePath() + "Unable to delete erroneous fasta index file. " +
                                     "Please delete it manually and restart ReadXplorer.";
                        LOG.error( msg, ex );
                        ErrorHelper.getHandler().handle( new IOException( msg ) );
                    }
                }
            } catch( FileNotFoundException e ) {
                fastaUtils.recreateMissingIndex( fastaFile );
            } catch( IOException e ) {
                String msg = fastaFile.getAbsolutePath() + ": Unable to close fasta index file.";
                LOG.error( msg );
                ErrorHelper.getHandler().handle( new IOException( msg ) );
            }
            indexedRefFile = fastaUtils.getIndexedFasta( fastaFile );

        } else {
            indexedRefFile = resetRefFile( ref );
        }
        if( indexedRefFile == null ) {
            //If the new path is not set by the user throw exception notifying about this
            throw new UserCanceledTrackPathUpdateException();
        }
        return indexedRefFile;
    }


    /**
     * In case a reference fasta file is missing and needs to be replaced, this
     * method offers a dialog to replace the old file with a new one in the
     * correct file format.
     * <p>
     * @param ref The reference whose fasta path has to be resetted
     * <p>
     * @return the new file or null, if it did not work
     *
     * @author rhilker
     */
    private IndexedFastaSequenceFile resetRefFile( PersistentReference ref ) {
        IndexedFastaSequenceFile newFastaFile = null;
        String name = ref.getFastaFile().getName();

        /* If this is true than we are working with a DB created on windows. In
         * this case Java will return not the name of the file, but the complete
         * windows path still containing \\ as path seperator. If this is the
         * case we have to manually split the name to get the real filename. AND
         * YES: two backslashed in the contains clause and four in the split
         * method are intended and CORRECT!
         */
        if( name.contains( "\\" ) ) {
            //We split at the windows path seperator
            String[] split = name.split( "\\\\" );
            //The last entry of the split array must be the real filename
            name = split[split.length - 1];
        }

        File newFile = new File( CONNECTOR.getDbLocation() ).getParentFile();
        newFile = new File( newFile.getAbsolutePath() + File.separator + name );

        if( !newFile.exists() ) {
            List<String> fileEndings = Arrays.asList( ".fasta", ".fa", ".fna", ".ffn" );
            newFile = this.openResetFilePathDialog( ref.getFastaFile(), fileEndings );
        }

        if( newFile != null ) {
            try {
                try {
                    newFastaFile = new IndexedFastaSequenceFile( newFile );
                } catch( FileNotFoundException ex ) { // we know the file exists, so only the index can be missing
                    LOG.info( "Reference fasta index " + ref.getName() + " missing. Will be recreated..." );
                    FastaUtils fastaUtils = new FastaUtils();  //TODO observers are empty, add observers!
                    fastaUtils.recreateMissingIndex( newFile );
                }
                CONNECTOR.resetReferencePath( newFile, ref );
            } catch( DatabaseException ex ) {
                ErrorHelper.getHandler().handle( ex, TITLE_FileReset() );
            }
        } else {
            ErrorHelper.getHandler().handle( new FileException( MSG_FileReset(), null ), TITLE_FileReset() );
            LOG.error( MSG_FileReset() );
        }

        return newFastaFile;
    }


    /**
     * In case a file is missing and needs to be replaced, this method offers a
     * dialog to replace the old file with a new one in the correct file format.
     * <p>
     * @param oldFile     Old file, whose path has to be resetted
     * @param fileEndings List of file endings to check
     * <p>
     * @return the new file or null, if it did not work
     *
     * @author rhilker
     */
    @Messages( { "# {0} - file formats", "MSG_WrongFileChosen=You did not choose a \"{0}\" file, please select a fasta file to proceed." } )
    private File openResetFilePathDialog( File oldFile, List<String> fileEndings ) {
        File newFile = null;
        ResetFilePanel resetPanel = new ResetFilePanel( oldFile.getAbsolutePath() );
        DialogDescriptor dialogDescriptor = new DialogDescriptor( resetPanel, "Reset File Path" );
        Dialog resetFileDialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        resetFileDialog.setVisible( true );

        if( dialogDescriptor.getValue().equals( DialogDescriptor.OK_OPTION ) && resetPanel.getNewFileLocation() != null ) {
            newFile = new File( resetPanel.getNewFileLocation() );

            boolean correctEnding = false;
            for( String fileEnding : fileEndings ) {
                if( newFile.getName().endsWith( fileEnding ) ) {
                    correctEnding = newFile.getName().endsWith( fileEnding );
                    break;
                }
            }

            if( newFile.exists() && newFile.isFile() && correctEnding ) {
                return newFile;

            } else if( !correctEnding ) {
                String msg;
                if( fileEndings.size() > 1 ) {
                    msg = MSG_WrongFileChosen( fileEndings.get( 0 ) );
                } else {
                    msg = MSG_WrongFileChosen( "correct" );
                }
                ErrorHelper.getHandler().handle( new IllegalArgumentException( msg ), TITLE_FileReset() );
                LOG.warn( msg );
                newFile = this.openResetFilePathDialog( oldFile, fileEndings );
            }
        } else {
            ErrorHelper.getHandler().handle( new FileException( MSG_FileReset(), null ), TITLE_FileReset() );
            LOG.error( MSG_FileReset() );
        }
        return newFile;
    }


    /**
     * Exception which should be thrown if the user cancels the update of a
     * missing track file path. Automatically logs the exception message.
     */
    public static class UserCanceledTrackPathUpdateException extends Exception {

        private static final long serialVersionUID = 1L;
        private static final String ERROR_MSG = "The user canceled the track path update. Thus, no TrackConnector can be created!";
//        private static final long serialVersionUID = 1L;


        /**
         * Exception which should be thrown if the user cancels the update of a
         * missing track file path. Automatically logs the exception message.
         */
        public UserCanceledTrackPathUpdateException() {
            super( ERROR_MSG );
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            LOG.warn( ERROR_MSG + ": ", currentTimestamp );
        }


    }

}
