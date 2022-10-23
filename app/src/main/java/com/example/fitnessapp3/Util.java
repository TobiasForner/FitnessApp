package com.example.fitnessapp3;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Util {
    public static final String WORKOUT_IN_PROGRESS_JSON="workout_in_progress.json";

    public static void writeFileOnInternalStorage(Context context, String filename, String fileContents){
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static String readFromInternal(String filename, Context context){
        FileInputStream fis;
        String contents;
        try{
        fis = context.openFileInput(filename);}
        catch(FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            contents = stringBuilder.toString().trim();
        }
        return contents;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
            v.requestLayout();
        }
    }

    public static String strip(String s){
        String result = s;
        while(result.length()>0 && result.charAt(0)==' '){
            result = result.substring(1);
        }
        while(result.length()>0 && result.charAt(result.length()-1)==' '){
            result = result.substring(0,result.length()-1);
        }
        return result;
    }


}
