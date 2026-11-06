package com.tickets.test;

import com.tickets.domain.*;
import com.tickets.exception.CapacityExceededException;
import com.tickets.exception.TicketNotAvailableException;
import com.tickets.exception.ValidationException;
import com.tickets.service.TicketService;

import java.time.LocalDate;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        TicketService s = new TicketService();

        // 1. Корректные сценарии
        Event e = s.createEvent("RockFest", LocalDate.now().plusDays(30), 10, 1000, 3000);
        Sale sold1 = s.sellTicket(e.getId(), "Иван");
        assert sold1.getAmount() > 0 : "Цена должна быть положительной";
        Sale sold2 = s.sellTicket(e.getId(), "Мария", "VIP", 10);
        assert sold2.getAmount() > 0;
        assert s.generateEventReport(e.getId()).contains("Продано:     2");

        // 2. Возврат
        Sale ret = s.returnTicket(sold1.getTicketId());
        assert ret.getAmount() < 0 : "Возврат — отрицательная сумма";
        assert s.generateEventReport(e.getId()).contains("Продано:     1");

        // 3. Некорректные данные
        expectThrow(() -> s.createEvent("", LocalDate.now(), 10, 100, 100),
                ValidationException.class, "Пустое имя → ошибка");
        expectThrow(() -> s.sellTicket(999, "X"),
                ValidationException.class, "Несуществующее событие → ошибка");
        expectThrow(() -> s.returnTicket(999),
                TicketNotAvailableException.class, "Возврат несуществующего билета → ошибка");

        // 4. Переполнение вместимости
        Event small = s.createEvent("Tiny", LocalDate.now().plusDays(5), 2, 100, 200);
        s.sellTicket(small.getId(), "A");
        s.sellTicket(small.getId(), "B");
        expectThrow(() -> s.sellTicket(small.getId(), "C"),
                CapacityExceededException.class, "Продажа сверх вместимости → ошибка");

        System.out.println("✅ Все smoke-тесты пройдены");
    }

    private static void expectThrow(Runnable r, Class<? extends Throwable> cls, String msg) {
        try {
            r.run();
            throw new AssertionError("Не выброшено ожидаемое " + cls.getSimpleName() + ": " + msg);
        } catch (Throwable t) {
            if (!cls.isInstance(t))
                throw new AssertionError("Ожидалось " + cls.getSimpleName() + ", получено "
                        + t.getClass().getSimpleName() + " — " + msg);
            System.out.println("✔ " + msg + " (" + t.getMessage() + ")");
        }
    }
}