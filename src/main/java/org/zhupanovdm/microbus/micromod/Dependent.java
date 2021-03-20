package org.zhupanovdm.microbus.micromod;

import java.util.Collection;

public interface Dependent {
    Collection<ModuleQuery> getDependencies();
}