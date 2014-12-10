/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.vcf.handling.visualization;


import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.broadinstitute.variant.variantcontext.VariantContext;


/**
 *
 * @author Marie
 */
public class Snp_VcfResult extends ResultTrackAnalysis<Object> {

    private final List<VariantContext> snpVcfList;
//    private VcfParser vcf = new VcfParser();


    /**
     *
     * @param snpVcfList
     * @param trackMap
     * @param reference
     * @param combineTracks
     */
    public Snp_VcfResult( List<VariantContext> snpVcfList, Map<Integer, PersistentTrack> trackMap, PersistentReference reference, boolean combineTracks ) {
        super( reference, trackMap, combineTracks, -1, 0 );
        this.snpVcfList = snpVcfList;
    }


    /**
     *
     * @return
     */
    public List<VariantContext> getSnpVcfList() {
        return this.snpVcfList;
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
