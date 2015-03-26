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
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class SNPAnalysisFileFilter implements FileFilter {

    private static final Logger LOG = Logger.getLogger( SNPAnalysisFileFilter.class.getName() );

    static {
        LOG.setLevel( INFO );
    }

    public static final String SUFFIX = "xls";
    public static final String PREFIX = "snp";
    private static final String PREFIX_COMPLETE = PREFIX + '-';
    private static final String SUFFIX_COMPLETE = '.' + SUFFIX;
    private static final String MERGED_RESULTS_FILE = PREFIX_COMPLETE + "analyses" + SUFFIX_COMPLETE;


    @Override
    public boolean accept( File file ) {

        LOG.log( FINE, "check file: {0}", file.getAbsolutePath() );

        if( !file.isFile()  ||  !file.canRead() ) {
            LOG.log( FINER, "file: {0} is either not a file or is not readable!", file.getAbsolutePath() );
            return false;
        }

        String fileName = file.getName();
        if( fileName.indexOf( '.' ) == -1 ) {
            LOG.log( FINER, "file: {0} doesn't contain a dot ('.') in its name!", file.getAbsolutePath() );
            return false;
        }
        if( fileName.indexOf( '-' ) == -1 ) {
            LOG.log( FINER, "file: {0} doesn't contain dash ('-') in its name!", file.getAbsolutePath() );
            return false;
        }
        if( MERGED_RESULTS_FILE.equals( fileName ) ) {
            return false;
        }


        String suffix = file.getName().substring( file.getName().lastIndexOf( '.' ) ).toLowerCase();
        String prefix = file.getName().substring( 0, 4 );

        return SUFFIX_COMPLETE.equals( suffix ) &&
               PREFIX_COMPLETE.equals( prefix );

    }

}
