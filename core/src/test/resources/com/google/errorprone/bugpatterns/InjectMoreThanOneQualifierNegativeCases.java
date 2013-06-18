package com.google.errorprone.bugpatterns;

import com.google.inject.BindingAnnotation;
import javax.inject.Qualifier;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectMoreThanOneQualifierNegativeCases {
  /**
   * A class in with no annotations on any of its members.
   */
  public class TestClass1 {
    private int n;

    public TestClass1() {}

    public void setN(int n) {}
  }

  /**
   * A class in which a single javax.inject.Qualifier annotation is on the class, on a constructor,
   * on a field, on a method, and on a method parameter.
   */
  @Foo
  public class TestClass2 {
    @Foo
    private int n;

    @Foo
    public TestClass2() {}

    @Foo
    public void setN(@Foo int n) {}
  }

  /**
   * A class in which a single com.google.inject.BindingAnnotation annotation is on the class, on a
   * constructor, on a field, on a method, and on a method parameter.
   */
  @Bar
  public class TestClass3 {
    @Bar
    private int n;

    @Bar
    public TestClass3() {}

    @Bar
    public void setN(@Bar int n) {}
  }

  @Qualifier
  public @interface Foo {
  }
  @BindingAnnotation
  public @interface Bar {
  }

}
