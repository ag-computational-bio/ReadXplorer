
package de.cebitec.readxplorer.transcriptomeanalyses.motifSearch;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import java.io.File;


/**
 *
 * @author jritter
 */
public class RbsAnalysisParameters implements ParameterSetI<Object> {

    private File workingDir;
    int seqLengthToAnalyze, motifWidth, numberOfCyclesForBioProspector, minSpacer;

//    public RbsAnalysisParameters(File workingDir, int seqLengthToAnalyze, int motifWidth, int numberOfCyclesForBioProspector, int minSpacer) {

    public RbsAnalysisParameters( int seqLengthToAnalyze, int motifWidth, int numberOfCyclesForBioProspector, int minSpacer ) {
//        this.workingDir = workingDir;

        this.seqLengthToAnalyze = seqLengthToAnalyze;
        this.motifWidth = motifWidth;
        this.numberOfCyclesForBioProspector = numberOfCyclesForBioProspector;
        this.minSpacer = minSpacer;
    }


    public File getWorkingDir() {
        return workingDir;
    }


    public int getMinSpacer() {
        return minSpacer;
    }


    public void setMinSpacer( int minSpacer ) {
        this.minSpacer = minSpacer;
    }


    public void setWorkingDir( File workingDir ) {
        this.workingDir = workingDir;
    }


    public int getSeqLengthToAnalyze() {
        return seqLengthToAnalyze;
    }


    public void setSeqLengthToAnalyze( int seqLengthToAnalyze ) {
        this.seqLengthToAnalyze = seqLengthToAnalyze;
    }


    public int getMotifWidth() {
        return motifWidth;
    }


    public void setMotifWidth( int motifWidth ) {
        this.motifWidth = motifWidth;
    }


    public int getNumberOfCyclesForBioProspector() {
        return numberOfCyclesForBioProspector;
    }


    public void setNumberOfCyclesForBioProspector( int numberOfCyclesForBioProspector ) {
        this.numberOfCyclesForBioProspector = numberOfCyclesForBioProspector;
    }


}
