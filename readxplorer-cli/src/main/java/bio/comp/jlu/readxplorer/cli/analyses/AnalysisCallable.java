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


import bio.comp.jlu.readxplorer.cli.analyses.AnalysisCallable.AnalysisResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public abstract class AnalysisCallable implements Callable<AnalysisResult> {

    protected final boolean verbosity;

    protected final AnalysisResult result;


    protected AnalysisCallable( boolean verbosity, String anaylsis ) {

        this.verbosity = verbosity;
        this.result = new AnalysisResult( anaylsis );

    }


    public class AnalysisResult {

        private final String analysisType;
        private final List<String> output;
        private String resultFile;


        public AnalysisResult( String analysisType ) {
            this.analysisType = analysisType;
            this.output = new ArrayList<>( 5 );
        }


        public String getType() {
            return analysisType;
        }


        public void addOutput( String msg ) {
            output.add( msg );
        }

        public List<String> getOutput() {
            return output;
        }


        public String getResultFile() {
            return resultFile;
        }


        public void setResultFile( String resultFile ) {
            this.resultFile = resultFile;
        }

    }


}
