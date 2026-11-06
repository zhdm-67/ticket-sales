package com.tickets.ui;

import com.tickets.domain.*;
import com.tickets.exception.CapacityExceededException;
import com.tickets.service.TicketService;
import com.tickets.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final TicketService service;
    private final FileStorage storage;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(TicketService service, FileStorage storage) {
        this.service = service;
        this.storage = storage;
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1 -> createEvent();
                    case 2 -> listEvents();
                    case 3 -> sellTicket();
                    case 4 -> returnTicket();
                    case 5 -> showEventReport();
                    case 6 -> listAllTickets();
                    case 7 -> showSales();
                    case 8 -> saveData();
                    case 9 -> loadData();
                    case 0 -> running = false;
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите число.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            } finally {
                // требование: блок finally — гарантируем разделитель после операции
                System.out.println();
            }
        }
        System.out.println("Выход. Не забудьте сохранить данные (пункт 8).");
    }

    private void printMenu() {
        System.out.println("============ МЕНЮ ============");
        System.out.println("1. Создать мероприятие");
        System.out.println("2. Список мероприятий");
        System.out.println("3. Купить билет");
        System.out.println("4. Вернуть билет");
        System.out.println("5. Отчёт по мероприятию");
        System.out.println("6. Все билеты");
        System.out.println("7. История продаж");
        System.out.println("8. Сохранить в файл");
        System.out.println("9. Загрузить из файла");
        System.out.println("0. Выход");
        System.out.print("> ");
    }

    private void createEvent() {
        System.out.print("Название: ");
        String name = scanner.nextLine();
        System.out.print("Дата (yyyy-MM-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Вместимость: ");
        int cap = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Цена STANDARD: ");
        double s = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Цена VIP: ");
        double v = Double.parseDouble(scanner.nextLine().trim());

        Event e = service.createEvent(name, date, cap, s, v);
        System.out.println("Создано: " + e);
    }

    private void listEvents() {
        List<Event> all = service.getAllEvents();
        if (all.isEmpty()) { System.out.println("Мероприятий нет."); return; }
        all.forEach(System.out::println);
    }

    private void sellTicket() throws CapacityExceededException {
        System.out.print("ID мероприятия: ");
        long eid = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Имя покупателя: ");
        String buyer = scanner.nextLine();
        System.out.print("Категория (STANDARD/VIP): ");
        String cat = scanner.nextLine().trim().toUpperCase();
        System.out.print("Скидка % (0 если нет): ");
        double disc = Double.parseDouble(scanner.nextLine().trim());

        Sale s = service.sellTicket(eid, buyer, cat, disc);
        System.out.println("Продано: " + s);
    }

    private void returnTicket() {
        System.out.print("ID билета: ");
        long tid = Long.parseLong(scanner.nextLine().trim());
        Sale s = service.returnTicket(tid);
        System.out.println("Возврат: " + s);
    }

    private void showEventReport() {
        System.out.print("ID мероприятия: ");
        long eid = Long.parseLong(scanner.nextLine().trim());
        System.out.println(service.generateEventReport(eid));
    }

    private void listAllTickets() {
        service.getAllTickets().forEach(System.out::println);
    }

    private void showSales() {
        service.getAllSales().forEach(System.out::println);
    }

    private void saveData() throws IOException {
        storage.save(service.getAllEvents(), service.getAllTickets(), service.getAllSales());
        System.out.println("Сохранено.");
    }

    private void loadData() throws IOException {
        // Очищаем — создаём новый сервис было бы правильнее, но в демо
        // подразумевается загрузка при старте; повторная загрузка не поддерживается.
        System.out.println("Загрузка выполняется при старте приложения.");
    }
}