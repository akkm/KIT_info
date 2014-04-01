package com.akkuma.kitinfo.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileUtils {
    
    private static boolean dontSave = false;

    public static void writeObjectToFile(Object object, String filename) {
        if (dontSave) {
            return;
        }

        ObjectOutputStream objectOut = null;
        try {

            FileOutputStream fileOut = new FileOutputStream(filename);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {}
            }
        }
    }

    public static Object readObjectFromFile(String filename) {

        ObjectInputStream objectIn = null;
        Object object = null;
        try {

            FileInputStream fileIn = new FileInputStream(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {}
            }
        }

        return object;
    }

    public static boolean isDontSave() {
        return dontSave;
    }

    public static void setDontSave(boolean dontSave) {
        FileUtils.dontSave = dontSave;
    }
    
    
}
