package com.tccsafeo.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {
    public static String readFileAsString(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(getFilePath(fileName)));
        String response = new String();
        for (String line; (line = br.readLine()) != null; response += line) ;
        return response;
    }

    public static String getFilePath(String fileName) {
        String currentDir = System.getProperty("user.dir");
        return currentDir + "/" + fileName;
    }
}
