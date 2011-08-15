/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.thumbnail;

import java.util.Collection;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author denis
 */
public class ThumbControllerLookup extends AbstractLookup {

	private InstanceContent content = null;
	private static ThumbControllerLookup def = new ThumbControllerLookup();

	public ThumbControllerLookup(InstanceContent content) {
		super(content);
		this.content = content;
	}

	public ThumbControllerLookup() {
		this(new InstanceContent());
	}

	public void add(Object instance) {
		content.add(instance);
	}

	public void remove(Object instance) {
		content.remove(instance);
	}

	public <T> void removeAll(Class<T> clazz) {
		Collection<? extends T> col = lookupAll(clazz);
		for (T o : col) {
			remove(o);
		}
	}

	public static ThumbControllerLookup getDefault() {
		return def;
	}
}