package de.cebitec.vamp.differentialExpression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kstaderm
 */
public class ProcessingLog {

    private static ProcessingLog instance;
    private StringBuilder gnuRprocessing = new StringBuilder();
    private Map<String, Object> properties = null;

    private ProcessingLog() {
    }

    public static ProcessingLog getInstance() {
        if (instance == null) {
            instance = new ProcessingLog();
        }
        return instance;
    }

    public synchronized void logGNURoutput(String output) {
        gnuRprocessing.append(output);
    }

    public synchronized void setProperties(Map<String, Object> properties) {
        if (this.properties == null) {
            this.properties = properties;
        }
    }
    
    public synchronized void addProperty(String key, Object value){
        properties.put(key, value);
    }

    public synchronized void resetLog() {
        properties = null;
        gnuRprocessing = new StringBuilder();
    }

    public String generateLog() {
        StringBuilder log = new StringBuilder();
        Set<String> keys = properties.keySet();
        Map<String, Object> tmpProperties = new HashMap<>(properties);

        for (Iterator<String> it = keys.iterator(); it.hasNext();) {
            String key = it.next();
            if (!key.startsWith("WizardPanel")) {
                Object currentProperty = tmpProperties.get(key);
                if(currentProperty instanceof Map){
                    Map currentPropertyMap = (Map) currentProperty;
                    log.append(key).append(" = ").append(Arrays.deepToString(currentPropertyMap.values().toArray())).append(System.getProperty("line.separator"));
                } else {
                    log.append(key).append(" = ").append(currentProperty.toString()).append(System.getProperty("line.separator"));
                }                
            }
        }
        log.append(System.getProperty("line.separator"));
        log.append("GNU R output during processing: ");
        log.append(System.getProperty("line.separator"));

        log.append(gnuRprocessing);

        return log.toString();
    }
}
