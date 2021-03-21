package org.zhupanovdm.microbus.micromod.spawner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.micromod.ModuleQuery;
import org.zhupanovdm.microbus.samples.sample02.With3ArgConstructor;
import org.zhupanovdm.microbus.samples.sample02.WithNoArgConstructor;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class MethodSpawnerTest {
    @Mock
    Initializer initializer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Spawn instance with method spawner impl")
    void spawnWith3ArgMethod() throws Exception {
        Method method = WithNoArgConstructor.class.getDeclaredMethod("factoryMethod", Integer.class, String.class, Double.class);
        Spawner spawner = new MethodSpawner(method, "declaringModule", initializer);

        Object instance = spawner.apply(dependencies -> {
            assertThat(dependencies, hasSize(4));

            for (ModuleQuery query : dependencies) {
                if ("declaringModule".equals(query.getId())) {
                    assertThat(query.isStrictId(), is(true));
                    assertThat(WithNoArgConstructor.class.equals(query.getType()), is(true));
                    query.getInjector().accept(new WithNoArgConstructor());
                }
                if (Integer.class.equals(query.getType()))
                    query.getInjector().accept(10);
                if (String.class.equals(query.getType()))
                    query.getInjector().accept("1050");
                if (Double.class.equals(query.getType()))
                    query.getInjector().accept(123.456D);
            }
        });

        assertThat(instance, instanceOf(With3ArgConstructor.class));
        assertThat(((With3ArgConstructor) instance).i, is(10));
        assertThat(((With3ArgConstructor) instance).s, is("1050"));
        assertThat(((With3ArgConstructor) instance).d, is(123.456D));
    }

}