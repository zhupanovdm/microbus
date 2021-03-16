package org.zhupanovdm.microbus.micromod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.micromod.spawner.InstanceProvider;
import org.zhupanovdm.microbus.samples.sample01.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
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
        registry.register(modAncestor = new Module("ancestor", Ancestor.class, InstanceProvider.Singleton.class));
        registry.register(modChild = new Module("child", Child.class, InstanceProvider.Singleton.class));
        registry.register(modGrandChild = new Module("grandChild", GrandChild.class, InstanceProvider.Singleton.class));
        registry.register(modIFace = new Module("iFace", IFace.class, InstanceProvider.Singleton.class));
    }

    @Test
    @DisplayName("Register module")
    void register() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, InstanceProvider.Singleton.class));
        assertThat(registry.request(ModuleQuery.create("another")), is(another));
    }

    @Test
    @DisplayName("Register same module throws exception")
    void registerSameModuleFails() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, InstanceProvider.Singleton.class));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registry.register(another));
        assertThat(exception.getMessage(), containsString("Module is already registered"));
    }

    @Test
    @DisplayName("Register module with same id throws exception")
    void registerModuleWithSameIdFails() {
        registry.register(new Module("another", NotRegistered.class, InstanceProvider.Singleton.class));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> registry.register(new Module("another", NotRegistered.class, InstanceProvider.Singleton.class)));
        assertThat(exception.getMessage(), containsString("Module is already registered"));
    }

    @Test
    @DisplayName("Unregister module")
    void unregister() {
        Module another;
        registry.register(another = new Module("another", NotRegistered.class, InstanceProvider.Singleton.class));
        registry.unregister(another);
        assertNull(registry.request(ModuleQuery.create("another")));
    }

    @Test
    @DisplayName("Query module by ID only")
    void queryById() {
        assertThat(registry.request(ModuleQuery.create("ancestor")), is(modAncestor));
        assertThat(registry.request(ModuleQuery.create("child")), is(modChild));
        assertThat(registry.request(ModuleQuery.create("grandChild")), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("iFace")), is(modIFace));
        assertNull(registry.request(ModuleQuery.create("notRegistered")));
    }

    @Test
    @DisplayName("Query module by type only")
    void queryByType() {
        assertThat(registry.request(ModuleQuery.create(GrandChild.class)), is(modGrandChild));
        assertNull(registry.request(ModuleQuery.create(NotRegistered.class)));
    }

    @Test
    @DisplayName("Ambiguous query throws exception")
    void queryAmbiguousThrowsException() {
        IllegalStateException exception;

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create(Ancestor.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create(Child.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create(IFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create(SuperIFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("someUnregistered", Ancestor.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("someUnregistered", Child.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("someUnregistered", IFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));

        exception = assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("someUnregistered", SuperIFace.class)));
        assertThat(exception.getMessage(), containsString("Ambiguous request"));
    }

    @Test
    @DisplayName("Query module by id & type")
    void queryByIdAndType() {
        assertThat(registry.request(ModuleQuery.create("ancestor", Ancestor.class)), is(modAncestor));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("ancestor", Child.class)));
        assertThat(registry.request(ModuleQuery.create("ancestor", GrandChild.class)), is(modGrandChild));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("ancestor", IFace.class)));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("ancestor", SuperIFace.class)));
        assertNull(registry.request(ModuleQuery.create("ancestor", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.create("ancestor", Ancestor.class, true)), is(modAncestor));
        assertNull(registry.request(ModuleQuery.create("ancestor", Child.class, true)));
        assertNull(registry.request(ModuleQuery.create("ancestor", GrandChild.class, true)));
        assertNull(registry.request(ModuleQuery.create("ancestor", IFace.class, true)));
        assertNull(registry.request(ModuleQuery.create("ancestor", SuperIFace.class, true)));
        assertNull(registry.request(ModuleQuery.create("ancestor", NotRegistered.class, true)));

        assertThat(registry.request(ModuleQuery.create("child", Ancestor.class)), is(modChild));
        assertThat(registry.request(ModuleQuery.create("child", Child.class)), is(modChild));
        assertThat(registry.request(ModuleQuery.create("child", GrandChild.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("child", IFace.class)), is(modChild));
        assertThat(registry.request(ModuleQuery.create("child", SuperIFace.class)), is(modChild));
        assertNull(registry.request(ModuleQuery.create("child", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.create("child", Ancestor.class, true)), is(modChild));
        assertThat(registry.request(ModuleQuery.create("child", Child.class, true)), is(modChild));
        assertNull(registry.request(ModuleQuery.create("child", GrandChild.class, true)));
        assertThat(registry.request(ModuleQuery.create("child", IFace.class, true)), is(modChild));
        assertThat(registry.request(ModuleQuery.create("child", SuperIFace.class, true)), is(modChild));
        assertNull(registry.request(ModuleQuery.create("child", NotRegistered.class, true)));

        assertThat(registry.request(ModuleQuery.create("grandChild", Ancestor.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", Child.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", GrandChild.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", IFace.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", SuperIFace.class)), is(modGrandChild));
        assertNull(registry.request(ModuleQuery.create("grandChild", NotRegistered.class)));

        assertThat(registry.request(ModuleQuery.create("grandChild", Ancestor.class, true)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", Child.class, true)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", GrandChild.class, true)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", IFace.class, true)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("grandChild", SuperIFace.class, true)), is(modGrandChild));
        assertNull(registry.request(ModuleQuery.create("grandChild", NotRegistered.class, true)));

        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("iFace", Ancestor.class)));
        assertThrows(IllegalStateException.class, () -> registry.request(ModuleQuery.create("iFace", Child.class)));
        assertThat(registry.request(ModuleQuery.create("iFace", GrandChild.class)), is(modGrandChild));
        assertThat(registry.request(ModuleQuery.create("iFace", IFace.class)), is(modIFace));
        assertThat(registry.request(ModuleQuery.create("iFace", SuperIFace.class)), is(modIFace));
        assertNull(registry.request(ModuleQuery.create("iFace", NotRegistered.class)));

        assertNull(registry.request(ModuleQuery.create("iFace", Ancestor.class, true)));
        assertNull(registry.request(ModuleQuery.create("iFace", Child.class, true)));
        assertNull(registry.request(ModuleQuery.create("iFace", GrandChild.class, true)));
        assertThat(registry.request(ModuleQuery.create("iFace", IFace.class, true)), is(modIFace));
        assertThat(registry.request(ModuleQuery.create("iFace", SuperIFace.class, true)), is(modIFace));
        assertNull(registry.request(ModuleQuery.create("iFace", NotRegistered.class, true)));
    }

}