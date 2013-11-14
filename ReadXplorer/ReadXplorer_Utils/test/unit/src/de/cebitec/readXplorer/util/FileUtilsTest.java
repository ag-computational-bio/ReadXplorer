package de.cebitec.readXplorer.util;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class FileUtilsTest {
    
    public FileUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    
    @Test
    public void testGetFilePathWithoutExtension() {
        assertEquals("/var/lib/samfile", FileUtils.getFilePathWithoutExtension(new File("/var/lib/samfile.sam")));
        assertEquals("/var/lib/samfile.redo", FileUtils.getFilePathWithoutExtension(new File("/var/lib/samfile.redo.sam")));
    }
}