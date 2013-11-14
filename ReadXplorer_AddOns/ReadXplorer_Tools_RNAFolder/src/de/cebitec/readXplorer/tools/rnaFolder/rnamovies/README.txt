RNA Movies 2.04
(available at http://bibiserv.techfak.uni-bielefeld.de/rnamovies/)

  RNA Movies is free software. Pleas see LICENSE for details.
  This file only describes how to build RNA Movies. For further information on
  how the program is to be used please refere to
  http://bibiserv.techfak.uni-bielefeld.de/rnamovies/manual.html

  1. System requirements
  RNA Movies 2.04 needs at least j2se v1.5.0 (available at
  http://java.sun.com/) to run properly.

  For building RNA Movies you will need Apache Ant > 1.6 (available at
  http://ant.apache.org/).

  RNA Movies should be able to run on any modern platform that ist supported
  by j2se (see http://java.sun.com/j2se/1.5.0/system-configurations.html).

  2. Files and directories
  the archive should contain the following files and directories:

    RNAMovies2.04/              top-level directory
    RNAMovies2.04/LICENSE
    RNAMovies2.04/README
    RNAMovies2.04/build.xml     ant build-file
    RNAMovies2.04/lib/          external libraries needed by RNA Movies
    RNAMovies2.04/sample/       some nice samples
    RNAMovies2.04/src/          source tree

  after executing 'ant' in the top-level directory it should look like the
  following:

    RNAMovies2.04/              top-level directory
    RNAMovies2.04/LICENSE
    RNAMovies2.04/README
    RNAMovies2.04/bin/          classes
    RNAMovies2.04/build.xml     ant build-file
    RNAMovies2.04/dist/         rnamovies.jar is stored here
    RNAMovies2.04/doc/          java-doc results are stored here
    RNAMovies2.04/lib/          external libraries needed by RNA Movies 
    RNAMovies2.04/sample/       some nice samples
    RNAMovies2.04/src/          source tree

  3. Building RNA Movies
  Building RNA Movies is quiet simple: just execute ant inside the top-level
  directory of RNA Movies that contains the file build.xml. If you want to run
  RNA Movies from its directory use 'ant run'.
  The following tags are also understood:

    ant dist      
              creates rna-movies.jar and puts it into the dist/ directory
    ant javadoc
              generates the java-doc and puts it into the doc/ directory
    ant clean
              removes the directories bin/ dist/ and doc/

  4. Installation
  If you want to use RNA Movies permanently, copy the rnamovies.jar file to
  any location on your hardrive. On Mac and Windows platforms you may execute
  RNA Movies by double-clicking on the jar-file. On UNIX platforms you may
  copy rnamovies.jar into your existing hirarchy e.g /usr/local/lib/ and write
  an apropriate wrapper script to start it.

  5. Copyright and references
  
  All java programming done by
    Alexander Kaiser <akaiser@techfak.uni-bielefeld.de>
    Jan Krüger <jkrueger@techfak.uni-bielefeld.de> (xml import and animated gif export) 

  References:

    Robert Giegerich, Dirk J. Evers
      RNA Movies: visualizing RNA secondary structure spaces
      Bioinformatics (formerly CABIOS), Volume 15, Issue 1, January 1999,
      pp. 32-37, OUP Press

    Robert E. Bruccoleri, Gerhard Heinrich
      An improved algorithm for nucleic acid secondary structure display
      CABIOS, Volume 4, Issue 1, 1988, pp. 167-173, IRL Press
