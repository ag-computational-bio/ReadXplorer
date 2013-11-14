/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.util;

/**
 * This is a simple interface to define an abstract output for displaying
 * normal and error messages to the user
 * @author Evgeny Anisiforov
 */
public interface SimpleOutput {
    public void showMessage(String s);
    public void showError(String s);
}
