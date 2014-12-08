
package de.cebitec.readxplorer.vcf.importer;


import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.databackend.dataObjects.SnpI;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.samtools.util.CloseableIterator;
import org.broadinstitute.variant.variantcontext.Allele;
import org.broadinstitute.variant.variantcontext.GenotypesContext;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFFileReader;


/**
 *
 * @author marend
 */
final class VcfParser {

    private ArrayList<SnpI> snpList;
    private Snp snp;
    private final File vcfFile;
    private int positionA;
    private int positionE;
    private String source;
    private List<Allele> alleles;
    private GenotypesContext genotypeContext;
    private double log10PError;
    private Set<String> filters;
    private Map<String, Object> attributes;
    private int homologCount;


    /**
     * @author marend
     * <p>
     * Dem VcfParser wird ein vcf-File übergeben und die darin enthaltenen
     * Informationen
     * werden in einer Liste von VariantContexten gespeichert.
     * Diese soll später als Tabellenform ausgegeben werden können und
     * zusätzlich durch eine bestimmte Visualisierungsart grafisch dargestellt
     * werden.
     * <p>
     * @param vcfFile
     */
    VcfParser( File vcfFile ) {
        this.vcfFile = vcfFile;
        getVariantContextList();
    }


    public List<VariantContext> getVariantContextList() {
        VCFFileReader reader = new VCFFileReader( vcfFile, false );
//         VariantContextWriterFactory.create(vcfFile, null)

        CloseableIterator<VariantContext> it = reader.iterator();
        List<VariantContext> variantCs = new ArrayList<>();

        while( it.hasNext() ) {
            VariantContext variantC = it.next();

//            source = variantC.getSource();
//            System.out.println(source);
//            positionA = variantC.getStart();
//            System.out.println(positionA);
//            positionE = variantC.getEnd();
//            System.out.println(positionE);
//            alleles = variantC.getAlleles();
//            System.out.println(alleles);
//            System.out.println(alleles.get(0).getBaseString());
//            System.out.println(alleles.get(0).getDisplayString());
//            genotypeContext = variantC.getGenotypes();
//            System.out.println(genotypeContext);
//            System.out.println(genotypeContext.get(0).getAD());
//            System.out.println(genotypeContext.get(0).getDP());
//            System.out.println(genotypeContext.get(0).getGQ());
//            System.out.println(genotypeContext.get(0).getPL());
//            System.out.println(genotypeContext.get(0).getGenotypeString());
//            log10PError = variantC.getLog10PError();
//            System.out.println(log10PError);
//            filters = variantC.getFilters();
//            System.out.println(filters);
//            attributes = variantC.getAttributes();
//            System.out.println(attributes);
//            homologCount = variantC.getHomVarCount();
//            System.out.println(homologCount);


            variantCs.add( variantC );

        }
        return variantCs;
    }


}
