package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.ModuleQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class InstanceProviderTest {
    @Mock
    Spawner spawner;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Singleton instantiation")
    void testSingleton() {
        Consumer<Collection<ModuleQuery>> injector = dependencies -> {};

        doAnswer(invocation -> {
            //noinspection unchecked
            assertThat((Consumer<Collection<ModuleQuery>>) invocation.getArgument(0, Consumer.class), is(injector));
            return new Object();
        }).when(spawner).apply(eq(injector));

        InstanceProvider singleton = new InstanceProvider.Singleton(spawner);
        Set<Object> result = new HashSet<>();
        result.add(singleton.apply(injector));
        for (int i = 0; i < 10; i++) {
            assertThat(result.add(singleton.apply(injector)), is(false));
        }

        verify(spawner).apply(any());
    }

    @Test
    @DisplayName("Factory instantiation")
    void testFactory() {
        Consumer<Collection<ModuleQuery>> injector = dependencies -> {};

        doAnswer(invocation -> {
            //noinspection unchecked
            assertThat((Consumer<Collection<ModuleQuery>>) invocation.getArgument(0, Consumer.class), is(injector));
            return new Object();
        }).when(spawner).apply(eq(injector));

        InstanceProvider singleton = new InstanceProvider.Factory(spawner);
        Set<Object> result = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            assertThat(result.add(singleton.apply(injector)), is(true));
        }

        verify(spawner, times(10)).apply(any());
    }

}