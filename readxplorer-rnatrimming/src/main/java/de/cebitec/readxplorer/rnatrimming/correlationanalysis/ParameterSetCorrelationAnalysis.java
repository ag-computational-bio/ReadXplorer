/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.rnatrimming.correlationanalysis;


import de.cebitec.readxplorer.databackend.ParameterSetI;
import de.cebitec.readxplorer.databackend.ParameterSetWithReadClasses;
import de.cebitec.readxplorer.databackend.ParametersReadClasses;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.rnatrimming.correlationanalysis.CorrelationAnalysisAction.CorrelationCoefficient;
import java.util.Collections;
import java.util.List;


/**
 * Data storage for all parameters associated with a correlation analysis.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ParameterSetCorrelationAnalysis extends ParameterSetWithReadClasses
        implements ParameterSetI<ParameterSetCorrelationAnalysis> {

    private final CorrelationCoefficient correlationCoefficient;
    private final int intervalLength;
    private final int minCorrelation;
    private final int minPeakCoverage;
    private final List<PersistentTrack> selectedTracks;


    /**
     * Data storage for all parameters associated with a correlation analysis.
     * <p>
     * @param selFeatureTypes        the set of selected feature types
     * @param readClassParams        only include data in the analysis, which
     *                               belong to
     *                               the selected read classes.
     * @param correlationCoefficient
     * @param intervalLength
     * @param minCorrelation
     * @param minPeakCoverage
     */
    public ParameterSetCorrelationAnalysis( ParametersReadClasses readClassParams, CorrelationCoefficient correlationCoefficient,
                                            int intervalLength, int minCorrelation, int minPeakCoverage, List<PersistentTrack> selectedTracks ) {
        super( readClassParams );

        this.correlationCoefficient = correlationCoefficient;
        this.intervalLength = intervalLength;
        this.minCorrelation = minCorrelation;
        this.minPeakCoverage = minPeakCoverage;
        this.selectedTracks = selectedTracks;
    }


    public CorrelationCoefficient getCorrelationCoefficient() {
        return correlationCoefficient;
    }


    public int getIntervalLength() {
        return intervalLength;
    }


    public int getMinCorrelation() {
        return minCorrelation;
    }


    public int getMinPeakCoverage() {
        return minPeakCoverage;
    }


    public List<PersistentTrack> getSelectedTracks() {
        return Collections.unmodifiableList( selectedTracks );
    }


}
