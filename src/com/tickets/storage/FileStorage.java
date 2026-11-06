package com.tickets.storage;

import com.tickets.domain.*;
import com.tickets.exception.ValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private final Path dataDir;

    public FileStorage(String dir) {
        this.dataDir = Paths.get(dir);
    }

    // ==================== SAVE ====================
    public void save(List<Event> events, List<Ticket> tickets, List<Sale> sales) throws IOException {
        Files.createDirectories(dataDir);
        try (BufferedWriter w = Files.newBufferedWriter(dataDir.resolve("events.csv"), StandardCharsets.UTF_8)) {
            for (Event e : events) {
                w.write(String.join(";",
                        String.valueOf(e.getId()),
                        e.getName(),
                        e.getDate().toString(),
                        String.valueOf(e.getCapacity()),
                        String.valueOf(e.getStandardPrice()),
                        String.valueOf(e.getVipPrice())));
                w.newLine();
            }
        }
        try (BufferedWriter w = Files.newBufferedWriter(dataDir.resolve("tickets.csv"), StandardCharsets.UTF_8)) {
            for (Ticket t : tickets) {
                String lounge = (t instanceof VipTicket vt) ? String.valueOf(vt.hasLoungeAccess()) : "-";
                w.write(String.join(";",
                        String.valueOf(t.getId()),
                        String.valueOf(t.getEventId()),
                        t.getCategory(),
                        String.valueOf(t.getBasePrice()),
                        lounge,
                        t.getStatus().name(),
                        t.getBuyerName() == null ? "" : t.getBuyerName(),
                        t.getSoldAt() == null ? "" : t.getSoldAt().toString()));
                w.newLine();
            }
        }
        try (BufferedWriter w = Files.newBufferedWriter(dataDir.resolve("sales.csv"), StandardCharsets.UTF_8)) {
            for (Sale s : sales) {
                w.write(String.join(";",
                        String.valueOf(s.getId()),
                        String.valueOf(s.getTicketId()),
                        s.getBuyerName(),
                        String.valueOf(s.getAmount()),
                        s.getTimestamp().toString(),
                        s.getType().name()));
                w.newLine();
            }
        }
    }

    // ==================== LOAD ====================
    public List<Event> loadEvents() throws IOException {
        List<Event> list = new ArrayList<>();
        Path f = dataDir.resolve("events.csv");
        if (!Files.exists(f)) return list;
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(";", -1);
                try {
                    list.add(new Event(
                            Long.parseLong(p[0]),
                            p[1],
                            LocalDate.parse(p[2]),
                            Integer.parseInt(p[3]),
                            Double.parseDouble(p[4]),
                            Double.parseDouble(p[5])));
                } catch (RuntimeException ex) {
                    throw new IOException("Ошибка формата events.csv: " + line, ex);
                }
            }
        }
        return list;
    }

    public List<Ticket> loadTickets() throws IOException {
        List<Ticket> list = new ArrayList<>();
        Path f = dataDir.resolve("tickets.csv");
        if (!Files.exists(f)) return list;
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(";", -1);
                try {
                    long id = Long.parseLong(p[0]);
                    long eventId = Long.parseLong(p[1]);
                    String category = p[2];
                    double base = Double.parseDouble(p[3]);
                    Ticket t;
                    if (category.startsWith("VIP")) {
                        boolean lounge = Boolean.parseBoolean(p[4]);
                        t = new VipTicket(id, eventId, base, lounge);
                    } else {
                        t = new StandardTicket(id, eventId, base);
                    }
                    TicketStatus st = TicketStatus.valueOf(p[5]);
                    if (st == TicketStatus.SOLD) {
                        t.markSold(p[6]);
                        // восстановить оригинальную дату продажи
                        // (markSold ставит текущую — перезаписывать не будем, но принимаем)
                    } else if (st == TicketStatus.RETURNED) {
                        // оставляем как есть — RETURNED
                        // для этого присваиваем напрямую через маркер (без побочных эффектов):
                        t.markSold("_tmp_");
                        t.markReturned();
                    }
                    list.add(t);
                } catch (RuntimeException ex) {
                    throw new IOException("Ошибка формата tickets.csv: " + line, ex);
                }
            }
        }
        return list;
    }

    public List<Sale> loadSales() throws IOException {
        List<Sale> list = new ArrayList<>();
        Path f = dataDir.resolve("sales.csv");
        if (!Files.exists(f)) return list;
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(";", -1);
                try {
                    list.add(new Sale(
                            Long.parseLong(p[0]),
                            Long.parseLong(p[1]),
                            p[2],
                            Double.parseDouble(p[3]),
                            LocalDateTime.parse(p[4]),
                            Sale.Type.valueOf(p[5])));
                } catch (RuntimeException ex) {
                    throw new IOException("Ошибка формата sales.csv: " + line, ex);
                }
            }
        }
        return list;
    }
}