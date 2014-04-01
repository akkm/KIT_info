package com.akkuma.kitinfo.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FileUtils {
    
    private static boolean dontSave = false;
    
    public static void write(Object obj, String filename, Type type) {
        if (dontSave) {
            return;
        }
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("  ");
            Gson gson = new Gson();
            gson.toJson(obj, type, jsonWriter);
            jsonWriter.flush();
            jsonWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static Object read(String filename, Type type) {
        Object obj = null;
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            JsonReader jsonReader = new JsonReader(reader);
            Gson gson = new Gson();
            obj = gson.fromJson(jsonReader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static boolean isDontSave() {
        return dontSave;
    }

    public static void setDontSave(boolean dontSave) {
        FileUtils.dontSave = dontSave;
    }
    
    
}
