package com.google.errorprone.bugpatterns;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectMisuseOfScopingAnnotationNegativeCases {

  /**
   * Class has no scoping annotation.
   */
  public class TestClass1 {
    public TestClass1(int n) {}
  }

  /**
   * has a scoping annotation on the class
   */
  @Singleton
  public class TestClass2 {
    public TestClass2(int n) {}
  }

  /**
   * Class has scoping annotation on a @Provides method
   */
  public class TestClass3 {
    public TestClass3(int n) {}

    @Provides
    @Singleton
    String provideString() {
      return "";
    }
  }
}
