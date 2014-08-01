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
package de.cebitec.readXplorer.util;

/**
 * A simple limit to set a maximum to how often an error message can be shown.
 * 
 * @author Evgeny Anisiforov
 */
public class ErrorLimit {
    
    private long errorCount;
    private long maxErrorCount;
    
    /**
     * A simple limit to set a maximum to how often an error message can be shown.
     * @param maxErrorCount the maximum count for each error to be output
     */
    public ErrorLimit(long maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
        this.errorCount = 0;
    }
    
    /**
     * A simple limit to set a maximum to how often an error message can be shown.
     * The default error limit for this constructor is 20.
     */
    public ErrorLimit() {
        this(20);
    }
    
    //skip error messages, if too many occur to prevent bug in the output panel
    public boolean allowOutput() {
        this.setErrorCount(this.getErrorCount() + 1);
        return getErrorCount() <= getMaxErrorCount();
    }

    /**
     * @return the maxErrorCount
     */
    public long getMaxErrorCount() {
        return maxErrorCount;
    }

    /**
     * @param maxErrorCount the maxErrorCount to set
     */
    public void setMaxErrorCount(long maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
    }

    /**
     * @return the errorCount
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * @param errorCount the errorCount to set
     */
    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }
    
    /**
     * @return the amount of skipped errors
     */
    public long getSkippedCount() {
        long n = this.errorCount - this.maxErrorCount;
        if (n < 0) { n = 0; }
        return n;
    }
    
                    
}
