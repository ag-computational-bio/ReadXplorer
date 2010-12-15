package de.cebitec.vamp.controller;

//import de.cebitec.vamp.view.ViewController;
//import de.cebitec.vamp.ui.dataAdministration.model.GestureListenerI;
//import de.cebitec.vamp.dataAdministration.RunningTaskI;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.view.LoginFrame;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author ddoppmeier
 */
public class ApplicationController /*implements GestureListenerI*/ {

    private LoginFrame login;
//    private List<RunningTaskI> runningTasks;
//    private ViewController viewController;
    private static ApplicationController appCon;
    public static String APPNAME = "VAMP Version 1.2";
    

    private ApplicationController() {
        createAndShowLogin();
//        runningTasks = new ArrayList<RunningTaskI>();

    }

    public static synchronized ApplicationController getInstance(){

        if(appCon == null){
            appCon = new ApplicationController();
        }
        return appCon;
    }

//    public void addRunningTask(RunningTaskI runningTask){
//        runningTasks.add(runningTask);
//        updateViewButtons();
//    }

//    public void removeRunningTask(RunningTaskI runningTask){
//        runningTasks.remove(runningTask);
//        updateViewButtons();
//    }

    private void updateViewButtons(){
//        viewController.blockControlsByRunningTasks(runningTasks);
    }

    private void createAndShowLogin(){
        login = new LoginFrame();
//        login.addGestureListener(this);
        login.setVisible(true);
    }

    private void dropLogin(){
        login.setVisible(false);
        login = null;
    }

    private void createAndShowApp(){
//        viewController = new ViewController();
//        viewController.addGestureListener(this);
//        viewController.showApplicationFrame(true);
    }

    private void dropApp(){
//        viewController.showApplicationFrame(false);
//        viewController = null;
    }

//    @Override
    public void login(String adapter, String hostname, String database, String user, String password) {
        try {
            ProjectConnector.getInstance().connect(adapter, hostname, database, user, password);
            dropLogin();
            createAndShowApp();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Could not connect to database!\nPlease check the data!", "Connection error", JOptionPane.ERROR_MESSAGE);
        }

            
    }

//    @Override
    public void logOff() {
        if(ProjectConnector.getInstance().isConnected()){
            ProjectConnector.getInstance().disconnect();
        }
        dropApp();
        createAndShowLogin();

    }

//    @Override
    public void shutDownApplication() {

//        if(!runningTasks.isEmpty()){
//            JOptionPane.showMessageDialog(null, "The programm cannot be stopped, as long as there are running jobs!", "Work in progress", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        if(ProjectConnector.getInstance().isConnected()){
            ProjectConnector.getInstance().disconnect();
        }
        System.exit(0);
    }


}
