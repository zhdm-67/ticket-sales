// src/main/java/com/tickets/exception/CapacityExceededException.java
package com.tickets.exception;

/** CHECKED-исключение: нарушение ключевого правила предметной области. */
public class CapacityExceededException extends Exception {
    public CapacityExceededException(String message) { super(message); }
}