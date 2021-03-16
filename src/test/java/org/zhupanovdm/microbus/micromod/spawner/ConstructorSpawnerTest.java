package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.TwoConstructors;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConstructorSpawnerTest {
    @Mock
    ModuleManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Spawn instance with constructor spawner impl")
    void spawnWith3ArgConstructor() {
        Module module = new Module("id", With3ArgConstructor.class, InstanceProvider.Singleton.class);
        Spawner spawner = new ConstructorSpawner(module, manager);
        Set<Module> chain = new HashSet<>();
        InstanceProvider singleton = new InstanceProvider.Singleton();

        doAnswer(invocation -> singleton)
                .when(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));

        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            assertNull(query.getId());
            assertNotNull(query.getType());
            assertThat(query.isStrictId(), is(false));
            if (Integer.class.equals(query.getType()))
                return 10;
            if (String.class.equals(query.getType()))
                return "1050";
            if (Double.class.equals(query.getType()))
                return 123.456D;
            return null;
        }).when(manager).resolve(any(ModuleQuery.class));

        Object instance = spawner.spawn(chain);
        assertThat(instance, instanceOf(With3ArgConstructor.class));
        assertThat(((With3ArgConstructor) instance).i, is(10));
        assertThat(((With3ArgConstructor) instance).s, is("1050"));
        assertThat(((With3ArgConstructor) instance).d, is(123.456D));

        verify(manager, times(3)).resolve(any(ModuleQuery.class));
    }

    @Test
    @DisplayName("Constructor spawner fails on class with more than one constructor")
    void constructorSpawnerFailsOnNonSingleConstructor() {
        Module module = new Module("id", TwoConstructors.class, InstanceProvider.Singleton.class);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ConstructorSpawner(module, manager));
        assertThat(exception.getMessage(), containsString("Only one constructor is supported"));
    }

}