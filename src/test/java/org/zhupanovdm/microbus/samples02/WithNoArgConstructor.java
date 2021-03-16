package org.zhupanovdm.microbus.samples02;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class WithNoArgConstructor {
    private Integer i;
    protected String s;
    public Double d;

    With3ArgConstructor factoryMethod(Integer i, String s, Double d) {
        return new With3ArgConstructor(i, s, d);
    }
}
