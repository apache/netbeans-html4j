/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.java.html.json.tests;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.java.html.json.Models;

final class SimpleMap<K,V> implements Map<K,V> {
    private final List<E<K,V>> entries;

    private SimpleMap() {
        this.entries = Models.asList();
    }

    public static <K,V> Map<K,V> empty() {
        return new SimpleMap<K,V>();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private int indexOf(Object obj, int index) {
        for (int i = 0; i < entries.size(); i++) {
            E<K,V> arr = entries.get(i);
            if (equals(index == 0 ? arr.key : arr.value, obj)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key, 0) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        return indexOf(value, 1) != -1;
    }

    @Override
    public V get(Object key) {
        int at = indexOf(key, 0);
        return at == -1 ? null : entries.get(at).value;
    }

    @Override
    public V put(K key, V value) {
        int at = indexOf(key, 0);
        if (at == -1) {
            entries.add(new E<K,V>(key, value));
            return null;
        } else {
            E<K,V> arr = entries.get(at);
            return arr.setValue(value);
        }
    }

    @Override
    public V remove(Object key) {
        int at = indexOf(key, 0);
        if (at == -1) {
            return null;
        }
        return entries.remove(at).value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Set<K> keySet() {
        List<K> keys = Models.asList();
        for (E<K, V> entry : entries) {
            keys.add(entry.key);
        }
        return new ROSet<K>(keys);
    }

    @Override
    public Collection<V> values() {
        List<V> values = Models.asList();
        for (E<K, V> entry : entries) {
            values.add(entry.value);
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new ROSet<Entry<K, V>>(entries);
    }

    private static final class E<K,V> implements Entry<K,V> {
        final K key;
        V value;

        E(K key, V v) {
            this.key = key;
            this.value = v;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V prev = this.value;
            this.value = value;
            return prev;
        }
    }

    private static final class ROSet<T> implements Set<T> {
        private final Collection<? extends T> delegate;

        ROSet(Collection<? extends T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            final Iterator<? extends T> it = delegate.iterator();
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public T next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
