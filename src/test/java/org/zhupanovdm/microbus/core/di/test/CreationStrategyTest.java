package org.zhupanovdm.microbus.core.di.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.zhupanovdm.microbus.core.di.CreationStrategy;
import org.zhupanovdm.microbus.core.di.InstanceHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

class CreationStrategyTest {
    private InstanceHolder<Object> holder;
    private AtomicInteger counter;

    @BeforeEach
    void setup() {
        holder = new InstanceHolderImpl();
        counter = new AtomicInteger();
    }

    @Test
    @DisplayName("Singleton instance creation")
    public void testSingleton() {
        CreationStrategy strategy = new CreationStrategy.Singleton();
        Supplier<Object> spawner = mockSpawner();

        Object instance = strategy.getInstance(holder, spawner);
        for (int i = 0; i < 99; i++) {
            Assertions.assertSame(instance, strategy.getInstance(holder, spawner));
        }

        verify(spawner).get();
    }

    @Test
    @DisplayName("Singleton instance creation by multiple threads")
    public void testSingletonConcurrent() throws Exception {
        CreationStrategy strategy = new CreationStrategy.Singleton();
        Set<Object> instances = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final int executions = 100;

        CyclicBarrier barrier = new CyclicBarrier(executions);
        ExecutorService executors = Executors.newFixedThreadPool(executions);
        for (int i = 0; i < executions; i++) {
            executors.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException("Got exception", e);
                }
                instances.add(strategy.getInstance(holder, Object::new));
                counter.incrementAndGet();
            });
        }

        //noinspection ResultOfMethodCallIgnored
        executors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(counter.get(), equalTo(executions));
        assertThat(instances.size(), equalTo(1));
    }

    @Test
    @DisplayName("Factory instance production")
    public void testFactory() {
        CreationStrategy strategy = new CreationStrategy.Factory();
        Supplier<Object> spawner = mockSpawner();

        Set<Object> instances = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Assertions.assertTrue(instances.add(strategy.getInstance(holder, spawner)));
        }
        verify(spawner, times(100)).get();
    }

    @Test
    @DisplayName("Factory instance production by multiple threads")
    public void testFactoryConcurrent() throws Exception {
        CreationStrategy strategy = new CreationStrategy.Factory();
        Set<Object> instances = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final int executions = 100;

        CyclicBarrier barrier = new CyclicBarrier(executions);
        ExecutorService executors = Executors.newFixedThreadPool(executions);
        for (int i = 0; i < executions; i++) {
            executors.submit(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException("Got exception", e);
                }
                instances.add(strategy.getInstance(holder, Object::new));
                counter.incrementAndGet();
            });
        }

        //noinspection ResultOfMethodCallIgnored
        executors.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(counter.get(), equalTo(executions));
        assertThat(instances.size(), equalTo(executions));
    }

    private static class InstanceHolderImpl implements InstanceHolder<Object> {
        private Object instance;

        @Override
        public Object getInstance() {
            return instance;
        }

        @Override
        public void setInstance(Object instance) {
            this.instance = instance;
        }
    }

    static Supplier<Object> mockSpawner() {
        //noinspection unchecked
        return mock(Supplier.class, delegatesTo((Supplier<Object>) Object::new));
    }

}