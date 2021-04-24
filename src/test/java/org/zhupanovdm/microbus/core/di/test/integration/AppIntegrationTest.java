package org.zhupanovdm.microbus.core.di.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.App;
import org.zhupanovdm.microbus.core.di.test.samples.sample01.IBaseService;
import org.zhupanovdm.microbus.core.di.test.samples.sample01.IService;
import org.zhupanovdm.microbus.core.di.test.samples.sample01.Service01;
import org.zhupanovdm.microbus.core.di.test.samples.sample02.Dependency2;
import org.zhupanovdm.microbus.core.di.test.samples.sample02.RootUnit;
import org.zhupanovdm.microbus.core.di.test.samples.sample03.DependentUnit;
import org.zhupanovdm.microbus.core.di.test.samples.sample04.*;

import java.util.HashSet;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.PERMISSIVE_ID;

public class AppIntegrationTest {

    @BeforeEach
    void setup() {
        App.run(AppIntegrationTest.class, new String[] {});
    }

    @Test
    @DisplayName("Get unit (basic)")
    public void testGetUnitBasic() {

        App.scan(Service01.class.getPackageName());
        App.activate(Service01.class.getPackageName());

        Object service01 = App.getUnit("service01");
        assertTrue(service01 instanceof Service01);

        assertSame(service01, App.getUnit(IBaseService.class));
        assertSame(service01, App.getUnit(IService.class));
        assertSame(service01, App.getUnit(Service01.class));
        assertSame(service01, App.getUnit("service01", Service01.class));
        assertSame(service01, App.getUnit("service01", IBaseService.class));
        assertSame(service01, App.getUnit("service01", IService.class));
        assertSame(service01, App.getUnit("service01", Service01.class, EXACT_TYPE));
        assertSame(service01, App.getUnit("unknown", Service01.class, PERMISSIVE_ID));
        assertSame(service01, App.getUnit("unknown", IService.class, PERMISSIVE_ID));

        assertThrows(NoSuchElementException.class, () -> App.getUnit("service01", IService.class, EXACT_TYPE));
        assertThrows(NoSuchElementException.class, () -> App.getUnit("service02", Service01.class));
        assertThrows(NoSuchElementException.class, () -> App.getUnit("service02"));

    }

    @Test
    @DisplayName("Get unit with dependencies (satisfy by type)")
    public void testGetUnitWithDependencies() {

        App.scan(RootUnit.class.getPackageName());
        App.activate(RootUnit.class.getPackageName());

        RootUnit rootUnit = (RootUnit) App.getUnit("rootUnit");

        assertSame(App.getUnit("dependency1"), rootUnit.dependency1);
        assertSame(App.getUnit("dependency2"), rootUnit.dependency2);
        assertTrue(rootUnit.dependency2 instanceof Dependency2);

        assertSame(App.getUnit("childDependency"), ((Dependency2) rootUnit.dependency2).childDependency);

    }

    @Test
    @DisplayName("Inject dependencies with @Inject")
    public void testInject() {

        App.scan(DependentUnit.class.getPackageName());
        App.activate(DependentUnit.class.getPackageName());

        DependentUnit dependentUnit = (DependentUnit) App.getUnit("dependentUnit");
        assertSame(App.getUnit("dependency01"), dependentUnit.d01);
        assertSame(App.getUnit("dependency02"), dependentUnit.getD02());
        assertSame(App.getUnit("dependency03"), dependentUnit.dependency03);

    }

    @Test
    @DisplayName("Factory unit creation")
    public void testFactory() {

        App.scan(Factory.class.getPackageName());
        App.activate(Factory.class.getPackageName());

        Object unitDep01 = App.getUnit("unit-dep-1");

        Unit01 unit01 = (Unit01) App.getUnit("unitFactory01");
        assertSame(unit01, App.getUnit("unitFactory01"));
        assertTrue(unit01.dependency instanceof UnitDependency01);
        assertSame(unit01.dependency, unitDep01);

        HashSet<Object> instances1 = new HashSet<>();
        HashSet<Object> instances2 = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Unit01 unitFactory02 = (Unit01) App.getUnit("unitFactory02");
            assertTrue(instances1.add(unitFactory02));
            assertTrue(unitFactory02.dependency instanceof UnitDependency02);
            assertTrue(instances2.add(unitFactory02.dependency));
        }

        Unit01 unit03 = (Unit01) App.getUnit("unitFactory03");
        assertSame(unit03, App.getUnit("unitFactory03"));
        assertTrue(unit03.dependency instanceof UnitDependency01);
        assertSame(unit03.dependency, unitDep01);

    }

}
