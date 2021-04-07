package org.zhupanovdm.microbus.core.unit;

import lombok.Data;
import lombok.ToString;
import org.zhupanovdm.microbus.utils.CommonUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.PERMISSIVE_ID;

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

    public boolean matches(@Nullable UnitHolder<?> unitHolder) {
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

    public static UnitQuery of(Field field) {
        return new UnitQuery(field.getName(), field.getType(), null, PERMISSIVE_ID);
    }

    public static UnitQuery of(Parameter arg) {
        return new UnitQuery(null, arg.getType(), null);
    }

    public enum Option {
        PERMISSIVE_ID,
        EXACT_TYPE
    }

}