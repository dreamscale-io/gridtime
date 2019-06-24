package com.dreamscale.htmflow.core.gridtime.machine.commons;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class DefaultCollections {

    private static final LinkedHashMap<?,?> EMPTY_MAP = new LinkedHashMap<>();
    private static final LinkedHashSet<?> EMPTY_SET = new LinkedHashSet<>();
    private static final ArrayList<?> EMPTY_LIST = new ArrayList<>();

    public static <T> LinkedHashSet<T> toSet(T ... items) {
        LinkedHashSet<T> set = new LinkedHashSet<>();

        for (T item : items) {
            set.add(item);
        }

        return set;
    }


    public static <K, V> LinkedHashMap<K, V> toMap(K key, V value) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        map.put(key, value);

        return map;
    }

    public static <T> List<T> emptyList() {
        return (List<T>) EMPTY_LIST;
    }

    public static <T> Set<T> emptySet() {
        return (Set<T>) EMPTY_SET;
    }

    public static <K, V> Map<K,V> emptyMap() {
        return (Map<K,V>) EMPTY_MAP;
    }

    public static <T> List<T> toList(T ... items) {
        ArrayList<T> list = new ArrayList<T>();

        for (T item : items) {
            list.add(item);
        }

        return list;
    }


    public static <T> LinkedHashSet<T> set() {
        return new LinkedHashSet<>();
    }


    public static <K, V> LinkedHashMap<K, V> map() {
        return new LinkedHashMap<>();
    }

    public static <K, V> MultiValueMap<K, V> multiMap() {
        return new LinkedMultiValueMap<>();
    }

    public static <T> List<T> list() {
        return new ArrayList<T>();
    }


    public static <T> LinkedList<T> queueList() {
        return new LinkedList<>();
    }
}
