
package de.cebitec.centrallookup;


import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;


/**
 * A lookup, that represents a project. Within a project data can be put into it
 * and removed from it.
 * When a selected project changes, the lookup for the new project replaces the
 * old one.
 * The project lookup is associated to the global lookup through a custom
 * implementation of
 * ContextGlobalProvider.
 *
 * @see GlobalActionContextImpl
 * @author ljelonek
 */
public class ProjectLookup extends AbstractLookup implements Lookup.Provider {

    private static final long serialVersionUID = 8835898;

    /**
     * A ProjectLookup implementation that is empty and does not take
     * objects.
     * <p>
     * @see Lookup.EMPTY
     */
    private static final EmptyProjectLookup EMPTY = new EmptyProjectLookup();

    private static ProjectLookup pl;

    private InstanceContent content;
    /**
     * registry to work with
     */
    private TopComponent.Registry registry;


    public ProjectLookup() {

        this( new InstanceContent() );

        registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(new PropertyChangeListener() {

            /**
             * Requests refresh of our lookup everytime component is changed.
             */
            @Override
            public void propertyChange( java.beans.PropertyChangeEvent evt ) {

                if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {

                    TopComponent tc = registry.getActivated();
                    if( tc instanceof ProjectLookup.Provider ) {
                        pl = ((ProjectLookup.Provider) registry.getActivated()).getProjectLookup();
                    }
                    Utilities.actionsGlobalContext().lookup( javax.swing.ActionMap.class );
                }

                if( TopComponent.Registry.PROP_TC_CLOSED.equals( evt.getPropertyName() ) ) {
                    TopComponent tc = (TopComponent) evt.getNewValue();
                    if( tc instanceof ProjectLookup.Provider ) {
                        pl = null;
                    }
                    Utilities.actionsGlobalContext().lookup( javax.swing.ActionMap.class );
                }

            }


        } );

    }


    public ProjectLookup( InstanceContent content ) {
        super( content );
        this.content = content;
    }


    /**
     * Adds an instance to the lookup.
     * <p>
     * @param instance
     */
    public void add( Object instance ) {
        content.add( instance );
    }


    /**
     * Removes an instance from the lookup.
     * <p>
     * @param instance
     */
    public void remove( Object instance ) {
        content.remove( instance );
    }


    /**
     * Removes all instances of clazz from the lookup and adds instance to the
     * lookup.
     * <p>
     * @param <T>
     * @param clazz
     * @param instance
     */
    public <T> void replace( Class<T> clazz, Object instance ) {
        Collection<?> c = lookupAll( clazz );
        for( Object o : c ) {
            remove( o );
        }
        add( instance );
    }


    /**
     * Returns the ProjectLookup for the currently selected project. If
     * no selected project is selected an empty lookup is returned.
     *
     * @return A lookup that holds the current project state.
     */
    public static ProjectLookup getCurrent() {
        return pl == null ? EMPTY : pl;
    }


    @Override
    public Lookup getLookup() {
        return getCurrent();
    }



    
    /**
     * TopComponents that implement Provider share their ProjectLookup with the
     * global lookup.
     */
    public static interface Provider {

        ProjectLookup getProjectLookup();

    }


    private static class EmptyProjectLookup extends ProjectLookup {

        @Override
        public void add( Object instance ) {
        }


        @Override
        public void remove( Object instance ) {
        }


        @Override
        public <T> Result<T> lookupResult( Class<T> clazz ) {

            return new Result<T>() {

                private static final long serialVersionUID = 8835899;

                @Override
                public void addLookupListener( LookupListener l ) {
                }

                @Override
                public void removeLookupListener( LookupListener l ) {
                }

                @Override
                public Collection<? extends T> allInstances() {
                    return Collections.emptyList();
                }

            };

        }

    }

}
