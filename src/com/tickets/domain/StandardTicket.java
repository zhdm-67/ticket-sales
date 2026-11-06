package com.tickets.domain;

public class StandardTicket extends Ticket {
    public StandardTicket(long id, long eventId, double basePrice) {
        super(id, eventId, basePrice);
    }

    /** Переопределение: стандартный билет = базовая цена. */
    @Override public double calculatePrice() { return getBasePrice(); }

    @Override public String getCategory() { return "STANDARD"; }
}