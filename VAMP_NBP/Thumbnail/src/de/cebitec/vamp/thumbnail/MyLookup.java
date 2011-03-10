/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.thumbnail;

import de.cebitec.centrallookup.CentralLookup;
import java.util.Collection;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author denis
 */
public class MyLookup extends AbstractLookup {

	private InstanceContent content = null;
	private static MyLookup def = new MyLookup();

	public MyLookup(InstanceContent content) {
		super(content);
		this.content = content;
	}

	public MyLookup() {
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

	public static MyLookup getDefault() {
		return def;
	}
}