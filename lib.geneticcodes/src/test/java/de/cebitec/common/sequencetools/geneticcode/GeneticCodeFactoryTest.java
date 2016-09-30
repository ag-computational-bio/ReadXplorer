package de.cebitec.common.sequencetools.geneticcode;

import de.cebitec.common.sequencetools.geneticcode.IllegalGeneticCodeException;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author rhilker
 *
 * Tests the GeneticCodeFactory class.
 */
public class GeneticCodeFactoryTest{

    @Test
    public void testExisitingGeneticCodes() {
        GeneticCodeFactory gcf = GeneticCodeFactory.getDefault();

        int[] existing = new int[]{1,2,3,4,5,6,9,10,11,12,13,14,15,16,21,22,23};
        for (int i : existing) {
            assertNotNull(gcf.getGeneticCodeById(i));
        }

        int[] notExisting = new int[]{7,8,17,18,19,20};
        for (int i : notExisting) {
            try {
                gcf.getGeneticCodeById(i);
                fail();
            } catch (IllegalGeneticCodeException ex) {
            }
        }
    }

    /**
     * Test of initGeneticCodes method, of class GeneticCodeFactory.
     */
    @Test
    public void testGetGeneticCodes() {
        System.out.println("initGeneticCodes");
        List<GeneticCode> geneticCodes = GeneticCodeFactory.getDefault().getGeneticCodes();
        //test correctness of fst entry according to ftp://ftp.ncbi.nih.gov/entrez/misc/data/gc.prt
        //just by checking some random entries of the code
        GeneticCode code = geneticCodes.get(0);

        assertTrue(code.getDescription().equals("Standard"));
        assertTrue(code.getAlternativeDescription().equals("SGC0"));
        assertTrue(code.getId() == 1);

        List<String> startCodons = code.getStartCodons();
        assertTrue(startCodons.get(0).equals("TTG") || startCodons.get(0).equals("CTG") || startCodons.get(0).equals("ATG"));
        assertTrue(startCodons.get(1).equals("TTG") || startCodons.get(1).equals("CTG") || startCodons.get(1).equals("ATG"));
        assertTrue(startCodons.get(2).equals("TTG") || startCodons.get(2).equals("CTG") || startCodons.get(2).equals("ATG"));
        assertTrue(!startCodons.get(0).equals(startCodons.get(1)) && !startCodons.get(0).equals(startCodons.get(2)) &&
                   !startCodons.get(1).equals(startCodons.get(2)));

        List<String> stopCodons = code.getStopCodons();
        assertTrue(stopCodons.get(0).equals("TAA") || stopCodons.get(0).equals("TAG") || stopCodons.get(0).equals("TGA"));
        assertTrue(stopCodons.get(1).equals("TAA") || stopCodons.get(1).equals("TAG") || stopCodons.get(1).equals("TGA"));
        assertTrue(stopCodons.get(2).equals("TAA") || stopCodons.get(2).equals("TAG") || stopCodons.get(2).equals("TGA"));
        assertFalse(stopCodons.get(0).equals(stopCodons.get(1)) || stopCodons.get(0).equals(stopCodons.get(2)) ||
                   stopCodons.get(1).equals(stopCodons.get(2)));
        assertTrue(code.getTranslation("CAG").equals('Q'));

        List<String> translations = code.getInverseTranslation('F');
        assertTrue(translations.get(0).equals("TTT") || translations.get(0).equals("TTC"));
        assertTrue(translations.get(1).equals("TTT") || translations.get(1).equals("TTC"));
        assertFalse(translations.get(0).equals(translations.get(1)));

        Map<String, Character> map = code.getTranslationMap();
        assertTrue(map.size() == 64);
        assertTrue(map.containsKey("ATT"));
        assertTrue(map.get("ATT").equals('I'));

    }
}
