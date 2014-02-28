//package de.cebitec.readXplorer.util;
//
//import java.util.HashMap;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
///**
// *
// * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
// */
//public class GeneralUtilsTest {
//    
//    public GeneralUtilsTest() {
//    }
//
//    /**
//     * Test of calculatePercentageIncrease method, of class GeneralUtils.
//     */
//    @Test
//    public void testCalculatePercentageIncrease() {
//    }
//
//    /**
//     * Test of getClipboardContents method, of class GeneralUtils.
//     */
//    @Test
//    public void testGetClipboardContents() {
//    }
//
//    /**
//     * Test of isValidPositiveNumberInput method, of class GeneralUtils.
//     */
//    @Test
//    public void testIsValidPositiveNumberInput() {
//    }
//
//    /**
//     * Test of isValidNumberInput method, of class GeneralUtils.
//     */
//    @Test
//    public void testIsValidNumberInput() {
//    }
//
//    /**
//     * Test of isValidPercentage method, of class GeneralUtils.
//     */
//    @Test
//    public void testIsValidPercentage() {
//    }
//
//    /**
//     * Test of getTime method, of class GeneralUtils.
//     */
//    @Test
//    public void testGetTime() {
//    }
//
//    /**
//     * Test of generateConcatenatedString method, of class GeneralUtils.
//     */
//    @Test
//    public void testGenerateConcatenatedString() {
//    }
//
//    /**
//     * Test of deleteOldWorkFile method, of class GeneralUtils.
//     */
//    @Test
//    public void testDeleteOldWorkFile() throws Exception {
//    }
//
//    /**
//     * Test of implode method, of class GeneralUtils.
//     */
//    @Test
//    public void testImplode() {
//    }
//
//    /**
//     * Test of implodeMap method, of class GeneralUtils.
//     */
//    @Test
//    public void testImplodeMap() {
//    }
//
//    /**
//     * Test of convertNumber method, of class GeneralUtils.
//     */
//    @Test
//    public void testConvertNumber() {
//    }
//
//    /**
//     * Test of formatNumber method, of class GeneralUtils.
//     */
//    @Test
//    public void testFormatNumber_Integer() {
//    }
//
//    /**
//     * Test of formatNumber method, of class GeneralUtils.
//     */
//    @Test
//    public void testFormatNumber_Long() {
//    }
//
//    /**
//     * Test of enshortenReadName method, of class GeneralUtils.
//     */
//    @Test
//    public void testEnshortenReadName() {
//    }
//
//    /**
//     * Test of escapeHtml method, of class GeneralUtils.
//     */
//    @Test
//    public void testEscapeHtml() {
//    }
//
//    /**
//     * Test of generateStringMap method, of class GeneralUtils.
//     */
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testGenerateStringMap() {
//        String readName = "HWI-ST486_0090:5:1101:17454:23711#ACTTGA/1";
//        String readName2 = "HWI-ST486_0090:5:1101:17454:23712#ACTTGA/1";
//        String readName3 = "HWI-ST486_0090:5:1101:17454:23713#ACTTGA/1";
//        String[] nameParts = GeneralUtils.splitReadName(readName, GeneralUtils.NameStyle.STYLE_ILLUMINA);
//        String[] nameParts2 = GeneralUtils.splitReadName(readName2, GeneralUtils.NameStyle.STYLE_ILLUMINA);
//        String[] nameParts3 = GeneralUtils.splitReadName(readName3, GeneralUtils.NameStyle.STYLE_ILLUMINA);
//        String[] nameParts4 = GeneralUtils.splitReadName(readName, GeneralUtils.NameStyle.STYLE_STANDARD);
//        String[] nameParts5 = GeneralUtils.splitReadName(readName2, GeneralUtils.NameStyle.STYLE_STANDARD);
//        String[] nameParts6 = GeneralUtils.splitReadName(readName3, GeneralUtils.NameStyle.STYLE_STANDARD);
//        
//        HashMap<String, Object> resultMap = GeneralUtils.generateStringMap(new HashMap<String, Object>(), nameParts, 1);
//        resultMap = GeneralUtils.generateStringMap(resultMap, nameParts2, 1);
//        resultMap = GeneralUtils.generateStringMap(resultMap, nameParts3, 1);
//        resultMap = GeneralUtils.generateStringMap(resultMap, nameParts4, 1);
//        resultMap = GeneralUtils.generateStringMap(resultMap, nameParts5, 1);
//        resultMap = GeneralUtils.generateStringMap(resultMap, nameParts6, 1);
//        assertTrue(resultMap.containsKey("HWI-ST486_0090"));
//        HashMap<String, Object> subMap = (HashMap<String, Object>) resultMap.get("HWI-ST486_0090");
//        assertTrue(subMap.containsKey("5"));
//        assertTrue(subMap.size() == 1);
//        HashMap<String, Object> subMap2 = (HashMap<String, Object>) subMap.get("5");
//        assertTrue(subMap2.containsKey("1101"));
//        assertTrue(subMap2.size() == 1);
//        HashMap<String, Object> subMap3 = (HashMap<String, Object>) subMap2.get("1101");
//        assertTrue(subMap3.containsKey("17454"));
//        assertTrue(subMap3.size() == 1);
//        HashMap<String, Object> subMap4 = (HashMap<String, Object>) subMap3.get("17454");
//        assertTrue(subMap4.containsKey("23711"));
//        assertTrue(subMap4.containsKey("23712"));
//        assertTrue(subMap4.containsKey("23713"));
//        assertTrue(subMap4.size() == 3);
//        
//    }
//    
//}
