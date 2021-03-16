package org.zhupanovdm.microbus.micromod;

import lombok.Getter;

public class ModuleException extends RuntimeException {
    @Getter
    private final Module module;

    public ModuleException(String message, Module module) {
        super(message);
        this.module = module;
    }

    public ModuleException(String message, Module module, Throwable throwable) {
        super(message, throwable);
        this.module = module;
    }

}
