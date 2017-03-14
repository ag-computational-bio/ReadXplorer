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

package de.cebitec.readxplorer.vcfhandling.visualization;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Marie
 */
public class SnpVcfResult extends ResultTrackAnalysis<Object> {

    private final List<VariantContext> snpVcfList;


    /**
     *
     * @param snpVcfList
     * @param trackMap
     * @param reference
     * @param combineTracks
     */
    public SnpVcfResult( List<VariantContext> snpVcfList, Map<Integer, PersistentTrack> trackMap, PersistentReference reference, boolean combineTracks ) {
        super( reference, trackMap, combineTracks, -1, 0 );
        this.snpVcfList = new ArrayList<>( snpVcfList );
    }


    /**
     *
     * @return
     */
    public List<VariantContext> getSnpVcfList() {
        return Collections.unmodifiableList( snpVcfList );
    }


    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "SNP Vcf-Data Table" );
        return sheetNames;
    }


    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptionsList = new ArrayList<>();

        List<String> dataColumnDescriptions = new ArrayList<>();
        dataColumnDescriptions.add( "Source" );
        dataColumnDescriptions.add( "PositionA" );
        dataColumnDescriptions.add( "PositionE" );
        dataColumnDescriptions.add( "Alleles" );
        dataColumnDescriptions.add( "Genotypes" );
        dataColumnDescriptions.add( "Log 10 Error" );
        dataColumnDescriptions.add( "Filters" );
        dataColumnDescriptions.add( "Attributes" );
        dataColumnDescriptions.add( "Homolog Count" );


        dataColumnDescriptionsList.add( dataColumnDescriptions );

        //add snp statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "SNP Vcf-File Data" );
        dataColumnDescriptionsList.add( statisticColumnDescriptions );

        return dataColumnDescriptionsList;

    }


    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


}
