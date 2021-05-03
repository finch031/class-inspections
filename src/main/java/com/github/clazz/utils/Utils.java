package com.github.clazz.utils;

import com.github.clazz.model.JarClass;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Utils {
    private static final long MILLIS_ONE_DAY = 86400000L;

    private Utils(){
        // no instance.
    }

    public static JarClass jarClassParse(String jarPath){
        JarFile jarFile = null;

        try{
            jarFile = new JarFile(jarPath);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        JarClass jarClass = new JarClass();
        jarClass.setJarPath(jarPath);

        if(jarFile != null){
            Enumeration<JarEntry> entries =  jarFile.entries();
            List<String> canonicalNames = new ArrayList<>();
            while(entries.hasMoreElements()){
                JarEntry jarEntry = entries.nextElement();
                if(!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")){
                    String clazzStrTemp = jarEntry.getName();
                    String clazzStr = clazzStrTemp.split("\\.")[0];
                    String clazzCanonicalName = clazzStr.replaceAll("/",".");
                    canonicalNames.add(clazzCanonicalName);
                }
            }
            jarClass.setCanonicalName(canonicalNames);
        }

       return jarClass;
    }

    public static List<File> dirJarFileScan(String jarPath){
        List<File> dirJarFiles = new ArrayList<>();

        try{
            Files.walkFileTree(Paths.get(jarPath),new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    File dirFile = file.toFile();
                    if(dirFile.getName().endsWith(".jar")){
                        dirJarFiles.add(dirFile);
                    }

                    return super.visitFile(file,attrs);
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc){
                    return FileVisitResult.CONTINUE;
                }
            });

        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        return dirJarFiles;
    }

    public static List<JarClass> jarFileClassesParse(List<File> dirJarFiles){
        List<JarClass> jarClassList = new ArrayList<>();
        for (File jarFile : dirJarFiles) {
            JarClass parsedJarClass = jarClassParse(jarFile.getAbsolutePath());
            jarClassList.add(parsedJarClass);
        }
        return jarClassList;
    }

    /**
     * Call Class.forName(className), but return null if any exception is thrown.
     *
     * @param className
     *            The class name to load.
     * @return The class of the requested name, or null if an exception was thrown while trying to load the class.
     */
    public static Class<?> classForNameOrNull(final String className) {
        try {
            return Class.forName(className);
        } catch (final ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /**
     * Creates a file-protocol URL for the given file.
     */
    public static URL toURL(File file) throws MalformedURLException {
        String path = file.getAbsolutePath();

        // This is a bunch of weird code that is required to
        // make a valid URL on the Windows platform, due
        // to inconsistencies in what getAbsolutePath returns.
        String fs = System.getProperty("file.separator");
        if (fs.length() == 1) {
            char sep = fs.charAt(0);
            if (sep != '/') {
                path = path.replace(sep, '/');
            }
            if (path.charAt(0) != '/') {
                path = '/' + path;
            }
        }
        path = "file://" + path;
        return new URL(path);
    }

    public static Class<?> loadClassFromJar(File file,String clazzStr){
        Class<?> clazz = null;

        try{
            URLClassLoader urlClassLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    System.class.getClassLoader());
            clazz = urlClassLoader.loadClass(clazzStr);
        }catch (ClassNotFoundException | MalformedURLException cfe){
            // cfe.printStackTrace();
        }catch (NoClassDefFoundError nde){
            // nde.printStackTrace();
        }catch (UnsupportedClassVersionError uce){
            // uce.printStackTrace();
        }

        System.out.println("load class:" + clazzStr + "," + (clazz != null ? "success" : "failed"));

        return clazz;
    }

    /**
     * 查找指定命令行参数的索引位置.
     * @param args 命令行参数数组.
     * @param param 待查找的命令行参数.
     * @return index 参数索引位置,-1表示没有查找到.
     * */
    public static int paramIndexSearch(String[] args,String param){
        int index = -1;
        for (int i = 0; i < args.length; i++) {
            if(args[i].equalsIgnoreCase(param)){
                index = i;
                break;
            }
        }

        return index;
    }

    public static void ensureFileOrDirExistAndCanRead(final File file) throws IOException {
        if(!file.exists()){
            throw new IOException(file.getAbsolutePath() + " not exists.");
        }

        if (file.isFile() && !file.getName().endsWith(".jar")) {
            throw new IOException(file.getAbsolutePath() + " is not a jar file.");
        }

        if (!file.canRead()) {
            throw new IOException(file.getAbsolutePath() + " file or directory does not have read privilege");
        }
    }

    /**
     * Get the stack trace from an exception as a string
     */
    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Given a time expressed in milliseconds, append the time formatted as
     * "hh[:mm[:ss]]".
     *
     * @param buf    Buffer to append to
     * @param transMillis Milliseconds
     */
    public static void appendPosixTime(StringBuilder buf, long transMillis) {
        int millis = 0;

        if(transMillis > MILLIS_ONE_DAY){
            long temp = transMillis % MILLIS_ONE_DAY;
            long daysNum = (transMillis - temp) / MILLIS_ONE_DAY;
            buf.append(daysNum);
            buf.append(" days,");
            millis = (int)(transMillis - daysNum * MILLIS_ONE_DAY);
        }else{
            millis = (int)transMillis;
        }

        if (millis < 0) {
            buf.append('-');
            millis = -millis;
        }
        int hours = millis / 3600000;
        buf.append(hours);
        millis -= hours * 3600000;
        if (millis == 0) {
            return;
        }
        buf.append(':');
        int minutes = millis / 60000;
        if (minutes < 10) {
            buf.append('0');
        }
        buf.append(minutes);
        millis -= minutes * 60000;
        if (millis == 0) {
            return;
        }
        buf.append(':');
        int seconds = millis / 1000;
        if (seconds < 10) {
            buf.append('0');
        }
        buf.append(seconds);
    }
}
