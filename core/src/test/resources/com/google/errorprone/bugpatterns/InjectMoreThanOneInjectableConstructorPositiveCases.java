package com.google.errorprone.bugpatterns;

import com.google.inject.Inject;
import java.beans.ConstructorProperties;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */

public class InjectMoreThanOneInjectableConstructorPositiveCases {

  /**
   * class has 2 constructors, both are injectable
   */
  public class TestClass1 {
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass1() {}

    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass1(int n) {}
  }

  /**
   * class has 3 constructors, 2 are injectable
   */
  public class TestClass2 {
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass2() {}

    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass2(int n) {}

    public TestClass2(String s) {}
  }

  /**
   * testing that the error appears on the @Inject annotation even in the presence of other
   * annotations
   */
  public class TestClass3 {
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass3() {}

    //BUG: Suggestion includes "remove"
    @Inject
    @ConstructorProperties({"m", "n"})
    public TestClass3(int m, int n) {}
  }



  /**
   * testing that the error appears on the @Inject annotation even in the presence of other
   * annotations
   */
  public class TestClass4 {

    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass4() {}

    @ConstructorProperties({"m", "n"}) 
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass4(int m, int n) {}
  }
}
