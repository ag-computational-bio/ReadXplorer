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
package de.cebitec.readXplorer.parser.common;

import java.util.HashSet;

/**
 * Since the RUN domain has been excluded a PersistentRun is not needed anymore!
 * Should be deprecated as long as it is not needed for anything else.
 *
 * @author ddoppmeier
 */
@Deprecated
public class ParsedReadname {

    private HashSet<String> reads;
    private long id;
    public int numOfReads;
    
    public ParsedReadname(){
        reads = new HashSet<String>();
    }

    public void addRead(String readname){
        reads.add(readname);
    }

    public HashSet<String> getReads(){
        return reads;
    }

    public int getNumOfReads() {
        return reads.size();
    }

    public long getID() {
        return id;
    }

    public void setID(long seqID) {
        this.id = seqID;
    }

}
