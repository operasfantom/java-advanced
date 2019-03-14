package ru.ifmo.rain.yatcheniy.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@link Method}'s wrapper. It needs to more precise comparing {@link Method}s against default behaviour
 */
class UniqueMethod {
    private final Method method;

    /**
     * Instantiates a new Unique method.
     *
     * @param method the method
     */
    UniqueMethod(Method method) {
        this.method = method;
    }

    /**
     * Gets method.
     *
     * @return the method
     */
    Method getMethod() {
        return method;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for more precise hash base on its name
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return method.getName().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Base on its names and parameter's types
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
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
