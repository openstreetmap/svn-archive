/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openstreetmap.josm.eventbus.outside;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.eventbus.EventBus;
import org.openstreetmap.josm.eventbus.Subscribe;

/**
 * Test that EventBus finds the correct subscribers.
 *
 * <p>This test must be outside the c.g.c.eventbus package to test correctly.
 *
 * @author Louis Wasserman
 */
public class AnnotatedSubscriberFinderTests {

  private static final Object EVENT = new Object();

  private abstract static class AbstractEventBusTestParent<H> {
    abstract H createSubscriber();

    private H subscriber;

    H getSubscriber() {
      return subscriber;
    }

    @Before
    public void setUp() throws Exception {
      subscriber = createSubscriber();
      EventBus bus = new EventBus();
      bus.register(subscriber);
      bus.post(EVENT);
    }

    @After
    public void tearDown() throws Exception {
      subscriber = null;
    }
  }

  /*
   * We break the tests up based on whether they are annotated or abstract in the superclass.
   */
  public static class BaseSubscriberFinderTest
      extends AbstractEventBusTestParent<BaseSubscriberFinderTest.Subscriber> {
    static class Subscriber {
      final List<Object> nonSubscriberEvents = new ArrayList<>();
      final List<Object> subscriberEvents = new ArrayList<>();

      public void notASubscriber(Object o) {
        nonSubscriberEvents.add(o);
      }

      @Subscribe
      public void subscriber(Object o) {
        subscriberEvents.add(o);
      }
    }

    @Test
    public void testNonSubscriber() {
      assertTrue(getSubscriber().nonSubscriberEvents.isEmpty());
    }

    @Test
    public void testSubscriber() {
      assertTrue(getSubscriber().subscriberEvents.contains(EVENT));
    }

    @Override
    Subscriber createSubscriber() {
      return new Subscriber();
    }
  }

  public static class AnnotatedAndAbstractInSuperclassTest
      extends AbstractEventBusTestParent<AnnotatedAndAbstractInSuperclassTest.SubClass> {
    abstract static class SuperClass {
      @Subscribe
      public abstract void overriddenAndAnnotatedInSubclass(Object o);

      @Subscribe
      public abstract void overriddenInSubclass(Object o);
    }

    static class SubClass extends SuperClass {
      final List<Object> overriddenAndAnnotatedInSubclassEvents = new ArrayList<>();
      final List<Object> overriddenInSubclassEvents = new ArrayList<>();

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }

      @Override
      public void overriddenInSubclass(Object o) {
        overriddenInSubclassEvents.add(o);
      }
    }

    @Test
    public void testOverriddenAndAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenAndAnnotatedInSubclassEvents.contains(EVENT));
    }

    @Test
    public void testOverriddenNotAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenInSubclassEvents.contains(EVENT));
    }

    @Override
    SubClass createSubscriber() {
      return new SubClass();
    }
  }

  public static class AnnotatedNotAbstractInSuperclassTest
      extends AbstractEventBusTestParent<AnnotatedNotAbstractInSuperclassTest.SubClass> {
    static class SuperClass {
      final List<Object> notOverriddenInSubclassEvents = new ArrayList<>();
      final List<Object> overriddenNotAnnotatedInSubclassEvents = new ArrayList<>();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = new ArrayList<>();
      final List<Object> differentlyOverriddenNotAnnotatedInSubclassBadEvents =
          new ArrayList<>();
      final List<Object> differentlyOverriddenAnnotatedInSubclassBadEvents = new ArrayList<>();

      @Subscribe
      public void notOverriddenInSubclass(Object o) {
        notOverriddenInSubclassEvents.add(o);
      }

      @Subscribe
      public void overriddenNotAnnotatedInSubclass(Object o) {
        overriddenNotAnnotatedInSubclassEvents.add(o);
      }

      @Subscribe
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }

      @Subscribe
      public void differentlyOverriddenNotAnnotatedInSubclass(Object o) {
        // the subclass overrides this and does *not* call super.dONAIS(o)
        differentlyOverriddenNotAnnotatedInSubclassBadEvents.add(o);
      }

      @Subscribe
      public void differentlyOverriddenAnnotatedInSubclass(Object o) {
        // the subclass overrides this and does *not* call super.dOAIS(o)
        differentlyOverriddenAnnotatedInSubclassBadEvents.add(o);
      }
    }

    static class SubClass extends SuperClass {
      final List<Object> differentlyOverriddenNotAnnotatedInSubclassGoodEvents =
          new ArrayList<>();
      final List<Object> differentlyOverriddenAnnotatedInSubclassGoodEvents = new ArrayList<>();

      @Override
      public void overriddenNotAnnotatedInSubclass(Object o) {
        super.overriddenNotAnnotatedInSubclass(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        super.overriddenAndAnnotatedInSubclass(o);
      }

      @Override
      public void differentlyOverriddenNotAnnotatedInSubclass(Object o) {
        differentlyOverriddenNotAnnotatedInSubclassGoodEvents.add(o);
      }

      @Subscribe
      @Override
      public void differentlyOverriddenAnnotatedInSubclass(Object o) {
        differentlyOverriddenAnnotatedInSubclassGoodEvents.add(o);
      }
    }

    @Test
    public void testNotOverriddenInSubclass() {
      assertTrue(getSubscriber().notOverriddenInSubclassEvents.contains(EVENT));
    }

    @Test
    public void testOverriddenNotAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenNotAnnotatedInSubclassEvents.contains(EVENT));
    }

    @Test
    public void testDifferentlyOverriddenNotAnnotatedInSubclass() {
      assertTrue(getSubscriber().differentlyOverriddenNotAnnotatedInSubclassGoodEvents
          .contains(EVENT));
      assertTrue(getSubscriber().differentlyOverriddenNotAnnotatedInSubclassBadEvents.isEmpty());
    }

    @Test
    public void testOverriddenAndAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenAndAnnotatedInSubclassEvents.contains(EVENT));
    }

    @Test
    public void testDifferentlyOverriddenAndAnnotatedInSubclass() {
      assertTrue(getSubscriber().differentlyOverriddenAnnotatedInSubclassGoodEvents
          .contains(EVENT));
      assertTrue(getSubscriber().differentlyOverriddenAnnotatedInSubclassBadEvents.isEmpty());
    }

    @Override
    SubClass createSubscriber() {
      return new SubClass();
    }
  }

  public static class AbstractNotAnnotatedInSuperclassTest
      extends AbstractEventBusTestParent<AbstractNotAnnotatedInSuperclassTest.SubClass> {
    abstract static class SuperClass {
      public abstract void overriddenInSubclassNowhereAnnotated(Object o);

      public abstract void overriddenAndAnnotatedInSubclass(Object o);
    }

    static class SubClass extends SuperClass {
      final List<Object> overriddenInSubclassNowhereAnnotatedEvents = new ArrayList<>();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = new ArrayList<>();

      @Override
      public void overriddenInSubclassNowhereAnnotated(Object o) {
        overriddenInSubclassNowhereAnnotatedEvents.add(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }
    }

    @Test
    public void testOverriddenAndAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenAndAnnotatedInSubclassEvents.contains(EVENT));
    }

    @Test
    public void testOverriddenInSubclassNowhereAnnotated() {
      assertTrue(getSubscriber().overriddenInSubclassNowhereAnnotatedEvents.isEmpty());
    }

    @Override
    SubClass createSubscriber() {
      return new SubClass();
    }
  }

  public static class NeitherAbstractNorAnnotatedInSuperclassTest
      extends AbstractEventBusTestParent<NeitherAbstractNorAnnotatedInSuperclassTest.SubClass> {
    static class SuperClass {
      final List<Object> neitherOverriddenNorAnnotatedEvents = new ArrayList<>();
      final List<Object> overriddenInSubclassNowhereAnnotatedEvents = new ArrayList<>();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = new ArrayList<>();

      public void neitherOverriddenNorAnnotated(Object o) {
        neitherOverriddenNorAnnotatedEvents.add(o);
      }

      public void overriddenInSubclassNowhereAnnotated(Object o) {
        overriddenInSubclassNowhereAnnotatedEvents.add(o);
      }

      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }
    }

    static class SubClass extends SuperClass {
      @Override
      public void overriddenInSubclassNowhereAnnotated(Object o) {
        super.overriddenInSubclassNowhereAnnotated(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        super.overriddenAndAnnotatedInSubclass(o);
      }
    }

    @Test
    public void testNeitherOverriddenNorAnnotated() {
      assertTrue(getSubscriber().neitherOverriddenNorAnnotatedEvents.isEmpty());
    }

    @Test
    public void testOverriddenInSubclassNowhereAnnotated() {
      assertTrue(getSubscriber().overriddenInSubclassNowhereAnnotatedEvents.isEmpty());
    }

    @Test
    public void testOverriddenAndAnnotatedInSubclass() {
      assertTrue(getSubscriber().overriddenAndAnnotatedInSubclassEvents.contains(EVENT));
    }

    @Override
    SubClass createSubscriber() {
      return new SubClass();
    }
  }

  public static class DeepInterfaceTest
      extends AbstractEventBusTestParent<DeepInterfaceTest.SubscriberClass> {
    interface Interface1 {
      @Subscribe
      void annotatedIn1(Object o);

      @Subscribe
      void annotatedIn1And2(Object o);

      @Subscribe
      void annotatedIn1And2AndClass(Object o);

      void declaredIn1AnnotatedIn2(Object o);

      void declaredIn1AnnotatedInClass(Object o);

      void nowhereAnnotated(Object o);
    }

    interface Interface2 extends Interface1 {
      @Override
      @Subscribe
      void declaredIn1AnnotatedIn2(Object o);

      @Override
      @Subscribe
      void annotatedIn1And2(Object o);

      @Override
      @Subscribe
      void annotatedIn1And2AndClass(Object o);

      void declaredIn2AnnotatedInClass(Object o);

      @Subscribe
      void annotatedIn2(Object o);
    }

    static class SubscriberClass implements Interface2 {
      final List<Object> annotatedIn1Events = new ArrayList<>();
      final List<Object> annotatedIn1And2Events = new ArrayList<>();
      final List<Object> annotatedIn1And2AndClassEvents = new ArrayList<>();
      final List<Object> declaredIn1AnnotatedIn2Events = new ArrayList<>();
      final List<Object> declaredIn1AnnotatedInClassEvents = new ArrayList<>();
      final List<Object> declaredIn2AnnotatedInClassEvents = new ArrayList<>();
      final List<Object> annotatedIn2Events = new ArrayList<>();
      final List<Object> nowhereAnnotatedEvents = new ArrayList<>();

      @Override
      public void annotatedIn1(Object o) {
        annotatedIn1Events.add(o);
      }

      @Subscribe
      @Override
      public void declaredIn1AnnotatedInClass(Object o) {
        declaredIn1AnnotatedInClassEvents.add(o);
      }

      @Override
      public void declaredIn1AnnotatedIn2(Object o) {
        declaredIn1AnnotatedIn2Events.add(o);
      }

      @Override
      public void annotatedIn1And2(Object o) {
        annotatedIn1And2Events.add(o);
      }

      @Subscribe
      @Override
      public void annotatedIn1And2AndClass(Object o) {
        annotatedIn1And2AndClassEvents.add(o);
      }

      @Subscribe
      @Override
      public void declaredIn2AnnotatedInClass(Object o) {
        declaredIn2AnnotatedInClassEvents.add(o);
      }

      @Override
      public void annotatedIn2(Object o) {
        annotatedIn2Events.add(o);
      }

      @Override
      public void nowhereAnnotated(Object o) {
        nowhereAnnotatedEvents.add(o);
      }
    }

    @Test
    public void testAnnotatedIn1() {
      assertTrue(getSubscriber().annotatedIn1Events.contains(EVENT));
    }

    @Test
    public void testAnnotatedIn2() {
      assertTrue(getSubscriber().annotatedIn2Events.contains(EVENT));
    }

    @Test
    public void testAnnotatedIn1And2() {
      assertTrue(getSubscriber().annotatedIn1And2Events.contains(EVENT));
    }

    @Test
    public void testAnnotatedIn1And2AndClass() {
      assertTrue(getSubscriber().annotatedIn1And2AndClassEvents.contains(EVENT));
    }

    @Test
    public void testDeclaredIn1AnnotatedIn2() {
      assertTrue(getSubscriber().declaredIn1AnnotatedIn2Events.contains(EVENT));
    }

    @Test
    public void testDeclaredIn1AnnotatedInClass() {
      assertTrue(getSubscriber().declaredIn1AnnotatedInClassEvents.contains(EVENT));
    }

    @Test
    public void testDeclaredIn2AnnotatedInClass() {
      assertTrue(getSubscriber().declaredIn2AnnotatedInClassEvents.contains(EVENT));
    }

    @Test
    public void testNowhereAnnotated() {
      assertTrue(getSubscriber().nowhereAnnotatedEvents.isEmpty());
    }

    @Override
    SubscriberClass createSubscriber() {
      return new SubscriberClass();
    }
  }
}
