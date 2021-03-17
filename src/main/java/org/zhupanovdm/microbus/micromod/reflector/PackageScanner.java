package org.zhupanovdm.microbus.micromod.reflector;

import com.google.common.reflect.ClassPath;

import java.io.IOException;

public class PackageScanner {
    @SuppressWarnings("UnstableApiUsage")
    public Class<?>[] scan(String packageName) {
        ClassPath classPath;
        try {
            classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Unable to scan package", e);
        }
        return classPath.getTopLevelClassesRecursive(packageName).stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(aClass -> !aClass.isAnnotation())
                    .toArray(Class[]::new);
    }
}