package de.cebitec.common.sequencetools.geneticcode;

import de.cebitec.common.sequencetools.geneticcode.MalformedGeneticCodeException;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.common.sequencetools.geneticcode.Codon;
import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author rhilker
 */
public class GeneticCodeTest {

    @Test
    public void testGetTranslation() {
        System.out.println("getTranslation");
        GeneticCode gc = GeneticCodeFactory.getDefault().getGeneticCodeById(1);

        assertEquals("x", gc.getTranslation("abc").toString());
        assertEquals("x", gc.getTranslation("nnn").toString());
        assertNull(gc.getTranslation("at"));
        assertNull(gc.getTranslation("atta"));
        assertNull(gc.getTranslation(""));
        assertNull(gc.getTranslation(null));
    }

    /**
     * Test of getTranslationForString method, of class GeneticCode.
     */
    @Test
    public void testGetTranslationForString() {
        System.out.println("getTranslationForString");
        String dnaToTranslate = "atgACGGGCACCAAGATTGAGTAGATGCGCTGAGTCTAG";

        GeneticCode stdCode = GeneticCodeFactory.getDefault().getGeneticCodeById(1);
        assertNotNull(stdCode);
        String expResult = "MTGTKIE*MR*V*";
        String result = stdCode.getTranslationForString(dnaToTranslate);
        assertEquals(expResult, result);

        // test what happens to sequences that have an incomplete codon at the end
        // they should be left out
        assertEquals("M", stdCode.getTranslationForString("atgaa"));

        // test n stretch
        assertEquals("xx", stdCode.getTranslationForString("nnnnnn"));
        assertEquals("xxx", stdCode.getTranslationForString("blödeswort"));
    }

    @Test
    public void testGeneticCodeException() {
        System.out.println("testGeneticCodeException");
        List<Codon> codons = new ArrayList<Codon>();
        try {
            GeneticCode testCode = new GeneticCode(-1, null, codons); //code that won't work
            fail();
        } catch (MalformedGeneticCodeException ex) {
            assertTrue(ex.getMessage().equals(ResourceBundle.getBundle(
                    "de/cebitec/common/sequencetools/Bundle").getString("GeneticCode.Error")));
        }
    }
}
