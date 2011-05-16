package de.cebitec.vamp.tools.rnaFolder.rnamovies.configuration;

import java.util.EventListener;

public interface ConfigListener extends EventListener {
		public void configurationChanged(ConfigChangedEvent e);
}
