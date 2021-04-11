package org.zhupanovdm.microbus.core.di;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.zhupanovdm.microbus.core.reflector.ClassMappedValueScanner;
import org.zhupanovdm.microbus.util.CommonUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.EXACT_TYPE;
import static org.zhupanovdm.microbus.core.di.UnitQuery.Option.PERMISSIVE_ID;
import static org.zhupanovdm.microbus.util.CommonUtils.doWithLock;

@Slf4j
@ThreadSafe
public class UnitRegistry {
    private final Map<String, UnitHolder> ids = new HashMap<>();
    private final ClassMappedValueScanner<UnitHolder> types = new ClassMappedValueScanner<>(UnitHolder::getType);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(@NonNull UnitHolder unit) {
        doWithLock(lock.writeLock(), () -> {
            UnitHolder registered = ids.get(unit.getId());
            if (registered == null) {
                types.put(unit);
                return ids.put(unit.getId(), unit);
            }
            log.error("Unit with the same id {} already registered {}", registered.getId(), registered.getName());
            throw new IllegalArgumentException("Unit with the same id is already registered " + unit);
        });
    }

    public Optional<UnitHolder> request(@NonNull UnitQuery query) {
        return doWithLock(lock.readLock(), () -> searchMatchingUnit(query));
    }

    private Optional<UnitHolder> searchMatchingUnit(UnitQuery query) {
        if (CommonUtils.isDefined(query.getId())) {
            UnitHolder unitHolder = ids.get(query.getId());
            if (query.matches(unitHolder))
                return Optional.of(unitHolder);
            if (!query.hasOption(PERMISSIVE_ID))
                return Optional.empty();
        }

        Set<UnitHolder> result = new HashSet<>();
        types.scan(query.getType(), (units, integer) -> {
            result.addAll(units);
            return !query.hasOption(EXACT_TYPE);
        });

        if (result.size() > 1) {
            log.error("Ambiguous unit query {} found: {}", query, result);
            throw new IllegalStateException("Ambiguous unit query");
        }

        return CommonUtils.anyOf(result);
    }

}
