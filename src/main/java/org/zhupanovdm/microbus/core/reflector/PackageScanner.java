package org.zhupanovdm.microbus.core.reflector;

import com.google.common.reflect.ClassPath;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PackageScanner {
    @SuppressWarnings("UnstableApiUsage")
    public static Class<?>[] scan(String packageName) {
        ClassPath classPath;
        try {
            classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        } catch (IOException e) {
            log.error("Failed to scan package: {}", packageName, e);
            throw new RuntimeException("Unable to scan package: " + packageName, e);
        }

        return classPath.getTopLevelClassesRecursive(packageName).stream()
                .map(ClassPath.ClassInfo::load)
                .filter(aClass -> !aClass.isAnnotation())
                .toArray(Class[]::new);
    }
}