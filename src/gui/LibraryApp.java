package gui;

import com.mycompany.librarymanagementsystem.DatabaseSetup;
import javax.swing.SwingUtilities;

public class LibraryApp {
    public static void main(String[] args) {

        // create tables if not exist
        DatabaseSetup.createTables();

        // start GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibraryMainFrame();
            }
        });
    }
}
