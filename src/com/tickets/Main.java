package com.tickets;

import com.tickets.service.TicketService;
import com.tickets.storage.FileStorage;
import com.tickets.ui.ConsoleUI;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TicketService service = new TicketService();
        FileStorage storage = new FileStorage("data");

        try {
            service.loadData(storage.loadEvents(), storage.loadTickets(), storage.loadSales());
            System.out.println("Данные загружены из файлов.");
        } catch (Exception e) {
            System.out.println("Не удалось загрузить данные: " + e.getMessage());
        }

        new ConsoleUI(service, storage).start();
    }
}