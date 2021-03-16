package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.Module;
import org.zhupanovdm.microbus.micromod.ModuleManager;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;
import org.zhupanovdm.microbus.samples.sample02.WithNoArgConstructor;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MethodSpawnerTest {
    @Mock
    ModuleManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Spawn instance with method spawner impl")
    void spawnWith3ArgMethod() throws Exception {
        Module declaringModule = new Module("declaringModule", WithNoArgConstructor.class, InstanceProvider.Singleton.class);
        Module factoryMethod = new Module("factoryMethod", With3ArgConstructor.class, InstanceProvider.Singleton.class);
        Spawner spawner = new MethodSpawner(factoryMethod,
                WithNoArgConstructor.class.getDeclaredMethod("factoryMethod", Integer.class, String.class, Double.class),
                declaringModule.getId(), manager);
        InstanceProvider singleton = spy(new InstanceProvider.Singleton());

        doAnswer(invocation -> singleton)
                .when(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));

        doAnswer(invocation -> {
            ModuleQuery query = invocation.getArgument(0, ModuleQuery.class);
            if ("declaringModule".equals(query.getId())) {
                assertThat(query.isStrictId(), is(true));
                assertThat(WithNoArgConstructor.class.equals(query.getType()), is(true));
                return new WithNoArgConstructor();
            }
            assertNull(query.getId());
            assertNotNull(query.getType());
            assertThat(query.isStrictId(), is(false));
            if (Integer.class.equals(query.getType())) {
                return 10;
            }
            if (String.class.equals(query.getType())) {
                return "1050";
            }
            if (Double.class.equals(query.getType())) {
                return 123.456D;
            }
            return null;
        }).when(manager).resolve(any(ModuleQuery.class));

        Object instance = spawner.spawn(new HashSet<>());
        assertThat(instance, instanceOf(With3ArgConstructor.class));
        assertThat(((With3ArgConstructor) instance).i, is(10));
        assertThat(((With3ArgConstructor) instance).s, is("1050"));
        assertThat(((With3ArgConstructor) instance).d, is(123.456D));

        verify(singleton).apply(eq(factoryMethod), any());
        verify(manager).getInstanceProvider(eq(InstanceProvider.Singleton.class));
        verify(manager, times(4)).resolve(any(ModuleQuery.class));
    }

}