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

package bio.comp.jlu.readxplorer.cli.analyses;


import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class SNPAnalysisArgsProcessor implements ArgsProcessor {

    private final static Logger LOG = Logger.getLogger( SNPAnalysisArgsProcessor.class.getName() );


    /** Decide annotations or code.
     * Annotations are much cleaner and more intuitive but comes
     * with the cost of no current possibility to
     * group and thus combine several dependent options.
     */
    @Arg( shortName = 'a', longName = "analysis" )
    @Description( displayName = "file", shortDescription = "this is a file option for testing purposes only!" )
    public String analysisOption;


    @Override
    public void process( Env env ) throws CommandException {
    }

}
