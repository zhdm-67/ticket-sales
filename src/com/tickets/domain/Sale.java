package com.tickets.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** Запись о продаже / возврате (для истории и отчётов). */
public class Sale implements Identifiable {
    public enum Type { PURCHASE, RETURN }

    private final long id;
    private final long ticketId;
    private final String buyerName;
    private final double amount;           // >0 продажа, <0 возврат
    private final LocalDateTime timestamp;
    private final Type type;

    public Sale(long id, long ticketId, String buyerName, double amount,
                LocalDateTime timestamp, Type type) {
        this.id = id;
        this.ticketId = ticketId;
        this.buyerName = buyerName;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
    }

    @Override public long getId() { return id; }
    public long getTicketId() { return ticketId; }
    public String getBuyerName() { return buyerName; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Type getType() { return type; }

    @Override
    public String toString() {
        return String.format("#%d %s билет=%d покупатель=%s сумма=%.2f %s",
                id, type, ticketId, buyerName, amount, timestamp);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Sale && ((Sale) o).id == id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}