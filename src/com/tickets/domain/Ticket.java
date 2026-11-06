package com.tickets.domain;

import com.tickets.exception.TicketNotAvailableException;
import com.tickets.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Абстрактный базовый класс билета.
 * Реализует общую логику хранения и переходов состояний.
 */
public abstract class Ticket implements Identifiable {
    private final long id;
    private final long eventId;
    private final double basePrice;
    private TicketStatus status;
    private String buyerName;
    private LocalDateTime soldAt;

    protected Ticket(long id, long eventId, double basePrice) {
        if (basePrice < 0) throw new ValidationException("Базовая цена не может быть отрицательной");
        this.id = id;
        this.eventId = eventId;
        this.basePrice = basePrice;
        this.status = TicketStatus.AVAILABLE;
    }

    // --- Геттеры (инкапсуляция) ---
    @Override public long getId() { return id; }
    public long getEventId() { return eventId; }
    public double getBasePrice() { return basePrice; }
    public TicketStatus getStatus() { return status; }
    public String getBuyerName() { return buyerName; }
    public LocalDateTime getSoldAt() { return soldAt; }

    // --- Абстрактные методы (переопределение в наследниках) ---
    public abstract double calculatePrice();
    public abstract String getCategory();

    // --- Перегрузка: тот же метод с параметром скидки ---
    public double calculatePrice(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100)
            throw new ValidationException("Скидка должна быть в диапазоне [0;100]");
        return calculatePrice() * (1 - discountPercent / 100.0);
    }

    // --- Управление состоянием ---
    public void markSold(String buyerName) {
        if (status == TicketStatus.SOLD)
            throw new TicketNotAvailableException("Билет #" + id + " уже продан");
        if (buyerName == null || buyerName.isBlank())
            throw new ValidationException("Имя покупателя обязательно");
        this.status = TicketStatus.SOLD;
        this.buyerName = buyerName.trim();
        this.soldAt = LocalDateTime.now();
    }

    public void markReturned() {
        if (status != TicketStatus.SOLD)
            throw new TicketNotAvailableException("Билет #" + id + " не был продан — вернуть нельзя");
        this.status = TicketStatus.RETURNED;
        this.buyerName = null;
        this.soldAt = null;
    }

    public void markAvailable() {
        this.status = TicketStatus.AVAILABLE;
        this.buyerName = null;
        this.soldAt = null;
    }

    @Override
    public String toString() {
        return String.format("Билет#%d [%s] событие=%d базовая=%.2f статус=%s покупатель=%s",
                id, getCategory(), eventId, basePrice, status,
                buyerName == null ? "-" : buyerName);
    }
    
    /** Только для восстановления из файла (storage). */
    public void restoreState(TicketStatus status, String buyerName, LocalDateTime soldAt) {
        this.status = status;
        this.buyerName = buyerName;
        this.soldAt = soldAt;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        return id == ((Ticket) o).id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}