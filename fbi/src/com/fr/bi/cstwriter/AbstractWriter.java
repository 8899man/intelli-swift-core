package com.fr.bi.cstwriter;



import com.fr.bi.stable.utils.file.BIFileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by 小灰灰 on 2015/10/26.
 */
public abstract class AbstractWriter {

    public void write(File file,  String suffix, Class[] cls) throws Exception{
        writeFile(file, suffix, cls);
    }

    private void writeFile(File f, String suf, Class[] cls) throws Exception{
        BIFileUtils.createFile(f);
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        for (Class c : cls){
            if(c.isEnum()){
                writeEnum(writer, c, suf);
            }else{
                writeClass(writer, c, suf);
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private void writeEnum(BufferedWriter writer, Class c, String suf) throws IOException, IllegalAccessException {
        writer.write(suf + getSub() + c.getSimpleName().toUpperCase() + " = " + "{};");
        writer.newLine();
        for (Field f :c.getDeclaredFields()){
            try{
                writeFieldValue(writer, f, suf +getSub() + c.getSimpleName().toUpperCase());
                writer.newLine();
            } catch (Exception e){

            }
        }
    }

    private void writeClass(BufferedWriter writer, Class c, String suf) throws IOException, IllegalAccessException {
        for (Class cc : c.getClasses()){
            writeClassValue(writer, cc, suf);
            writeClass(writer, cc, suf +getSub() + cc.getSimpleName());
        }
        for (Field f :c.getDeclaredFields()){
            try{
                writeFieldValue(writer, f, suf);
                writer.newLine();
            } catch (Exception e){

            }
        }
    }

    protected abstract String getSub();

    protected abstract void writeClassValue(BufferedWriter writer, Class c, String suf) throws IOException;

    protected abstract void writeFieldValue(BufferedWriter writer, Field f, String suf) throws Exception;
}