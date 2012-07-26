package de.cebitec.vamp.externalSort;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.junit.*;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ExternalSortBAMReadSeqTest {
    
    public ExternalSortBAMReadSeqTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testExternalSort() {
        System.out.println("externalSort bam by read seq");
        String origPath = "E:\\Programmieren & Studieren\\Vamp\\Burkhard\\454Contigs.bam";
        File inputFile = new File(origPath);
        ExternalSortBAM sort = new ExternalSortBamReadSeq(inputFile);
        
        SAMFileReader inputReader = new SAMFileReader(new File(origPath));
        SAMFileReader sortedReader = new SAMFileReader(sort.getSortedFile());
        SAMRecordIterator inputItor = inputReader.iterator();
        SAMRecordIterator sortedItor = sortedReader.iterator();

        List<SAMRecord> inputRecords = new ArrayList<SAMRecord>();
        
        SAMRecord inputRecord;
        SAMRecord sortedRecord;
        while (inputItor.hasNext()) {
            inputRecord = inputItor.next();
            inputRecords.add(inputRecord);
        }
        
        while (sortedItor.hasNext()) {
            sortedRecord = sortedItor.next();
            if (!inputRecords.contains(sortedRecord)) {
                inputItor.close();
                inputReader.close();
                sortedItor.close();
                sortedReader.close();
                Assert.fail("The original file does not contain the record with this data:" 
                        + "Cigar: " + sortedRecord.getCigarString() 
                        + ", Readname: " + sortedRecord.getReadName()
                        + ", Reference: " + sortedRecord.getReferenceName()
                        + ", Start: " + sortedRecord.getAlignmentStart()
                        + ", Stop: " + sortedRecord.getAlignmentEnd()
                        + ", ReadSeq: " + sortedRecord.getReadString()
                        + ", Mapping Quality: " + sortedRecord.getMappingQuality()
                        + ", Strand: " + sortedRecord.getMateNegativeStrandFlag()
                        + ", Unmapped: " + sortedRecord.getReadUnmappedFlag()
                        + ", Reference Index: " + sortedRecord.getReferenceIndex()
                        + ", Base Quality: " + sortedRecord.getBaseQualityString());
            } else {
               inputRecords.remove(sortedRecord); 
            }
        }
        
        if (!inputRecords.isEmpty()) {
            inputItor.close();
            inputReader.close();
            sortedItor.close();
            sortedReader.close();
            Assert.fail("There are more records in the input, than in the output file:" + inputRecords.size());
        }
        
        inputItor.close();
        inputReader.close();
        sortedItor.close();
        sortedReader.close();
        
        Assert.assertTrue(true);
       
    }
}
