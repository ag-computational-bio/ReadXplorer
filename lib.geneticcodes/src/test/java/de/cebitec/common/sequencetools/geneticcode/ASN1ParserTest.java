package de.cebitec.common.sequencetools.geneticcode;

import de.cebitec.common.sequencetools.geneticcode.ParsedASN1Entry;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import de.cebitec.common.sequencetools.geneticcode.ASN1Parser;
import de.cebitec.common.sequencetools.geneticcode.ParsedASN1Table;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rhilker
 */
public class ASN1ParserTest {

    /**
     * Test of parseData method, of class ASN1Parser.
     */
    @Test
    public void testParseData() throws IOException {
        System.out.println("parseData");

        InputStream inputStream = GeneticCodeFactory.NCBIGeneticCodeParser.class.getResourceAsStream(GeneticCodeFactory.NCBIGeneticCodeParser.GEN_CODE_LOCATION);
        assertNotNull(inputStream);

        ASN1Parser instance = new ASN1Parser();
        ParsedASN1Table result = instance.parseData(inputStream);
        assertTrue(result.getTableHeader().equals("Genetic-code-table"));
        List<ParsedASN1Entry> entries = result.getParsedEntries();

        //Determined value testing for entry 1:
        HashMap<String, List<String>> map = entries.get(0).getEntry();
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.NAME_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.NAME_FIELD).get(0).equals("Standard"));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.NAME_FIELD).get(1).equals("SGC0"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.ID_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.ID_FIELD).get(0).equals("1"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.AACID_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.AACID_FIELD).get(0).equals("FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.START_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.START_FIELD).get(0).equals("---M---------------M---------------M----------------------------"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.BASE1_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.BASE1_FIELD).get(0).equals("TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.BASE2_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.BASE2_FIELD).get(0).equals("TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG"));
        assertTrue(map.containsKey(GeneticCodeFactory.NCBIGeneticCodeParser.BASE3_FIELD));
        assertTrue(map.get(GeneticCodeFactory.NCBIGeneticCodeParser.BASE3_FIELD).get(0).equals("TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG"));

        //Rest just printed out
        for (ParsedASN1Entry entry : entries) {
            map = entry.getEntry();
            Iterator it = map.keySet().iterator();


            while (it.hasNext()) {
                String key = (String) it.next();
                System.out.println(key + ", value: " + map.get(key));
            }
        }

    }
}
