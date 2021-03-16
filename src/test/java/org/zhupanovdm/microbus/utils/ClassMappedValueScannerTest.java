package org.zhupanovdm.microbus.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.zhupanovdm.microbus.samples01.Ancestor;
import org.zhupanovdm.microbus.samples01.Child;
import org.zhupanovdm.microbus.samples01.GrandChild;
import org.zhupanovdm.microbus.samples01.SuperIFace;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

class ClassMappedValueScannerTest {
    static final ValueHolder VALUE_ANCESTOR = new ValueHolder(Ancestor.class, 1);
    static final ValueHolder VALUE_CHILD = new ValueHolder(Child.class, 2);
    static final ValueHolder VALUE_GRAND_CHILD = new ValueHolder(GrandChild.class, 3);

    ClassMappedValueScanner<ValueHolder> scanner;

    @Captor
    ArgumentCaptor<Collection<ValueHolder>> valueHoldersArg;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        scanner = new ClassMappedValueScanner<>(ValueHolder::getClazz);
    }

    @Test
    @DisplayName("Discover a child class")
    void scanAssociatedValues1() {
        scanner.put(VALUE_GRAND_CHILD);

        var visitor = mockVisitorFn((valueHolders, integer) -> true);
        scanner.scan(Ancestor.class, visitor);

        verify(visitor).apply(valueHoldersArg.capture(), eq(2));
        assertThat(valueHoldersArg.getAllValues().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()), contains(VALUE_GRAND_CHILD));
    }

    @Test
    @DisplayName("Discover inherited classes (all generations)")
    void scanAssociatedValues2() {
        scanner.put(VALUE_GRAND_CHILD);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_ANCESTOR);

        ArgumentCaptor<Integer> generationArg = ArgumentCaptor.forClass(Integer.class);

        var visitor = mockVisitorFn((valueHolders, integer) -> true);
        scanner.scan(Ancestor.class, visitor);

        verify(visitor, times(3))
                    .apply(valueHoldersArg.capture(), generationArg.capture());
        assertThat(valueHoldersArg.getAllValues().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()), contains(VALUE_ANCESTOR, VALUE_CHILD, VALUE_GRAND_CHILD));
        assertThat(generationArg.getAllValues(), contains(0, 1, 2));
    }

    @Test
    @DisplayName("Class pre-ancestor wont be acquired")
    void scanAssociatedValues3() {
        scanner.put(VALUE_ANCESTOR);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_GRAND_CHILD);

        ArgumentCaptor<Integer> generationArg = ArgumentCaptor.forClass(Integer.class);

        var visitor = mockVisitorFn((valueHolders, integer) -> true);
        scanner.scan(Child.class, visitor);

        verify(visitor, times(2))
                    .apply(valueHoldersArg.capture(), generationArg.capture());
        assertThat(valueHoldersArg.getAllValues().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()), contains(VALUE_CHILD, VALUE_GRAND_CHILD));
        assertThat(generationArg.getAllValues(), contains(0, 1));
    }

    @Test
    @DisplayName("Interface implementations will be acquired")
    void scanAssociatedValues4() {
        scanner.put(VALUE_ANCESTOR);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_GRAND_CHILD);

        // three invocations expected, as a cause of GrandChild implements IFace too
        // generations:
        //  2: SuperiorIFace <- IFace <- GrandChild
        //  2: SuperiorIFace <- IFace <- Child
        //  3: SuperiorIFace <- IFace <- Child <- GrandChild
        var visitor = mockVisitorFn((valueHolders, generation) -> {
            if (List.of(VALUE_GRAND_CHILD).equals(valueHolders))
                assertThat(generation, isOneOf(2, 3));
            if (List.of(VALUE_CHILD).equals(valueHolders))
                assertThat(generation, is(2));
            return true;
        });

        scanner.scan(SuperIFace.class, visitor);

        verify(visitor, times(3))
                .apply(valueHoldersArg.capture(), anyInt());

        assertThat(valueHoldersArg.getAllValues().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()), containsInAnyOrder(VALUE_GRAND_CHILD, VALUE_CHILD, VALUE_GRAND_CHILD));
    }

    @Test
    @DisplayName("Class scan stops if visitor returns false")
    void scanAssociatedValues5() {
        scanner.put(VALUE_ANCESTOR);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_GRAND_CHILD);

        var visitor = mockVisitorFn((valueHolders, integer) -> false);
        scanner.scan(Child.class, visitor);

        verify(visitor).apply(valueHoldersArg.capture(), eq(0));
        assertThat(valueHoldersArg.getAllValues().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()), contains(VALUE_CHILD));
    }

    @Test
    @DisplayName("Collect visitor result")
    void collectAssociatedValues() {
        scanner.put(VALUE_ANCESTOR);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_GRAND_CHILD);

        HashSet<ValueHolder> result = scanner.collect(Ancestor.class, new HashSet<>());
        assertThat(result, containsInAnyOrder(VALUE_ANCESTOR, VALUE_CHILD, VALUE_GRAND_CHILD));
    }

    @Test
    @DisplayName("Remove associated value")
    void removeValue() {
        scanner.put(VALUE_GRAND_CHILD);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_ANCESTOR);
        scanner.remove(VALUE_CHILD);

        ArgumentCaptor<Integer> generationArg = ArgumentCaptor.forClass(Integer.class);

        var visitor = mockVisitorFn((valueHolders, integer) -> true);
        scanner.scan(Ancestor.class, visitor);

        verify(visitor, times(2))
                .apply(valueHoldersArg.capture(), generationArg.capture());
        assertThat(valueHoldersArg.getAllValues().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()), contains(VALUE_ANCESTOR, VALUE_GRAND_CHILD));
        assertThat(generationArg.getAllValues(), contains(0, 2));

    }

    @Test
    @DisplayName("Get all types")
    void getTypes() {
        scanner.put(VALUE_GRAND_CHILD);
        scanner.put(VALUE_CHILD);
        scanner.put(VALUE_ANCESTOR);

        assertThat(scanner.types(), containsInAnyOrder(Ancestor.class, Child.class, GrandChild.class));
    }

    static BiFunction<Collection<ValueHolder>, Integer, Boolean> mockVisitorFn(BiFunction<Collection<ValueHolder>, Integer, Boolean> visitor) {
        //noinspection unchecked
        return mock(BiFunction.class, delegatesTo(visitor));
    }

    @Data
    @AllArgsConstructor
    static class ValueHolder {
        Class<?> clazz;
        Integer value;
    }
}