package com.github.clazz.model;

import java.util.List;

public class JarClass {
    private String jarPath;
    private List<String> canonicalNames;

    public void setCanonicalName(List<String> canonicalNames){
        this.canonicalNames = canonicalNames;
    }

    public void setJarPath(String jarPath){
        this.jarPath = jarPath;
    }

    public List<String> getCanonicalNames(){
        return this.canonicalNames;
    }

    public String getJarPath() {
        return this.jarPath;
    }
}
