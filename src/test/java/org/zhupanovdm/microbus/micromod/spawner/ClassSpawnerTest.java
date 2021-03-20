package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.TwoConstructors;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassSpawnerTest {
    @Test
    @DisplayName("Spawn instance with constructor spawner impl")
    void spawnWith3ArgConstructor() {
        Spawner spawner = new ClassSpawner(With3ArgConstructor.class);
        Collection<ModuleQuery> dependencies = spawner.getDependencies();
        assertThat(dependencies, hasSize(3));

        for (ModuleQuery query : dependencies) {
            if (Integer.class.equals(query.getType()))
                query.getInjector().accept(10);
            if (String.class.equals(query.getType()))
                query.getInjector().accept("1050");
            if (Double.class.equals(query.getType()))
                query.getInjector().accept(123.456D);
        }

        Object instance = spawner.get();
        assertThat(instance, instanceOf(With3ArgConstructor.class));
        assertThat(((With3ArgConstructor) instance).i, is(10));
        assertThat(((With3ArgConstructor) instance).s, is("1050"));
        assertThat(((With3ArgConstructor) instance).d, is(123.456D));
    }

    @Test
    @DisplayName("Constructor spawner fails on class with more than one constructor")
    void constructorSpawnerFailsOnNonSingleConstructor() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ClassSpawner(TwoConstructors.class));
        assertThat(exception.getMessage(), containsString("Only one constructor is supported"));
    }
}