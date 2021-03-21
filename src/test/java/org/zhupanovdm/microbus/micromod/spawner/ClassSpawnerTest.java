package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.TwoConstructors;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;

import java.util.Collection;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

class ClassSpawnerTest {
    @Mock
    Initializer initializer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Spawn instance with constructor spawner impl")
    void spawnWith3ArgConstructor() {
        Spawner spawner = new ClassSpawner(With3ArgConstructor.class, initializer);

        Object instance = spawner.apply(mockInjector(dependencies -> {
            assertThat(dependencies, hasSize(3));
            for (ModuleQuery query : dependencies) {
                if (Integer.class.equals(query.getType()))
                    query.getInjector().accept(10);
                if (String.class.equals(query.getType()))
                    query.getInjector().accept("1050");
                if (Double.class.equals(query.getType()))
                    query.getInjector().accept(123.456D);
            }
        }));

        assertThat(instance, instanceOf(With3ArgConstructor.class));
        assertThat(((With3ArgConstructor) instance).i, is(10));
        assertThat(((With3ArgConstructor) instance).s, is("1050"));
        assertThat(((With3ArgConstructor) instance).d, is(123.456D));
    }

    @Test
    @DisplayName("Constructor spawner fails on class with more than one constructor")
    void constructorSpawnerFailsOnNonSingleConstructor() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ClassSpawner(TwoConstructors.class, initializer));
        assertThat(exception.getMessage(), containsString("Only one constructor is supported"));
    }

    static Consumer<Collection<ModuleQuery>> mockInjector(Consumer<Collection<ModuleQuery>> injector) {
        //noinspection unchecked
        return mock(Consumer.class, delegatesTo(injector));
    }

}