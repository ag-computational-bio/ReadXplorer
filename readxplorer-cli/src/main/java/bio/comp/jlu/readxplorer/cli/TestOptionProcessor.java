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

package bio.comp.jlu.readxplorer.cli;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
@ServiceProvider( service = OptionProcessor.class )
public class TestOptionProcessor extends OptionProcessor {

    private final static Logger LOG = Logger.getLogger( TestOptionProcessor.class.getName() );


    /** Decide annotations or code.
     * Annotations are much cleaner and more intuitive but comes
     * with the cost of no current possibility to
     * group and thus combine several dependent options.
     */
    @Arg( shortName = 'f', longName = "file" )
    @Description( displayName = "file", shortDescription = "this is a file option for testing purposes only!" )
    private final Option fileTestOption = Option.requiredArgument( 'f', "file" );


    @Override
    protected Set<Option> getOptions() {

        Set<Option> options = new HashSet<>( 5 );
            options.add( fileTestOption );

        return options;

    }


    @Override
    protected void process( Env env, Map<Option, String[]> options ) throws CommandException {

        LOG.info( "processing options..." );

        if( options.containsKey( fileTestOption ) ) {

            LOG.fine( "processing option f" );
            String argument = options.get( fileTestOption )[0];
            try{
                File file = new File( argument );
                BufferedReader br = new BufferedReader( new FileReader( file ) );
            }
            catch( FileNotFoundException fnfe ) {
                LOG.severe( "file does not exist!" );
                CommandException ce = new CommandException( 1 );
                    ce.initCause( fnfe );
                throw ce;
            }

        }

    }


}
