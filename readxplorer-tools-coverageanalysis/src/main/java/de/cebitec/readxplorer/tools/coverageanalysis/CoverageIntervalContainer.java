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

package de.cebitec.readxplorer.tools.coverageanalysis;


import de.cebitec.readxplorer.databackend.dataobjects.AnalysisResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Container for all data belonging to a coverage analysis result. Also converts
 * a all data into the format readable for the ExcelExporter. Generates all
 * three, the sheet names, headers and data to write.
 *
 * @author Tobias Zimmermann, Rolf Hilker
 * <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class CoverageIntervalContainer extends AnalysisResult {

    private static final long serialVersionUID = 1L;

    private final List<CoverageInterval> coverageInfo;
    private final List<CoverageInterval> coverageInfoRev;


    /**
     * Container for all data belonging to a coverage analysis result. Also
     * converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     */
    public CoverageIntervalContainer() {
        this.coverageInfo = new ArrayList<>();
        this.coverageInfoRev = new ArrayList<>();
    }


    /**
     * Container for all data belonging to a coverage analysis result. Also
     * converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * <p>
     * @param coverageIntervals set the coverage intervals of this result
     *                          directly, if only one array for both strands needs to be added
     */
    public CoverageIntervalContainer( List<CoverageInterval> coverageIntervals ) {
        this.coverageInfo = coverageIntervals;
        this.coverageInfoRev = new ArrayList<>();
    }


    /**
     * Container for all data belonging to a coverage analysis result. Also
     * converts a all data into the format readable for the ExcelExporter.
     * Generates all three, the sheet names, headers and data to write.
     * <p>
     * @param coverageInfoFwd set the forward coverage intervals of this result
     *                        directly
     * @param coverageInfoRev set the reverse coverage intervals of this result
     *                        directly
     */
    public CoverageIntervalContainer( List<CoverageInterval> coverageInfoFwd, List<CoverageInterval> coverageInfoRev ) {
        this( coverageInfoFwd );
        this.coverageInfoRev.addAll( coverageInfo );
    }


    /**
     * @return The size of both coverage interval arrays = result size
     */
    public int getResultSize() {
        return this.coverageInfo.size() + this.coverageInfoRev.size();
    }


    /**
     * @return the fwd or complete (if coverage of both strands was summed)
     *         coverage interval list
     */
    public List<CoverageInterval> getCoverageIntervals() {
        return Collections.unmodifiableList( coverageInfo );
    }


    /**
     * @param coverageIntervals the fwd or complete (if coverage of both strands
     *                          was summed) coverage interval list to set
     */
    public void setIntervalsSumOrFwd( List<CoverageInterval> coverageIntervals ) {
        this.coverageInfo.clear();
        this.coverageInfo.addAll( coverageIntervals );
    }


    /**
     * @return the rev coverage interval list
     */
    public List<CoverageInterval> getCoverageIntervalsRev() {
        return Collections.unmodifiableList( coverageInfoRev );
    }


    /**
     * @param coverageIntervalsRev the rev coverage interval list to set
     */
    public void setIntervalsRev( List<CoverageInterval> coverageIntervalsRev ) {
        this.coverageInfoRev.clear();
        this.coverageInfoRev.addAll( coverageIntervalsRev );
    }


}
