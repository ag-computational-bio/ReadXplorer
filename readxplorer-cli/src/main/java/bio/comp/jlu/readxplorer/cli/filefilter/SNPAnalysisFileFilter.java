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


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class SNPAnalysisFileFilter implements FileFilter {

    public static final String SUFFIX = "xls";
    public static final String PREFIX = "snp";
    private static final String PREFIX_COMPLETE = PREFIX + '-';
    private static final String SUFFIX_COMPLETE = '.' + SUFFIX;


    @Override
    public boolean accept( File file ) {

        String suffix = file.getName().substring( file.getName().lastIndexOf( '.' ) ).toLowerCase();
        String prefix = file.getName().substring( 0, 4 );

        return file.canRead() &&
               file.isFile() &&
               SUFFIX_COMPLETE.equals( suffix ) &&
               PREFIX_COMPLETE.equals( prefix );

    }

}
