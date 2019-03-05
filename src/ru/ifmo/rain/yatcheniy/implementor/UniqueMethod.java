package ru.ifmo.rain.yatcheniy.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

class UniqueMethod {
    public final Method method;

    UniqueMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        return method.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UniqueMethod)) {
            return false;
        }
        UniqueMethod other = (UniqueMethod) obj;

        return (Objects.equals(method.getName(), other.method.getName())) &&
                (Arrays.equals(this.method.getParameterTypes(), other.method.getParameterTypes()));
    }
}
