package de.cebitec.vamp.differentialExpression;

import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kai
 */
public class DeSeqAnalysisHandlerTest implements de.cebitec.vamp.util.Observer {

    private DeSeqAnalysisHandler instance;
    private static final int numberOfTracks = 4;
    private static List<PersistantTrack> selectedTraks;
    private static Map<String, String[]> MultiFactorDesign;
    private static Map<String, String[]> TwoFactorDesign;

    public DeSeqAnalysisHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        AnalysisHandler.TESTING_MODE = true;
        selectedTraks = new ArrayList<>();
        for (int i = 0; i < numberOfTracks; i++) {
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            selectedTraks.add(new PersistantTrack(i, "", "track" + i, currentTimestamp, 0, 0));
        }

        MultiFactorDesign = new HashMap<>();
        String[] value1 = {"a", "a", "b", "b"};
        MultiFactorDesign.put("Eins", value1);
        String[] value2 = {"c", "c", "d", "d"};
        MultiFactorDesign.put("Zwei", value2);
        String[] value3 = {"e", "e", "f", "f"};
        MultiFactorDesign.put("Drei", value3);

        TwoFactorDesign = new HashMap<>();
        String[] value = {"untreated", "untreated", "treated", "treated"};
        TwoFactorDesign.put("condition", value);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of performAnalysis method, of class DeSeqAnalysisHandler.
     */
    @Test
    public void testPerformAnalysis() {
        System.out.println("perform single analysis");
        instance = new DeSeqAnalysisHandler(selectedTraks, TwoFactorDesign, false, null, null, 0, true, null);
        instance.registerObserver(this);
        instance.performAnalysis();
        instance.endAnalysis();

        System.out.println("perform multi analysis");
        instance = new DeSeqAnalysisHandler(selectedTraks, MultiFactorDesign, true, null, null, 0, true, null);
        instance.registerObserver(this);
        instance.performAnalysis();
        instance.endAnalysis();
    }

    @Override
    public void update(Object args) {
        List<DeSeqAnalysisHandler.Result> res = instance.getResults();
        for (Iterator<AnalysisHandler.Result> it = res.iterator(); it.hasNext();) {
            AnalysisHandler.Result result = it.next();
            result.getTableContents();
            result.getColnames();
            result.getRownames();
            System.out.println("Ergebnis");
        }
    }
}
