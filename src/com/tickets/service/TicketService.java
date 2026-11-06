package com.tickets.service;

import com.tickets.domain.*;
import com.tickets.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TicketService {
    private final Repository<Event> events = new Repository<>();
    private final Repository<Ticket> tickets = new Repository<>();
    private final List<Sale> sales = new ArrayList<>();

    private long nextEventId = 1;
    private long nextTicketId = 1;
    private long nextSaleId = 1;

    // ---------- Создание мероприятия ----------
    public Event createEvent(String name, LocalDate date, int capacity,
                             double stdPrice, double vipPrice) {
        Event ev = new Event(nextEventId++, name, date, capacity, stdPrice, vipPrice);
        events.add(ev);

        // создаём билеты сразу: ~10% VIP, остальное STANDARD
        int vipCount = Math.max(1, capacity / 10);
        int stdCount = capacity - vipCount;
        for (int i = 0; i < stdCount; i++)
            tickets.add(new StandardTicket(nextTicketId++, ev.getId(), stdPrice));
        for (int i = 0; i < vipCount; i++)
            tickets.add(new VipTicket(nextTicketId++, ev.getId(), vipPrice, i % 2 == 0));

        return ev;
    }

    // ---------- Продажа (перегрузка) ----------
    public Sale sellTicket(long eventId, String buyerName)
            throws CapacityExceededException {
        return sellTicket(eventId, buyerName, "STANDARD", 0);
    }

    public Sale sellTicket(long eventId, String buyerName, String category)
            throws CapacityExceededException {
        return sellTicket(eventId, buyerName, category, 0);
    }

    public Sale sellTicket(long eventId, String buyerName, String category, double discountPercent)
            throws CapacityExceededException {

        Event event = events.findById(eventId)
                .orElseThrow(() -> new ValidationException("Мероприятие #" + eventId + " не найдено"));

        long sold = tickets.findWhere(t -> t.getEventId() == eventId
                && t.getStatus() == TicketStatus.SOLD).size();
        if (sold >= event.getCapacity())
            throw new CapacityExceededException(
                    "На мероприятии '" + event.getName() + "' нет свободных мест ("
                            + sold + "/" + event.getCapacity() + ")");

        String prefix = category.equalsIgnoreCase("VIP") ? "VIP" : "STANDARD";
        Optional<Ticket> opt = tickets.findWhere(t ->
                        t.getEventId() == eventId
                                && t.getStatus() == TicketStatus.AVAILABLE
                                && t.getCategory().startsWith(prefix))
                .stream().findFirst();

        if (opt.isEmpty())
            throw new TicketNotAvailableException(
                    "Нет свободных билетов категории " + category + " на событие #" + eventId);

        Ticket t = opt.get();
        t.markSold(buyerName);
        double amount = t.calculatePrice(discountPercent);

        Sale sale = new Sale(nextSaleId++, t.getId(), buyerName, amount,
                LocalDateTime.now(), Sale.Type.PURCHASE);
        sales.add(sale);
        return sale;
    }

    // ---------- Возврат ----------
    public Sale returnTicket(long ticketId) {
        Ticket t = tickets.findById(ticketId)
                .orElseThrow(() -> new TicketNotAvailableException("Билет #" + ticketId + " не найден"));
        if (t.getStatus() != TicketStatus.SOLD)
            throw new TicketNotAvailableException("Билет #" + ticketId + " не был продан");

        String buyer = t.getBuyerName();
        double refund = t.calculatePrice() * 0.8; // удерживаем 20%
        t.markReturned();

        Sale sale = new Sale(nextSaleId++, t.getId(), buyer, -refund,
                LocalDateTime.now(), Sale.Type.RETURN);
        sales.add(sale);
        return sale;
    }

    // ---------- Обобщённый метод ----------
    public <T extends Ticket> double totalRevenue(List<T> list) {
        return list.stream()
                .filter(t -> t.getStatus() == TicketStatus.SOLD)
                .mapToDouble(Ticket::calculatePrice)
                .sum();
    }

    // ---------- Содержательная операция: отчёт по мероприятию ----------
    public String generateEventReport(long eventId) {
        Event event = events.findById(eventId)
                .orElseThrow(() -> new ValidationException("Мероприятие не найдено"));

        List<Ticket> eventTickets = tickets.findWhere(t -> t.getEventId() == eventId);
        long sold = eventTickets.stream().filter(t -> t.getStatus() == TicketStatus.SOLD).count();
        long available = eventTickets.stream().filter(t -> t.getStatus() == TicketStatus.AVAILABLE).count();
        long vipSold = eventTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.SOLD && t.getCategory().startsWith("VIP"))
                .count();
        long stdSold = sold - vipSold;

        double revenue = totalRevenue(eventTickets);

        StringBuilder sb = new StringBuilder();
        sb.append("========== Отчёт по мероприятию ==========\n")
          .append("Название: ").append(event.getName()).append("\n")
          .append("Дата:     ").append(event.getDate()).append("\n")
          .append("Вместимость: ").append(event.getCapacity()).append("\n")
          .append("Продано:     ").append(sold).append("\n")
          .append("Свободно:    ").append(available).append("\n")
          .append("STANDARD продано: ").append(stdSold).append("\n")
          .append("VIP продано:      ").append(vipSold).append("\n")
          .append("Выручка:     ").append(String.format("%.2f", revenue)).append(" руб.\n")
          .append("==========================================\n");
        return sb.toString();
    }

    // ---------- Геттеры для UI/Storage ----------
    public List<Event> getAllEvents() { return events.findAll(); }
    public List<Ticket> getAllTickets() { return tickets.findAll(); }
    public List<Sale> getAllSales() { return new ArrayList<>(sales); }

    public Optional<Event> findEvent(long id) { return events.findById(id); }
    public Optional<Ticket> findTicket(long id) { return tickets.findById(id); }

    // ---------- Загрузка данных из хранилища ----------
    public void loadData(List<Event> evList, List<Ticket> tkList, List<Sale> slList) {
        for (Event e : evList) {
            events.add(e);
            nextEventId = Math.max(nextEventId, e.getId() + 1);
        }
        for (Ticket t : tkList) {
            tickets.add(t);
            nextTicketId = Math.max(nextTicketId, t.getId() + 1);
        }
        sales.addAll(slList);
        for (Sale s : slList)
            nextSaleId = Math.max(nextSaleId, s.getId() + 1);
    }
}