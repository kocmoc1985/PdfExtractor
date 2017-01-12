/*
 * To change this template, choose Tools | Templates
 * and openProperties the template in the editor.
 */
package main;

import com.jezhumble.javasysmon.JavaSysMon;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import sec.HelpM;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import sec.SimpleLoggerLight;

/**
 *
 * @author KOCMOC
 */
public class SriLankaPdfWatcher implements Runnable {

    private static final Properties p = HelpM.properties_load_properties("main.properties", false);
    private static final String UPDATE_MAP_PATH = "UPDATE_MAP";
    private static final String SRC_FOLDER = p.getProperty("src_folder", "c:/test"); // 
    public static final String DEST_FOLDER = p.getProperty("dest_folder", SRC_FOLDER); //
    public static final String LOG_FILE = "log.txt"; //
    private HashMap<String, Long> updateMap;
    private boolean RUN = true;
    //
    private Image img = null;
    private PopupMenu popup;
    private MenuItem exit;
    private MenuItem openProperties;
    private MenuItem rebuildAll;
    private MenuItem srcFolder;
    private MenuItem destFolder;
    private MenuItem restart;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private JavaSysMon monitor = new JavaSysMon();
    private int PID = monitor.currentPid();
    //
    private PdfExtractorSriLanka pdfExtractor = new PdfExtractorSriLanka(this);

    public SriLankaPdfWatcher() {
        initializeUpdateMap();
        toTray();
        startThread();
        SimpleLoggerLight.logg(LOG_FILE, "Started, pid: " + PID);
    }

    private void initializeUpdateMap() {
        updateMap = HelpM.restoreObjectFromFile(UPDATE_MAP_PATH);
    }

    private void startThread() {
        Thread x = new Thread(this);
        x.start();
    }

    public void displayTrayIconErrorMsg(String msg) {
        trayIcon.displayMessage("MCPdfExtractor", msg, TrayIcon.MessageType.ERROR);
    }

    private void go() {
        File[] f = new File(SRC_FOLDER).listFiles();

        if (f == null) {
            displayTrayIconErrorMsg("Source folder not found");
            SimpleLoggerLight.logg(LOG_FILE, "Source folder missing: " + SRC_FOLDER);
            stopThread();
            return;
        }

        for (File file : f) {
            if (file.isDirectory()) {
                continue;
            }
            //
            String path = file.getAbsolutePath();
            long lastModified = file.lastModified();
            //
            if (PdfExtractorSriLanka.isPdfFile(path)) { //&& PdfExtractorSriLanka.verifyIfSequencePdfFile(path)
                //
                if (updateMap.containsKey(path) == false) {
                    pdfExtractor.extractFromPdfAndWriteToFile(file, DEST_FOLDER);
                    updateMapAndSave(path, lastModified);
                } else {
                    if (updateMap.get(path) < lastModified) {
                        pdfExtractor.extractFromPdfAndWriteToFile(file, DEST_FOLDER);
                        updateMapAndSave(path, lastModified);
                    }
                }
                //
            }
        }
    }

    private void updateMapAndSave(String path, long lastModified) {
        //
        if (pdfExtractor.DEST_FOLDER_MISSING) {
            return;
        }
        //
        updateMap.put(path, lastModified);
        HelpM.objectToFile(UPDATE_MAP_PATH, updateMap);
    }

    private void resetUpdateMap() {
        updateMap = new HashMap<String, Long>();
        HelpM.objectToFile(UPDATE_MAP_PATH, updateMap);
    }

    public static void main(String[] args) {
        //
        final boolean runInNetbeans = HelpM.runningInNetBeans("PdfExtractor.jar");
        //
        if (runInNetbeans == false) {
            HelpM.err_output_to_file();
        }
        SriLankaPdfWatcher slpw = new SriLankaPdfWatcher();
    }

    public void stopThread() {
        RUN = false;
        trayIcon.setToolTip("Program stopped due to error (" + PID + ")");
    }

    @Override
    public void run() {
        while (RUN) {
            go();
            wait_(30000);
        }
    }

    private synchronized void wait_(int millis) {
        try {
            wait(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(SriLankaPdfWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void toTray() {
        if (SystemTray.isSupported()) {

            tray = SystemTray.getSystemTray();
            img = new ImageIcon(HelpM.IMAGE_ICON_URL).getImage();

            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == exit) {
                        System.exit(0);
                    } else if (e.getSource() == openProperties) {
                        try {
                            HelpM.run_application_jar_with_argument("PropertiesReader.jar", "", ".");
                        } catch (IOException ex) {
                            Logger.getLogger(SriLankaPdfWatcher.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (e.getSource() == rebuildAll) {
                        resetUpdateMap();
                    } else if (e.getSource() == srcFolder) {
                        HelpM.open_dir(SRC_FOLDER);
                    } else if (e.getSource() == destFolder) {
                        HelpM.open_dir(DEST_FOLDER);
                    } else if (e.getSource() == restart) {
                        try {
                            HelpM.run_application_jar_with_argument("restarter.jar", "PdfExtractor.jar", ".");
                            System.exit(0);
                        } catch (IOException ex) {
                            Logger.getLogger(SriLankaPdfWatcher.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
            popup = new PopupMenu();

            exit = new MenuItem("EXIT");
            openProperties = new MenuItem("Settings");
            rebuildAll = new MenuItem("Rebuild all");
            srcFolder = new MenuItem("Open src folder");
            destFolder = new MenuItem("Open dest folder");
            restart = new MenuItem("Restart");

            exit.addActionListener(actionListener);
            openProperties.addActionListener(actionListener);
            rebuildAll.addActionListener(actionListener);
            srcFolder.addActionListener(actionListener);
            destFolder.addActionListener(actionListener);
            restart.addActionListener(actionListener);

            popup.add(exit);
            popup.add(restart);
            popup.add(openProperties);
            popup.add(rebuildAll);
            popup.add(srcFolder);
            popup.add(destFolder);

            trayIcon = new TrayIcon(img, "MCPdfExtractor (" + PID + ")", popup);

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);

            try {
                tray.add(trayIcon);

            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {
            //  System Tray is not supported
        }
    }
}
