package ru.alastar;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Alastar on 24.06.2015.
 */
public class BinaryManager {

    public HashMap<String, Housing> loadDB() {
        try {

            File db_file = new File("hdb.bin");
            if (db_file.exists()) {
                return (HashMap<String, Housing>) SLAPI.load("hdb.bin");
            } else {
                createDB();
                loadDB();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createDB() throws IOException {
        File fdb = new File("hdb.bin");
        fdb.createNewFile();
        try {
            SLAPI.save(new HashMap<String, Housing>(), "hdb.bin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDB(HashMap<String, Housing> housings) {
        try {
            SLAPI.save(housings, "hdb.bin");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dumpDB(HashMap<String, Housing> housings) {
        try {
            File dir = new File("dumps");
            if (!dir.exists()) {
                dir.mkdir();
            }
            SLAPI.save(housings, "dump-" + Calendar.getInstance().getTime().toString() + ".bin");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
