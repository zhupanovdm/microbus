package org.zhupanovdm.microbus.core;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class App {
    private static AppContext context;

    public static void run(Class<?> mainClass, String[] args) {
        log.info("Launching app with args: {}", Arrays.toString(args));
        context = AppDefaultContext.create(mainClass, args);
        new ActivationLauncher(context).engage();
    }

    public static AppContext getContext() {
        return context;
    }

}
