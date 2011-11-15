package de.cebitec.vamp.tools.rnaFolder.rnamovies.actions;

import de.cebitec.vamp.tools.rnaFolder.rnamovies.MoviePane;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

//import de.cebitec.vamp.tools.rnaFolder.rnamovies.RNAMovies;

public abstract class MovieAction extends AbstractAction {

  //protected RNAMovies movies;
    protected MoviePane movies;

  public MovieAction(String name, String iconName) {
    super(name, loadIcon(iconName, name));
  }

  private static ImageIcon loadIcon(String name, String description) {
    URL imageURL = MovieAction.class.getResource("icons/" + name.replaceAll(" ", "_")+ ".png");
    ImageIcon icon = null;
    if (imageURL!= null) {
            icon = new ImageIcon(imageURL, description);
    }

    return icon;
  }
}
