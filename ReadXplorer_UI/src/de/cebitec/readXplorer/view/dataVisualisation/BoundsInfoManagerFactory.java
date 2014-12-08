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

package de.cebitec.readXplorer.view.dataVisualisation;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import java.util.HashMap;


/**
 * This class keeps track of created BoundsInfoManager-Instances and
 * ensures that there is only one BoundsInfoManager per genome
 * <p>
 * This is to ensure, that the panel navigation (i.e. "Jump to") still
 * works even if there are multiple genome object instances in use
 * <p>
 * @author Evgeny Anisiforov
 */
public class BoundsInfoManagerFactory {

    private final HashMap<Integer, BoundsInfoManager> data;


    public BoundsInfoManagerFactory() {
        this.data = new HashMap<>();
    }


    public BoundsInfoManager get( PersistentReference genome ) {
        if( data.containsKey( genome.getId() ) ) {
            return data.get( genome.getId() );
        }
        else {
            BoundsInfoManager boundsManager = new BoundsInfoManager( genome );
            data.put( genome.getId(), boundsManager );
            return boundsManager;
        }
    }


    public void clear() {
        data.clear();
    }


}
