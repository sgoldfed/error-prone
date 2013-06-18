package com.google.errorprone.bugpatterns;

import com.google.inject.Singleton;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */

public class InjectScopeAnnotationOnInterfaceOrAbstractClassPositiveCases {

  /**
   * abstract class has scoping annotation
   */
  //BUG: Suggestion includes "remove"
  @Singleton
  public abstract class TestClass1 {
  }

  /**
   * interface has scoping annotation
   */
  //BUG: Suggestion includes "remove"
  @Singleton
  public interface TestClass2 {
  }
}
