package org.zhupanovdm.microbus.micromod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.micromod.spawner.SpawnStrategy;
import org.zhupanovdm.microbus.samples.sample01.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleRegistryTest {
    ModuleRegistry registry;

    Module modAncestor;
    Module modChild;
    Module modGrandChild;
    Module modIFace;

    @BeforeEach
    void setup() {
        registry = new ModuleRegistry();
        registry.register(modAncestor = new Module("ancestor", Ancestor.class, SpawnStrategy.Singleton.class));
        registry.register(modChild = new Module("child", Child.class, SpawnStrategy.Singleton.class));
        registry.register(modGrandChild = new Module("grandChild", GrandChild.class, SpawnStrategy.Singleton.class));
        registry.register(modIFace = new Module("iFace", IFace.class, SpawnStrategy.Singleton.class));
    }

    @Test
    @DisplayName("Register module")
    void register() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, SpawnStrategy.Singleton.class));
        assertThat(registry.request(ModuleQuery.of("another")), sameInstance(another));
    }

    @Test
    @DisplayName("Register same module throws exception")
    void registerSameModuleFails() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, SpawnStrategy.Singleton.class));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registry.register(another));
        assertThat(exception.getMessage(), containsString("Module is already registered"));
    }

    @Test
    @DisplayName("Register module with same id throws exception")
    void registerModuleWithSameIdFails() {
        registry.register(new Module("another", NotRegistered.class, SpawnStrategy.Singleton.class));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registry.register(new Module("another", NotRegistered.class, SpawnStrategy.Singleton.class)));
        assertThat(exception.getMessage(), containsString("Module is already registered"));
    }

    @Test
    @DisplayName("Unregister module")
    void unregister() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, SpawnStrategy.Singleton.class));
        registry.unregister(another);
        assertNull(registry.request(ModuleQuery.of("another")));
    }

    @Test
    @DisplayName("Query module by ID only")
    void queryById() {
        assertThat(registry.request(ModuleQuery.of("ancestor")), sameInstance(modAncestor));
        assertThat(registry.request(ModuleQuery.of("child")), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("grandChild")), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("iFace")), sameInstance(modIFace));
        assertNull(registry.request(ModuleQuery.of("notRegistered")));
    }

    @Test
    @DisplayName("Query module by type only")
    void queryByType() {
        assertThat(registry.request(ModuleQuery.of(GrandChild.class)), sameInstance(modGrandChild));
        assertNull(registry.request(ModuleQuery.of(NotRegistered.class)));
    }

    @Test
    @DisplayName("Ambiguous query throws exception")
    void queryAmbiguousThrowsException() {
        IllegalStateException exception;

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of(Ancestor.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of(Child.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of(IFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of(SuperIFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("someUnregistered", Ancestor.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("someUnregistered", Child.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("someUnregistered", IFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("someUnregistered", SuperIFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));
    }

    @Test
    @DisplayName("Query module by id & type")
    void queryByIdAndType() {
        assertThat(registry.request(ModuleQuery.of("ancestor", Ancestor.class)), sameInstance(modAncestor));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("ancestor", Child.class)));
        assertThat(registry.request(ModuleQuery.of("ancestor", GrandChild.class)), sameInstance(modGrandChild));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("ancestor", IFace.class)));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("ancestor", SuperIFace.class)));
        assertNull(registry.request(ModuleQuery.of("ancestor", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.of("ancestor", Ancestor.class, true)), sameInstance(modAncestor));
        assertNull(registry.request(ModuleQuery.of("ancestor", Child.class, true)));
        assertNull(registry.request(ModuleQuery.of("ancestor", GrandChild.class, true)));
        assertNull(registry.request(ModuleQuery.of("ancestor", IFace.class, true)));
        assertNull(registry.request(ModuleQuery.of("ancestor", SuperIFace.class, true)));
        assertNull(registry.request(ModuleQuery.of("ancestor", NotRegistered.class, true)));

        assertThat(registry.request(ModuleQuery.of("child", Ancestor.class)), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("child", Child.class)), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("child", GrandChild.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("child", IFace.class)), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("child", SuperIFace.class)), sameInstance(modChild));
        assertNull(registry.request(ModuleQuery.of("child", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.of("child", Ancestor.class, true)), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("child", Child.class, true)), sameInstance(modChild));
        assertNull(registry.request(ModuleQuery.of("child", GrandChild.class, true)));
        assertThat(registry.request(ModuleQuery.of("child", IFace.class, true)), sameInstance(modChild));
        assertThat(registry.request(ModuleQuery.of("child", SuperIFace.class, true)), sameInstance(modChild));
        assertNull(registry.request(ModuleQuery.of("child", NotRegistered.class, true)));

        assertThat(registry.request(ModuleQuery.of("grandChild", Ancestor.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", Child.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", GrandChild.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", IFace.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", SuperIFace.class)), sameInstance(modGrandChild));
        assertNull(registry.request(ModuleQuery.of("grandChild", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.of("grandChild", Ancestor.class, true)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", Child.class, true)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", GrandChild.class, true)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", IFace.class, true)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("grandChild", SuperIFace.class, true)), sameInstance(modGrandChild));
        assertNull(registry.request(ModuleQuery.of("grandChild", NotRegistered.class, true)));

        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("iFace", Ancestor.class)));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.of("iFace", Child.class)));
        assertThat(registry.request(ModuleQuery.of("iFace", GrandChild.class)), sameInstance(modGrandChild));
        assertThat(registry.request(ModuleQuery.of("iFace", IFace.class)), sameInstance(modIFace));
        assertThat(registry.request(ModuleQuery.of("iFace", SuperIFace.class)), sameInstance(modIFace));
        assertNull(registry.request(ModuleQuery.of("iFace", NotRegistered.class)));

        assertNull(registry.request(ModuleQuery.of("iFace", Ancestor.class, true)));
        assertNull(registry.request(ModuleQuery.of("iFace", Child.class, true)));
        assertNull(registry.request(ModuleQuery.of("iFace", GrandChild.class, true)));
        assertThat(registry.request(ModuleQuery.of("iFace", IFace.class, true)), sameInstance(modIFace));
        assertThat(registry.request(ModuleQuery.of("iFace", SuperIFace.class, true)), sameInstance(modIFace));
        assertNull(registry.request(ModuleQuery.of("iFace", NotRegistered.class, true)));
    }

}