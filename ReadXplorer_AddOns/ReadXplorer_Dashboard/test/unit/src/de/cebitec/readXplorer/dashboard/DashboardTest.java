/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.dashboard;

import de.cebitec.readXplorer.databackend.FieldNames;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeff
 */
public class DashboardTest {
    
    public DashboardTest() {
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
    public void testEquals() {
        PersistantReference r1 = new PersistantReference(1, "", "", "", new Timestamp(111111111));
        PersistantReference r2 = new PersistantReference(1, "", "", "", new Timestamp(111111111));
        PersistantReference r3 = new PersistantReference(2, "", "", "", new Timestamp(111111111));
        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
        
        Object o1 = (Object) r1;
        Object o2 = (Object) r2;
        Object o3 = (Object) r3;
        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(o3));
    }
    
    @Test
    public void testMap() {
        HashMap<Value,Integer> map = new HashMap<Value,Integer>();
        Value v1 = new Value();
        Value v2 = new Value();
        map.put(v1, 1);
        assertNotNull(map.get(v2));
    }
    
    @Test
    public void testMap2() {
        Hashtable<PersistantReference, List<PersistantTrack>> tracks_by_reference
               = new Hashtable<PersistantReference, List<PersistantTrack>>();
        PersistantReference r1 = new PersistantReference(1, "", "", "", new Timestamp(111111111));
        PersistantReference r2 = new PersistantReference(1, "", "", "", new Timestamp(111111111));
        List<PersistantTrack> list = new ArrayList<PersistantTrack>();
        tracks_by_reference.put(r1, list);
        assertNotNull(tracks_by_reference.get(r2));
        
    }
    
    @Test 
    public void zip() throws Exception {
        String data = "djkfdskajkj";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        GZIPOutputStream gz;
        ObjectOutputStream    oos; 
        
        gz = new GZIPOutputStream(baos);
        oos = new ObjectOutputStream( gz );
        oos.writeObject( data ); 
        oos.close(); 
        byte[] array = baos.toByteArray();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
        "created byte representation ("+array.length+" bytes)");
        
        ByteArrayInputStream bais;
        GZIPInputStream gzi;
        ObjectInputStream ins;
        bais = new ByteArrayInputStream(array);
        
        gzi = new GZIPInputStream(bais);
        ins = new ObjectInputStream(gzi);
        Object data2 = ins.readObject();
        
        assertEquals(data, data2);
    }
    
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    private class Value {
        @Override
        public int hashCode() {
            return 1;
        }
        
        @Override
        public boolean equals(Object o) {
            return true;
        }
        

        
    }
}
