package org.zhupanovdm.microbus.core.di;

import lombok.Data;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

@Data
public class DependencyQualifierProvider {
    private final DependencyQualifier<Parameter> arguments;
    private final DependencyQualifier<Field> fields;
    private final DependencyQualifier<Executable> executableTargets;

    public DependencyQualifierProvider(DependencyQualifier<Parameter> arguments, DependencyQualifier<Field> fields, DependencyQualifier<Executable> executableTargets) {
        this.arguments = arguments;
        this.fields = fields;
        this.executableTargets = executableTargets;
    }

    public void define(Parameter obj, UnitQuery query) {
        arguments.define(obj, query);
    }

    public void define(Field obj, UnitQuery query) {
        fields.define(obj, query);
    }

    public void define(Executable obj, UnitQuery query) {
        executableTargets.define(obj, query);
    }

    public UnitQuery qualify(Parameter obj) {
        return arguments.qualify(obj);
    }

    public UnitQuery qualify(Field obj) {
        return fields.qualify(obj);
    }

    public UnitQuery qualify(Executable obj) {
        return executableTargets.qualify(obj);
    }

}
