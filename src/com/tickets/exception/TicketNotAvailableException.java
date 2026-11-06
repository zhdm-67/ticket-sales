// src/main/java/com/tickets/exception/TicketNotAvailableException.java
package com.tickets.exception;

public class TicketNotAvailableException extends RuntimeException {
    public TicketNotAvailableException(String message) { super(message); }
}