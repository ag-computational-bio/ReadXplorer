package de.cebitec.readXplorer.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class containing helper methods around TopComponents.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TopComponentHelper {
    
    /**
     * Fetches the first <cc>TopComponent</cc>, which is currently visible on 
     * the screen and of the given subclass instance handed over to the method.
     * @param <T> Class type of the TopComponent
     * @param activeTopCompToGet the specific subclass of <cc>TopComponent</cc>
     * which is desired.
     * @return The first <cc>TopComponent</cc>, which is currently visible on 
     * the screen and of the given subclass instance handed over to the method.
     */
    @SuppressWarnings("unchecked")
    public static <T>T getActiveTopComp(Class<? extends TopComponent> activeTopCompToGet) {
        //Get all open Components and filter for AppPanelTopComponent
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        T desiredTopComp = null;
        for (Iterator<TopComponent> it = topComps.iterator(); it.hasNext();) {
            TopComponent topComponent = it.next();
            if (topComponent.getClass().isAssignableFrom(activeTopCompToGet) && topComponent.isShowing()) {
                desiredTopComp = (T) topComponent;
                break;
            }
        }
        return desiredTopComp;
    }
    
    /**
     * Fetches all <cc>TopComponent</cc>s of the given subclass instance  handed 
     * over to the method, which are currently available.
     * @param <T> Class type of the TopComponent
     * @param topCompClassToGet the specific subclass of <cc>TopComponent</cc>
     * which is desired.
     * @return All <cc>TopComponent</cc>s of the given subclass instance  handed 
     * over to the method, which are currently available.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> List<T> getTopComps(Class<? extends TopComponent> topCompClassToGet) {
        List<T> topCompsOfClass = new ArrayList<>();
        //Get all open Components and filter for AppPanelTopComponent
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        for (Iterator<TopComponent> it = topComps.iterator(); it.hasNext();) {
            TopComponent topComponent = it.next();
            if (topComponent.getClass().isAssignableFrom(topCompClassToGet)) {
                topCompsOfClass.add((T) topComponent);
            }
        }
        return topCompsOfClass;
    }
    
    /**
     * @return The array of all currently opened TopComponents.
     */
    public static TopComponent[] getAllOpenedTopComponents() {
        Set<TopComponent> topComps = WindowManager.getDefault().getRegistry().getOpened();
        TopComponent[] topCompArray = new TopComponent[topComps.size()];
        return topComps.toArray(topCompArray);
    }
    
}
