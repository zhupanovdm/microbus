package org.zhupanovdm.microbus.core.di;

import lombok.Data;
import lombok.ToString;
import org.zhupanovdm.microbus.CommonUtils;
import org.zhupanovdm.microbus.core.annotation.Inject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.zhupanovdm.microbus.CommonUtils.isDefined;
import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.PERMISSIVE_ID;

@Data
@ToString
public class UnitQuery {
    private final String id;
    private final Class<?> type;
    private final String name;
    private final Set<Option> options;

    public UnitQuery(String id, Class<?> type, String name, Option ...options) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.options = EnumSet.noneOf(Option.class);
        this.options.addAll(Arrays.asList(options));
    }

    public boolean matches(@Nullable UnitHolder unitHolder) {
        if (unitHolder == null)
            return false;

        boolean isTypeMatches = type == null || (hasOption(EXACT_TYPE) ? type.equals(unitHolder.getType()) : type.isAssignableFrom(unitHolder.getType()));
        if (!isTypeMatches)
            return false;

        if (CommonUtils.isDefined(id) && !hasOption(PERMISSIVE_ID) && !id.equals(unitHolder.getId()))
            return false;

        return !CommonUtils.isDefined(name) || name.equals(unitHolder.getName());
    }

    public boolean hasOption(Option option) {
        return options.contains(option);
    }

    public static UnitQueryBuilder create() {
        return new UnitQueryBuilder();
    }

    public static class UnitQueryBuilder {
        private String id;
        private Class<?> type;
        private String name;
        private final Set<Option> options = EnumSet.noneOf(Option.class);

        public UnitQuery build() {
            return new UnitQuery(id, type, name, options.toArray(Option[]::new));
        }

        public UnitQueryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UnitQueryBuilder type(Class<?> type) {
            this.type = type;
            return this;
        }

        public UnitQueryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UnitQueryBuilder options(Option ...options) {
            this.options.addAll(Arrays.asList(options));
            return this;
        }

        public UnitQueryBuilder from(Field field) {
            id = field.getName();
            type = field.getType();
            options.add(PERMISSIVE_ID);
            return this;
        }

        public UnitQueryBuilder from(Parameter param) {
            type = param.getType();
            return this;
        }

        public UnitQueryBuilder from(Inject inject) {
            if (isDefined(inject.value()))
                id = inject.value();
            if (!inject.type().equals(Void.class))
                type = inject.type();
            return this;
        }

    }

    public enum Option {
        PERMISSIVE_ID,
        EXACT_TYPE
    }

}