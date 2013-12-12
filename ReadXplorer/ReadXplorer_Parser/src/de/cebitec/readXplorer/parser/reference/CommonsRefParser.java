package de.cebitec.readXplorer.parser.reference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains common methods for reference parsers.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class CommonsRefParser {
    
    private CommonsRefParser() {
        
    }
    
    /**
     * Generates a mapping of the "toString()" result of each item in the given list
     * to the item itself.
     * @param mapToTransform the list of items to transform into a map with the
     * toString key and the item as value
     * @return the map of the toString result to the item
     */
    public static <E> Map<String, E> generateStringMap(Collection<E> mapToTransform) {
        Map<String, E> stringMap = new HashMap<>();
        for (Iterator<E> itemIt = mapToTransform.iterator(); itemIt.hasNext();) {
            E item = itemIt.next();
            stringMap.put(item.toString(), item);
        }
        return stringMap;
    }
}
