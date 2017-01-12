/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sec;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author KOCMOC
 */
public class HelpM {

    public static final URL IMAGE_ICON_URL = HelpM.class.getResource("icon.png");

    public static boolean runningInNetBeans(String path) {
        File f = new File(path);
        if (f.exists() == false) {
            return true;
        } else {
            return false;
        }
    }

    public static void err_output_to_file() {
        create_dir_if_missing("err_output");
        try {
            String err_file = "err_" + get_date_time() + ".txt";
            String output_path = "err_output/" + err_file;
            PrintStream out = new PrintStream(new FileOutputStream(output_path));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void create_dir_if_missing(String path_and_folder_name) {
        File f = new File(path_and_folder_name);
        if (f.exists() == false) {
            f.mkdir();
        }
    }

    public static String get_date_time() {
        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }

    public static String get_proper_date_time_same_format_on_all_computers() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }

    public static void run_application_jar_with_argument(String application_to_run_name, String arg, String path) throws IOException {
        String[] commands = new String[4];
        if (application_to_run_name.contains(".jar")) {
            commands[0] = "java";
            commands[1] = "-jar";
            commands[2] = application_to_run_name; //OBS! pay attention here
            commands[3] = arg;
        }
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(new File(path));
        builder.start();
    }

    public static void open_dir(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static HashMap restoreObjectFromFile(String fileName) {
        try {
            Object obj = fileToObject(fileName);
            HashMap map = (HashMap) obj;
            return map;
        } catch (Exception ex) {
            return new HashMap();
        }
    }

    public static void objectToFile(String path, Object obj) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Object fileToObject(String path) throws IOException, ClassNotFoundException {
        FileInputStream fas = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fas);
        Object obj = ois.readObject();
        return obj;
    }

    public static Properties properties_load_properties(String path_andOr_fileName, boolean list_properties) {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(path_andOr_fileName));
            if (list_properties == true) {
                p.list(System.out);
            }
        } catch (IOException ex) {
            System.out.println("" + ex);
        }
        return p;
    }
}
