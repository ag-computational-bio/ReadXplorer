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

package de.cebitec.readxplorer.databackend.dataObjects;


import de.cebitec.readxplorer.databackend.IntervalRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Able to store the result for coverage, diffs and gaps.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CoverageAndDiffResult extends AnalysisResult implements
        Serializable {

    public static final long serialVersionUID = 42L;

    private List<CoverageManager> covManagers;
    private CoverageManager readStarts; //TODO: do the same for the other data structures as for the coverage
    private List<Difference> diffs;
    private List<ReferenceGap> gaps;


    /**
     * Data storage for a single coverageManager, diffs and gaps.
     *
     * @param covManager the coverage manager to store. If it is not used, you
     *                   can add an empty coverage manager.
     * @param diffs      the list of diffs to store, if they are not used, you
     *                   can
     *                   add null or an empty list.
     * @param gaps       the list of gaps to store, if they are not use, you can
     *                   add
     *                   null or an empty list
     * @param request    the interval request for which this result was
     *                   generated
     */
    public CoverageAndDiffResult( CoverageManager covManager, List<Difference> diffs, List<ReferenceGap> gaps,
                                  IntervalRequest request ) {
        this( new ArrayList<>( Arrays.asList( covManager ) ), diffs, gaps, request );
    }


    /**
     * Data storage for multiple coverageManager, diffs and gaps.
     * <p>
     * @param covManagers the coverage managers to store. If it is not used, you
     *                    can add an empty list.
     * @param diffs       the list of diffs to store, if they are not used, you
     *                    can
     *                    add null or an empty list.
     * @param gaps        the list of gaps to store, if they are not use, you
     *                    can add
     *                    null or an empty list
     * @param request     the interval request for which this result was
     *                    generated
     */
    public CoverageAndDiffResult( List<CoverageManager> covManagers, List<Difference> diffs, List<ReferenceGap> gaps,
                                  IntervalRequest request ) {
        super( request );
        this.covManagers = covManagers;
        readStarts = null;
        diffs = diffs;
        this.gaps = gaps;

    }


    /**
     * @return the diffs, if they are stored. If they are not,
     *         the list is empty.
     */
    public List<Difference> getDiffs() {
        if( diffs != null ) {
            return Collections.unmodifiableList( diffs );
        }
        else {
            return Collections.emptyList();
        }
    }


    /**
     * @return the gaps, if they are stored. If they are not,
     *         the list is empty.
     */
    public List<ReferenceGap> getGaps() {
        if( gaps != null ) {
            return Collections.unmodifiableList( gaps );
        }
        else {
            return Collections.emptyList();
        }
    }


    /**
     * @return the coverage manager, if it is stored. If it is not,
     *         it returns an empty coverage manager covering only 0.
     */
    public CoverageManager getCovManager() {
        if( covManagers != null && !covManagers.isEmpty() ) {
            return covManagers.get( 0 );
        }
        else {
            return new CoverageManager( 0, 0 );
        }
    }


    /**
     * @return the list of coverage managers, if it is stored. If it is not,
     *         it returns a list with an empty coverage manager covering only 0.
     */
    public List<CoverageManager> getCovManagers() {
        if( covManagers != null && !covManagers.isEmpty() ) {
            return Collections.unmodifiableList( covManagers );
        }
        else {
            return Collections.singletonList( new CoverageManager( 0, 0 ) );
        }
    }


    /**
     * Adds a CoverageManager to the list of managers.
     * <p>
     * @param covManager The CoverageManager to add
     */
    public void addCoverageManager( CoverageManager covManager ) {
        if( covManagers == null ) {
            covManagers = new ArrayList<>();
        }
        covManagers.add( covManager );
    }


    /**
     * @return the coverage object containing only the read start counts.
     */
    public CoverageManager getReadStarts() {
        return readStarts;
    }


    /**
     * @param readStarts The coverage object containing only the read start
     *                   counts.
     */
    public void setReadStarts( CoverageManager readStarts ) {
        this.readStarts = readStarts;
    }


}
