/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import htsjdk.samtools.SAMSequenceRecord;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * Creates a sequence name that can be changed to a new name.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ChangeableSeqName implements Transferable, Serializable {

    private static final long serialVersionUID = 1L;

    public static final DataFlavor SEQNAME_FLAVOR = new DataFlavor( ChangeableSeqName.class, "ChangeableSeqName" );
    public static final DataFlavor LIST_FLAVOR = new DataFlavor( ArrayList.class, "ArrayList" );

    private final SAMSequenceRecord seqRecord;
    private String newSeqName;


    /**
     * Creates a sequence name that can be changed to a new name.
     *
     * @param seqName Original name of the reference sequence
     * @param seqLength Length of the reference sequence
     */
    public ChangeableSeqName( SAMSequenceRecord seqRecord ) {
        this.seqRecord = seqRecord;
        this.newSeqName = null;
    }


    /**
     * @return <code>true</code> if this sequence got a new name assigned,
     *         <code>false</code> otherwise
     */
    public boolean hasNewName() {
        return newSeqName != null;
    }


    /**
     * @return The currently preferred sequence name. If a new name was set, the
     *         new name is returned, otherwise the initial name.
     */
    public String getNewName() {
        return newSeqName == null ? seqRecord.getSequenceName() : newSeqName;
    }


    /**
     * @param newSeqName The new sequence name which should replace the original
     *                   sequence name.
     */
    public void setNewSeqName( String newSeqName ) {
        this.newSeqName = newSeqName;
    }


    /**
     * @return The original sequence name.
     */
    public SAMSequenceRecord getSeqRecord() {
        return seqRecord;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
        return this;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ SEQNAME_FLAVOR };
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isDataFlavorSupported( DataFlavor flavor ) {
        return SEQNAME_FLAVOR.equals( flavor );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return newSeqName == null ? seqRecord.getSequenceName() : newSeqName + " (replaces " + seqRecord.getSequenceName() + ")";
    }


}
