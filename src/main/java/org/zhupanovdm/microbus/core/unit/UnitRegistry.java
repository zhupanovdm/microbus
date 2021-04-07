package org.zhupanovdm.microbus.core.unit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.utils.ClassMappedValueScanner;
import org.zhupanovdm.microbus.utils.CommonUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.unit.UnitQuery.Option.PERMISSIVE_ID;

@Slf4j
@ThreadSafe
public class UnitRegistry {
    private final Map<String, UnitHolder<?>> ids = new HashMap<>();
    private final ClassMappedValueScanner<UnitHolder<?>> types = new ClassMappedValueScanner<>(UnitHolder::getType);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(@NonNull UnitHolder<?> unit) {
        CommonUtils.doWithLock(lock.writeLock(), () -> {
            if (ids.containsKey(unit.getId())) {
                log.error("Mod with the same id already registered: {}", ids.get(unit.getId()));
                throw new IllegalArgumentException("Mod with the same id is already registered: " + unit);
            }
            types.put(unit);
            return ids.put(unit.getId(), unit);
        });
        log.debug("Registered {}", unit);
    }

    public Optional<UnitHolder<?>> request(@NonNull UnitQuery query) {
        return CommonUtils.doWithLock(lock.readLock(), () -> searchMatchingUnit(query));
    }

    private Optional<UnitHolder<?>> searchMatchingUnit(UnitQuery query) {
        if (CommonUtils.isDefined(query.getId())) {
            UnitHolder<?> unitHolder = ids.get(query.getId());
            if (query.matches(unitHolder))
                return Optional.of(unitHolder);
            if (!query.hasOption(PERMISSIVE_ID))
                return Optional.empty();
        }

        Set<UnitHolder<?>> result = new HashSet<>();
        types.scan(query.getType(), (units, integer) -> {
            result.addAll(units);
            return !query.hasOption(EXACT_TYPE);
        });

        if (result.size() > 1) {
            log.error("Ambiguous unit query {}", query);
            throw new IllegalStateException("Ambiguous unit query");
        }

        return CommonUtils.anyOf(result);
    }

}
