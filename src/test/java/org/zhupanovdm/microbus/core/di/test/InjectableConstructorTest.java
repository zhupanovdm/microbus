package org.zhupanovdm.microbus.core.di.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.core.di.DependencyQualifierProvider;
import org.zhupanovdm.microbus.core.di.InjectableConstructor;
import org.zhupanovdm.microbus.core.di.InjectableExecutable;
import org.zhupanovdm.microbus.core.di.UnitQuery;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

class InjectableConstructorTest {

    @Mock
    private DependencyQualifierProvider dependencyQualifier;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Instantiation constructor invocation")
    public void invoke() {
        Constructor<?> constructor = ConstructableImpl.class.getConstructors()[0];
        Parameter[] parameters = constructor.getParameters();

        final Object dependencyD = new Object();
        Function<UnitQuery, ?> injector = mockInjector(new HashMap<>() {{
            put(Integer.class, 1);
            put(int.class, 2);
            put(String.class, "random");
            put(Object.class, dependencyD);
        }});

        doAnswer(invocation -> new UnitQuery(null, invocation.getArgument(0, Parameter.class).getType(), null))
                .when(dependencyQualifier)
                .qualify(any(Parameter.class));

        InjectableExecutable<?> injectableExecutable = new InjectableConstructor(constructor, dependencyQualifier);

        ConstructableImpl instance = (ConstructableImpl) injectableExecutable.invoke(injector);
        assertThat(instance.a, equalTo(1));
        assertThat(instance.b, equalTo(2));
        assertThat(instance.c, equalTo("random"));
        assertThat(instance.d, equalTo(dependencyD));

        verify(injector, times(4)).apply(any(UnitQuery.class));
        for (Parameter parameter : parameters) {
            verify(dependencyQualifier).qualify(eq(parameter));
        }
    }

    static Function<UnitQuery, ?> mockInjector(Map<Class<?>, ?> resolver) {
        //noinspection unchecked
        return mock(Function.class, delegatesTo((Function<UnitQuery, ?>) query -> resolver.get(query.getType())));
    }

    private static class ConstructableImpl {
        public final Integer a;
        public final int b;
        public final String c;
        public final Object d;

        public ConstructableImpl(Integer a, int b, String c, Object d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }

}