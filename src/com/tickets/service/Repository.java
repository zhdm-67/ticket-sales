package com.tickets.service;

import com.tickets.domain.Identifiable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Обобщённый репозиторий для хранения сущностей по идентификатору. */
public class Repository<T extends Identifiable> {
    private final Map<Long, T> items = new LinkedHashMap<>();

    public void add(T item) {
        Objects.requireNonNull(item, "item");
        if (items.containsKey(item.getId()))
            throw new IllegalArgumentException("Объект с id=" + item.getId() + " уже существует");
        items.put(item.getId(), item);
    }

    public boolean remove(long id) { return items.remove(id) != null; }

    public Optional<T> findById(long id) { return Optional.ofNullable(items.get(id)); }

    public List<T> findAll() { return new ArrayList<>(items.values()); }

    public List<T> findWhere(Predicate<T> p) {
        return items.values().stream().filter(p).collect(Collectors.toList());
    }

    public int size() { return items.size(); }
    public boolean isEmpty() { return items.isEmpty(); }
}