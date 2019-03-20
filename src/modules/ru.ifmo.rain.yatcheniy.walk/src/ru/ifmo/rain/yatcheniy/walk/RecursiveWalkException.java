package ru.ifmo.rain.yatcheniy.walk;

class RecursiveWalkException extends Exception {
    RecursiveWalkException(String message) {
        super(message);
    }

    RecursiveWalkException(String format, Exception e) {
        super(format, e);
    }
}
