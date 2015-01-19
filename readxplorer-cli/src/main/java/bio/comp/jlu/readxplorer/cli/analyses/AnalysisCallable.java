
package bio.comp.jlu.readxplorer.cli.analyses;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public abstract class AnalysisCallable implements Callable<List<String>>{

    protected final boolean verbosity;

    protected final List<String> output;


    protected AnalysisCallable( boolean verbosity ) {

        this.verbosity  = verbosity;
        this.output     = new ArrayList<>( 10 );

    }


}
