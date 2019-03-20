/**
 * Module includes implementation of Impler and JarImpler from package {@code info.kgeorgiy.java.advanced.implementor}
 * from eponymous module
 *
 * @author Pavel Yatcheniy
 */
open module ru.ifmo.rain.yatcheniy.implementor {
    requires info.kgeorgiy.java.advanced.implementor;

    requires java.compiler;

    exports ru.ifmo.rain.yatcheniy.implementor;
}
