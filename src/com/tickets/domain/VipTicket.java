package com.tickets.domain;

public class VipTicket extends Ticket {
    private static final double VIP_MULTIPLIER = 1.6;
    private static final double LOUNGE_FEE = 50.0;

    private final boolean loungeAccess;

    public VipTicket(long id, long eventId, double basePrice, boolean loungeAccess) {
        super(id, eventId, basePrice);
        this.loungeAccess = loungeAccess;
    }

    public boolean hasLoungeAccess() { return loungeAccess; }

    /** Переопределение: VIP-билет = базовая цена × 1.6 (+ доступ в лаунж). */
    @Override
    public double calculatePrice() {
        double price = getBasePrice() * VIP_MULTIPLIER;
        if (loungeAccess) price += LOUNGE_FEE;
        return price;
    }

    @Override public String getCategory() {
        return loungeAccess ? "VIP+" : "VIP";
    }
}