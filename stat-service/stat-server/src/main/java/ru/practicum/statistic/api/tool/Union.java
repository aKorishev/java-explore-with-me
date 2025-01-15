package ru.practicum.statistic.api.tool;

import lombok.Getter;

import java.util.Optional;
import java.util.function.Function;

@Getter
public class Union<T1 extends Object, T2> {
    private final Optional<T1> value1;
    private final Optional<T2> value2;

    private Union(Optional<T1> value1, Optional<T2> value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public static <T1, T2> Union<T1, T2> ofValue1(T1 value) {
        return new Union<>(Optional.of(value), Optional.empty());
    }

    public static <T1, T2> Union<T1, T2> ofValue2(T2 value) {
        return new Union<>(Optional.empty(), Optional.of(value));
    }

    public <V> Optional<V> map(Function<T1, V> func1, Function<T2, V> func2) {
        return value1.map(func1::apply)
                .or(() -> value2.map(func2::apply));

    }

    public boolean hasValue1() {
        return value1.isPresent();
    }

    public boolean hasValue2() {
        return value2.isPresent();
    }
}
