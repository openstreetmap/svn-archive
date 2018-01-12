/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openstreetmap.josm.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.openstreetmap.josm.tools.MultiMap;

/**
 * Registry of subscribers to a single event bus.
 *
 * @author Colin Decker
 */
final class SubscriberRegistry {

  /**
   * All registered subscribers, indexed by event type.
   *
   * <p>The {@link CopyOnWriteArraySet} values make it easy and relatively lightweight to get an
   * immutable snapshot of all current subscribers to an event without any locking.
   */
  private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers =
    new ConcurrentHashMap<>();

  /** The event bus this registry belongs to. */
  private final EventBus bus;

  SubscriberRegistry(EventBus bus) {
    this.bus = Objects.requireNonNull(bus);
  }

  /** Registers all subscriber methods on the given listener object. */
  void register(Object listener) {
    MultiMap<Class<?>, Subscriber> listenerMethods = findAllSubscribers(listener);

    for (Entry<Class<?>, Set<Subscriber>> entry : listenerMethods.entrySet()) {
      Class<?> eventType = entry.getKey();
      Collection<Subscriber> eventMethodsInListener = entry.getValue();

      CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);

      if (eventSubscribers == null) {
        CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet<>();
        eventSubscribers =
            firstNonNull(subscribers.putIfAbsent(eventType, newSet), newSet);
      }

      eventSubscribers.addAll(eventMethodsInListener);
    }
  }

  /** Unregisters all subscribers on the given listener object. */
  void unregister(Object listener) {
    MultiMap<Class<?>, Subscriber> listenerMethods = findAllSubscribers(listener);

    for (Entry<Class<?>, Set<Subscriber>> entry : listenerMethods.entrySet()) {
      Class<?> eventType = entry.getKey();
      Collection<Subscriber> listenerMethodsForType = entry.getValue();

      CopyOnWriteArraySet<Subscriber> currentSubscribers = subscribers.get(eventType);
      if (currentSubscribers == null || !currentSubscribers.removeAll(listenerMethodsForType)) {
        // if removeAll returns true, all we really know is that at least one subscriber was
        // removed... however, barring something very strange we can assume that if at least one
        // subscriber was removed, all subscribers on listener for that event type were... after
        // all, the definition of subscribers on a particular class is totally static
        throw new IllegalArgumentException(
            "missing event subscriber for an annotated method. Is " + listener + " registered?");
      }

      // don't try to remove the set if it's empty; that can't be done safely without a lock
      // anyway, if the set is empty it'll just be wrapping an array of length 0
    }
  }

  Set<Subscriber> getSubscribersForTesting(Class<?> eventType) {
    return firstNonNull(subscribers.get(eventType), new HashSet<Subscriber>());
  }

  /**
   * Gets an iterator representing an immutable snapshot of all subscribers to the given event at
   * the time this method is called.
   */
  Iterator<Subscriber> getSubscribers(Object event) {
    Set<Class<?>> eventTypes = flattenHierarchy(event.getClass());

    List<Subscriber> subscriberList = new ArrayList<>(eventTypes.size());

    for (Class<?> eventType : eventTypes) {
      CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
      if (eventSubscribers != null) {
        // eager no-copy snapshot
        subscriberList.addAll(eventSubscribers);
      }
    }

    return Collections.unmodifiableList(subscriberList).iterator();
  }

  /**
   * Returns all subscribers for the given listener grouped by the type of event they subscribe to.
   */
  private MultiMap<Class<?>, Subscriber> findAllSubscribers(Object listener) {
    MultiMap<Class<?>, Subscriber> methodsInListener = new MultiMap<>();
    Class<?> clazz = listener.getClass();
    for (Method method : getAnnotatedMethods(clazz)) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Class<?> eventType = parameterTypes[0];
      methodsInListener.put(eventType, Subscriber.create(bus, listener, method));
    }
    return methodsInListener;
  }

  private static List<Method> getAnnotatedMethods(Class<?> clazz) {
    return getAnnotatedMethodsNotCached(clazz);
  }

  private static List<Method> getAnnotatedMethodsNotCached(Class<?> clazz) {
    Set<? extends Class<?>> supertypes = getClassesAndInterfaces(clazz);
    Map<MethodIdentifier, Method> identifiers = new HashMap<>();
    for (Class<?> supertype : supertypes) {
      for (Method method : supertype.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Subscribe.class) && !method.isSynthetic()) {
          // TODO(cgdecker): Should check for a generic parameter type and error out
          Class<?>[] parameterTypes = method.getParameterTypes();
          if (parameterTypes.length != 1) {
              throw new IllegalArgumentException(String.format(
                    "Method %s has @Subscribe annotation but has %s parameters."
                  + "Subscriber methods must have exactly 1 parameter.",
              method,
              parameterTypes.length));
          }

          MethodIdentifier ident = new MethodIdentifier(method);
          if (!identifiers.containsKey(ident)) {
            identifiers.put(ident, method);
          }
        }
      }
    }
    return new ArrayList<>(identifiers.values());
  }

  /** Global cache of classes to their flattened hierarchy of supertypes. */
  private static final Map<Class<?>, Set<Class<?>>> flattenHierarchyCache = new HashMap<>();

  /**
   * Flattens a class's type hierarchy into a set of {@code Class} objects including all
   * superclasses (transitively) and all interfaces implemented by these superclasses.
   */
  static Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
      return flattenHierarchyCache.computeIfAbsent(
              concreteClass, SubscriberRegistry::getClassesAndInterfaces);
  }

  private static final class MethodIdentifier {

    private final String name;
    private final List<Class<?>> parameterTypes;

    MethodIdentifier(Method method) {
      this.name = method.getName();
      this.parameterTypes = Arrays.asList(method.getParameterTypes());
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, parameterTypes);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof MethodIdentifier) {
        MethodIdentifier ident = (MethodIdentifier) o;
        return name.equals(ident.name) && parameterTypes.equals(ident.parameterTypes);
      }
      return false;
    }
  }
  
  /**
   * Returns the first of two given parameters that is not {@code null}, if either is, or otherwise
   * throws a {@link NullPointerException}.
   *
   * <p>To find the first non-null element in an iterable, use {@code Iterables.find(iterable,
   * Predicates.notNull())}. For varargs, use {@code Iterables.find(Arrays.asList(a, b, c, ...),
   * Predicates.notNull())}, static importing as necessary.
   *
   * <p><b>Note:</b> if {@code first} is represented as an {@link Optional}, this can be
   * accomplished with {@link Optional#or(Object) first.or(second)}. That approach also allows for
   * lazy evaluation of the fallback instance, using {@link Optional#or(Supplier)
   * first.or(supplier)}.
   *
   * @return {@code first} if it is non-null; otherwise {@code second} if it is non-null
   * @throws NullPointerException if both {@code first} and {@code second} are null
   * @since 18.0 (since 3.0 as {@code Objects.firstNonNull()}).
   */
  static <T> T firstNonNull(T first, T second) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    throw new NullPointerException("Both parameters are null");
  }
  
  private static Set<Class<?>> getClassesAndInterfaces(Class<?> clazz) {
      Set<Class<?>> result = new HashSet<>();
      Class<?> c = clazz;
      while (c != null) {
          result.add(c);
        for (Set<Class<?>> interfaces : Arrays.stream(c.getInterfaces()).map(
                SubscriberRegistry::getClassesAndInterfaces).collect(Collectors.toList())) {
              result.addAll(interfaces);
          }
          c = c.getSuperclass();
      }
      return result;
  }
}
