package com.google.errorprone.bugpatterns;

import com.google.inject.Inject;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */

public class MoreThanOneInjectableConstructorNegativeCases {

  /**
   * Class has single non-injectable constructor.
   */
  public class TestClass1 {
    public TestClass1() {}
  }

  /**
   * Class has single injectable constructor.
   */
  public class TestClass2 {
    @Inject
    public TestClass2() {}
  }

  /**
   * Class has two constructors, only one is injectable.
   */
  public class TestClass3 {
    @Inject
    public TestClass3() {}

    public TestClass3(int n) {}
  }

  /**
   * Class has two constructors, only one is injectable. Class also has an injectable field.
   */
  public class TestClass4 {
    @Inject
    String x;

    @Inject
    public TestClass4() {}

    public TestClass4(int n) {}
  }
}
