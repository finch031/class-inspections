package com.github.clazz;

import com.github.clazz.model.JarClass;
import com.github.clazz.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.clazz.utils.Utils.ensureFileOrDirExistAndCanRead;
import static com.github.clazz.utils.Utils.paramIndexSearch;

public class JarClassInspectionsMain {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String USAGE =
            "usage: " + LINE_SEPARATOR +
                    " java -jar class-inspections-1.0.0 " + LINE_SEPARATOR +
                    "    ---jar_paths path1|path2|path3|...(comma-delimited) " + LINE_SEPARATOR +
                    "    ---class_name class_name " + LINE_SEPARATOR +
                    "    ---method_name method_name "
            ;

    private static void printUsageAndExit(String...messages){
        for (String message : messages) {
            System.err.println(message);
        }
        System.err.println(USAGE);
        System.exit(1);
    }

    private static String[] getJarPaths(String[] args){
        String[] jarPaths = null;
        int index = paramIndexSearch(args,"---jar_paths");
        if(index != -1){
            String jarPathTemp = args[index+1];
            if(jarPathTemp.contains(",")){
                String[] items = jarPathTemp.split(",");
                for (String item : items) {
                    try{
                        ensureFileOrDirExistAndCanRead(new File(item));
                    }catch (IOException ioe){
                        printUsageAndExit(Utils.stackTrace(ioe));
                    }
                }

                jarPaths = items;
            }else{
                try{
                    ensureFileOrDirExistAndCanRead(new File(jarPathTemp));
                }catch (IOException ioe){
                    printUsageAndExit(Utils.stackTrace(ioe));
                }

                jarPaths = new String[1];
                jarPaths[0] = jarPathTemp;
            }
        }else {
            printUsageAndExit("error: ---jar_paths not found!");
        }

        return jarPaths;
    }

    private static String getClassName(String[] args){
        String className = null;
        int index = paramIndexSearch(args,"---class_name");
        if(index != -1){
            className = args[index+1];
        }else {
            printUsageAndExit("error: ---class_name not found!");
        }

        if(className == null || className.trim().length() == 0){
            printUsageAndExit("error: ---class_name is invalid!");
        }

        return className;
    }

    private static String getMethodName(String[] args){
        String methodName = null;
        int index = paramIndexSearch(args,"---method_name");
        if(index != -1){
            methodName = args[index+1];
        }
        return methodName;
    }

    public static void main(String[] args){
        // jar paths.
        String[] jarPaths = getJarPaths(args);
        // class name to inspections.
        String className = getClassName(args);
        // method name in the class.
        String methodName = getMethodName(args);

        long globalStartTs = System.currentTimeMillis();

        Map<String,String> matchJarClassMap = new HashMap<>();
        for (String jarPath : jarPaths) {
            long startTs = System.currentTimeMillis();
            List<File> dirJarFiles = Utils.dirJarFileScan(jarPath);
            long transMillis = System.currentTimeMillis() - startTs;
            StringBuilder sb = new StringBuilder();
            Utils.appendPosixTime(sb,transMillis);
            System.out.println("jar path:" + jarPath + ",jar file scan time elapsed:" + sb.toString());

            startTs = System.currentTimeMillis();
            List<JarClass> jarClassList = Utils.jarFileClassesParse(dirJarFiles);
            for (JarClass jarClass : jarClassList) {
                for (String s : jarClass.getCanonicalNames()) {
                    if(s.contains(className)){
                        matchJarClassMap.put(jarClass.getJarPath(),s);
                        break;
                    }
                }
            }
            transMillis = System.currentTimeMillis() - startTs;
            sb.delete(0,sb.length());
            Utils.appendPosixTime(sb,transMillis);
            System.out.println("jar path:" + jarPath + ",jar file parse time elapsed:" + sb.toString());
        }

        if(matchJarClassMap.size() > 0){
            System.out.println("match class jar:");
            int i = 0;
            for (Map.Entry<String, String> entry : matchJarClassMap.entrySet()) {
                System.out.println("   " + i + " => " + entry.getKey());
                System.out.println("         " + entry.getValue());
                i++;
            }
        }else{
            System.out.println("no match class jar.");
        }

        long transMillis = System.currentTimeMillis() - globalStartTs;
        StringBuilder sb = new StringBuilder();
        Utils.appendPosixTime(sb,transMillis);
        System.out.println("total time elapsed:" + sb.toString());
    }
}
