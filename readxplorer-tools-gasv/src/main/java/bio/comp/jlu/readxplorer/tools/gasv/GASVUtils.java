/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.classification.MappingClass;
import net.sf.samtools.SAMRecord;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class GASVUtils {


    /**
     * Instantiation not allowed!
     */
    private GASVUtils() {
    }


    /**
     * Checks whether the given SamRecord belongs to either the
     * {@link MappingClass.SINGLE_PERFECT_MATCH} or
     * {@link MappingClass.SINGLE_BEST_MATCH} class. These mappings are allowed,
     * all others are not.
     * <p>
     * @param record The record to check
     * <p>
     * @return <code>true</code> if the record does NOT belong to one of the
     *         above mentioned mapping classes, <code>false</code> if it is a
     *         mapping that can be accepted for the analysis
     * <p>
     * @throws NumberFormatException
     */
    public static boolean isForbiddenMapping( SAMRecord record ) throws NumberFormatException {
        Object readClass = record.getAttribute( Properties.TAG_READ_CLASS );
        if( readClass != null ) {
            Byte classification = Byte.valueOf( readClass.toString() );
            MappingClass mappingClass = MappingClass.getFeatureType( classification );
            if( MappingClass.SINGLE_PERFECT_MATCH != mappingClass && MappingClass.SINGLE_BEST_MATCH != mappingClass ) {
                return true;
            }
        }
        return false;
    }


}
