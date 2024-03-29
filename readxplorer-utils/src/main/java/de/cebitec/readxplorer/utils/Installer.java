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

package de.cebitec.readxplorer.utils;


import htsjdk.samtools.util.FileExtensions;
import org.openide.modules.ModuleInstall;


public class Installer extends ModuleInstall {

    private static final long serialVersionUID = 1L;


    @Override
    public void restored() {
        //add more fasta extensions to prevent errors when using such files as reference
        FileExtensions.FASTA.add( ".fas" );
        FileExtensions.FASTA.add( ".fas.gz" );
        FileExtensions.FASTA.add( ".fna" );
        FileExtensions.FASTA.add( ".fna.gz" );
    }


}
