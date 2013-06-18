package com.google.errorprone.bugpatterns;

import com.google.inject.Singleton;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectScopeAnnotationOnInterfaceOrAbstractClassNegativeCases {
  /**
   * concrete class has no scoping annotation
   */
  public class TestClass1 {
  }

  /**
   * abstract class has no scoping annotation
   */
  public abstract class TestClass2 {
  }
  /**
   * interface has no scoping annotation
   */
  public interface TestClass3 {
  }
  /**
   * concrete class has scoping annotation
   */
  @Singleton
  public class TestClass4 {
  }
}
