/*
 * LineScanner.java
 *
 * Created on March 28, 2007, 3:36 PM
 */

package de.cebitec.readXplorer.tools.rnaFolder.rnamovies.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

/**
 *
 * @author jkrueger
 */
public class LineScanner implements Enumeration<String> {

    private BufferedReader br;
    private String next;

    public LineScanner(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in));
        readLine();
    }

    @Override
    public String nextElement() {
        String tmp;

        tmp = next;
        readLine();

        return tmp;
    }

    @Override
    public boolean hasMoreElements() {
        return(next != null);
    }

    private void readLine() {
        try {
            do {
                next = br.readLine();
            } while(next != null && next.trim().equals(""));
        } catch(IOException e) {
            next = null;
        }
    }
}



