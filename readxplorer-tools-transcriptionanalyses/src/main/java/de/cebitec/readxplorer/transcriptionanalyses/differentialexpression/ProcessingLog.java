/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;


import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author kstaderm
 */
public final class ProcessingLog {

    private StringBuilder gnuRprocessing = new StringBuilder();
    private Map<String, Object> properties = null;


    public synchronized void logGNURoutput( String output ) {
        gnuRprocessing.append( output ).append( "\n" );
    }


    public synchronized void logGNURinput( String input ) {
        gnuRprocessing.append( "> " ).append( input ).append( "\n" );
    }


    public synchronized void setProperties( Map<String, Object> properties ) {
        if( this.properties == null ) {
            this.properties = properties;
        }
    }


    public synchronized void addProperty( String key, Object value ) {
        properties.put( key, value );
    }


    public String generateLog() {
        StringBuilder log = new StringBuilder();
        Set<String> keys = properties.keySet();
        Map<String, Object> tmpProperties = new HashMap<>( properties );

        for( String key : keys ) {
            if( !key.startsWith( "WizardPanel" ) ) {
                Object currentProperty = tmpProperties.get( key );
                if( currentProperty instanceof Map ) {
                    Map currentPropertyMap = (Map) currentProperty;
                    log.append( key ).append( " = " ).append( Arrays.deepToString( currentPropertyMap.values().toArray() ) )
                            .append( System.getProperty( "line.separator" ) );
                } else if( currentProperty instanceof ParametersReadClasses) {
                    ParametersReadClasses currentPropertyParam = (ParametersReadClasses) currentProperty;
                    log.append( key ).append( " = " ).append( "Min Mapping Qualit:").append(currentPropertyParam.getMinMappingQual())
                            .append( "|").append( "Strand Option:").append(currentPropertyParam.getStrandOption())
                            .append( "|").append( "Excluded Mapping Classes:")
                            .append( Arrays.deepToString(currentPropertyParam.getExcludedClasses().toArray()));

                } else {
                    log.append( key ).append( " = " ).append( currentProperty.toString() ).append( System.getProperty( "line.separator" ) );
                }
            }
        }
        log.append( System.getProperty( "line.separator" ) );
        log.append( "GNU R output during processing: " );
        log.append( System.getProperty( "line.separator" ) );

        log.append( gnuRprocessing );

        return log.toString();
    }


}
