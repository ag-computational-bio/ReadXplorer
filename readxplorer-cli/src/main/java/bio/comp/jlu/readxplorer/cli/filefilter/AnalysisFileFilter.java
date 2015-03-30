/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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


package bio.comp.jlu.readxplorer.cli.filefilter;


import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;


/**
 * Analysis File Filter.
 * This <code>FileFilter</code> implementation only accepts excel (.xls) files
 * which names contain a certain analysis type prefix e.g. "snp".
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class AnalysisFileFilter implements FileFilter {

    private static final Logger LOG = Logger.getLogger( AnalysisFileFilter.class.getName() );


    static {
        LOG.setLevel( INFO );
    }


    public static final String SUFFIX = "xls";

    private static final String DOT_SUFFIX = '.' + SUFFIX;
    private static final String MERGED_RESULTS_FILE = "analyses" + DOT_SUFFIX;

    private final String analysisType;
    private final String prefixDash;


    /**
     * Creates an <code>AnalysisFileFilter</code> instance for a certain type of analysis.
     * <p>
     * For example a file filter accepting SNP analysis result files should
     * be created with an "snp" analysis type.
     * @param analysisType analysis type and file prefix
     */
    public AnalysisFileFilter( String analysisType ) {

        this.analysisType = analysisType;
        prefixDash = analysisType + '-';

    }


    @Override
    public boolean accept( File file ) {

        LOG.log( FINE, "check file: {0}", file.getAbsolutePath() );

        if( !file.isFile() || !file.canRead() ) {
            LOG.log( FINER, "file ({0}) is either not a file or is not readable!", file.getAbsolutePath() );
            return false;
        }

        String fileName = file.getName();
        if( !fileName.startsWith( prefixDash ) ) {
            LOG.log( FINER, "file name ({0}) doesn't start with prefix (" + analysisType + "-)!", file.getAbsolutePath() );
            return false;
        }
        if( fileName.equals( prefixDash + MERGED_RESULTS_FILE ) ) {
            return false;
        }

        return DOT_SUFFIX.equals( fileName.substring( file.getName().lastIndexOf( '.' ) ).toLowerCase() );

    }


}
