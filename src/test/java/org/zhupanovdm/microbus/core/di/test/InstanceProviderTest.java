package org.zhupanovdm.microbus.core.di.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.core.AppContext;
import org.zhupanovdm.microbus.core.di.*;

import java.util.Collections;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

class InstanceProviderTest {

    @Mock
    private AppContext context;

    @Mock
    private UnitRegistry unitRegistry;

    @Mock
    private InstanceCreationStrategyProvider strategyProvider;

    @Mock
    private DependencyQualifierProvider qualifierProvider;

    @Mock
    private CreationStrategy creationStrategy;

    private InstanceProvider provider;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        doReturn(unitRegistry).when(context).getUnitRegistry();
        doReturn(strategyProvider).when(context).getInstanceCreationStrategyProvider();
        doReturn(qualifierProvider).when(context).getQualifierProvider();

        doReturn(creationStrategy).when(strategyProvider).get(any());

        provider = new InstanceProvider(context);
    }

    @Test
    @DisplayName("Resolve unit")
    public void testResolveUnit() {
        Object instance = new Object();

        DependencyQualifier<?> qualifier = mock(DependencyQualifier.class);
        doReturn(Collections.EMPTY_SET).when(qualifier).getAll();

        InjectableExecutable<?> constructor = mock(InjectableExecutable.class);
        doReturn(instance).when(constructor).invoke(any());

        UnitHolder unit = mock(UnitHolder.class);
        doReturn(CreationStrategy.Singleton.class).when(unit).getCreationStrategy();
        doReturn(constructor).when(unit).getConstructor();

        doReturn(qualifier).when(qualifierProvider).getFields();

        doAnswer(invocation -> {
            //noinspection unchecked
            return ((Supplier<Object>) invocation.getArgument(1)).get();
        }).when(creationStrategy).getInstance(eq(unit), any());

        assertThat(provider.resolve(unit), sameInstance(instance));

        verify(strategyProvider).get(eq(CreationStrategy.Singleton.class));
        verify(creationStrategy).getInstance(eq(unit), any());
        verify(constructor).invoke(any());

    }

}