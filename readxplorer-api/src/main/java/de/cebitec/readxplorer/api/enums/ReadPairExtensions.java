
package de.cebitec.readxplorer.api.enums;


/**
 * Supported read pair extensions.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum ReadPairExtensions {


    /**
     * / = separator used for read pair tags before Casava 1.8 format.
     */
    Separator( 0, '/' ),

    /**
     * 0 = For reads not having a pair tag.
     */
    Undefined( 1, '0' ),

    /**
     * 1 = Supported extension of read 1.
     */
    A1( 2, '1' ),

    /**
     * 2 = Supported extension of read 2.
     */
    A2( 3, '2' ),

    /**
     * f = Supported extension of read 1.
     */
    B1( 4, 'f' ),

    /**
     * r = Supported extension of read 2.
     */
    B2( 5, 'r' );


    private final int typeInt;
    private final char typeChar;


    private ReadPairExtensions( int typeInt, char typeChar ) {
        this.typeInt = typeInt;
        this.typeChar = typeChar;
    }


    public int getType() {
        return typeInt;
    }


    public char getChar() {
        return typeChar;
    }


    @Override
    public String toString() {
        return String.valueOf( typeChar );
    }


    public static ReadPairExtensions fromType( final int type ) {

        for( ReadPairExtensions rpExt : values() ) {
            if( rpExt.getType() == type ) {
                return rpExt;
            }
        }

        return null;

    }


    public static ReadPairExtensions fromChar( final char charType ) {

        for( ReadPairExtensions rpExt : values() ) {
            if( rpExt.getChar() == charType ) {
                return rpExt;
            }
        }

        return null;

    }




}
