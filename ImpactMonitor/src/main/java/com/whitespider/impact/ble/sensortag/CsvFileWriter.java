package com.whitespider.impact.ble.sensortag;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.whitespider.impact.util.Point3D;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class CsvFileWriter {

    public static final String ROOT_DIR = "ImpactMonitor";
    public static final String NEW_LINE_SEPARATOR = "\n";
    public static final String CSV_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + ROOT_DIR;
    public static final CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
    private static final Object [] FILE_HEADER = {
            "Time", "severity", "a_x","a_y","a_x", "g_x","g_y","g_z", "c_x","c_y","c_z"
    };
    private static final String TAG = "CsvFileWriter";
    private static SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    public void addConcussionEvent(MotionSensor p, byte concussionSeverity) {
        String fileName = CSV_FILE_PATH + File.separator + "data.csv";
        File f = new File(CSV_FILE_PATH);
        if(!f.exists()) {
            f.mkdirs();
        }
        File csv = new File(fileName);
        boolean isNewFile = ! csv.exists();
        FileWriter fileWriter = getFileWriter(csv);
        try {
            CSVPrinter csvFilePrinter = getCsvPrinter(fileWriter);
            try {
                if (isNewFile) {
                    csvFilePrinter.printRecord(FILE_HEADER);
                }
                final Point3D acc = p.getReading().getAccelerometer().getReading();
                final Point3D gyr = p.getReading().getGyroscope().getReading();
                final Point3D com = p.getReading().getCompass().getReading();

                csvFilePrinter.printRecord(FORMAT.format(p.getReading().getTimeOfReading()),
                        concussionSeverity,
                        acc.x , acc.y, acc.z,
                        gyr.x , gyr.y, gyr.z,
                        com.x , com.y, com.z
                );
            } catch (IOException e) {
                Log.d(TAG, "Error writing CSV data.", e);
                throw new RuntimeException("Error writing CSV data.", e);
            } finally {
                try {
                    csvFilePrinter.close();
                } catch (IOException e) {
                }
            }
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
            }
        }
    }

    @NonNull
    private CSVPrinter getCsvPrinter(FileWriter fileWriter) {
        try {
            return new CSVPrinter(fileWriter, csvFileFormat);
        } catch (IOException e) {
            Log.d(TAG, "Can't create printer.", e);
            throw new RuntimeException("Can't create printer.", e);
        }
    }

    @NonNull
    private FileWriter getFileWriter(File csv) {
        try {
            return new FileWriter(csv, true);
        } catch (IOException e) {
            Log.d(TAG, "Can't create writer.", e);
            throw new RuntimeException("Can't create writer.", e);
        }
    }
}
