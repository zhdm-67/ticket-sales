package com.tickets.domain;

import com.tickets.exception.ValidationException;

import java.time.LocalDate;
import java.util.Objects;

public class Event implements Identifiable {
    private final long id;
    private String name;
    private LocalDate date;
    private final int capacity;
    private double standardPrice;
    private double vipPrice;

    public Event(long id, String name, LocalDate date, int capacity,
                 double standardPrice, double vipPrice) {
        if (capacity <= 0) throw new ValidationException("Вместимость должна быть положительной");
        this.id = id;
        setName(name);
        setDate(date);
        this.capacity = capacity;
        setStandardPrice(standardPrice);
        setVipPrice(vipPrice);
    }

    @Override public long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public int getCapacity() { return capacity; }
    public double getStandardPrice() { return standardPrice; }
    public double getVipPrice() { return vipPrice; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Название события обязательно");
        if (name.contains(";"))
            throw new ValidationException("Название не может содержать ';'");
        this.name = name.trim();
    }

    public void setDate(LocalDate date) {
        if (date == null) throw new ValidationException("Дата обязательна");
        this.date = date;
    }

    public void setStandardPrice(double price) {
        if (price < 0) throw new ValidationException("Цена STANDARD не может быть отрицательной");
        this.standardPrice = price;
    }

    public void setVipPrice(double price) {
        if (price < 0) throw new ValidationException("Цена VIP не может быть отрицательной");
        this.vipPrice = price;
    }

    @Override
    public String toString() {
        return String.format("Событие#%d '%s' %s, вместимость=%d, STANDARD=%.2f, VIP=%.2f",
                id, name, date, capacity, standardPrice, vipPrice);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Event && ((Event) o).id == id;
    }
    @Override public int hashCode() { return Objects.hash(id); }
}