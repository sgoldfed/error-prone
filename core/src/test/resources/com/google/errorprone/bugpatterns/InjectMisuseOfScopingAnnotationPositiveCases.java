package com.google.errorprone.bugpatterns;

import com.google.inject.Provides;
import com.google.inject.ScopeAnnotation;

import javax.inject.Scope;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectMisuseOfScopingAnnotationPositiveCases {

  /**
   * Class has a javax scoping annotation on a field.
   */
  public class TestClass1 {
    //BUG: Suggestion includes "remove"
    @MyJavaxScopingAnnotation
    private String s;

    public TestClass1(int n) {}
  }

  /**
   * Class has a Guice scoping annotation on a field.
   */
  public class TestClass2 {
    //BUG: Suggestion includes "remove"
    @MyGuiceScopingAnnotation
    private String s;

    public TestClass2(int n) {}
  }

  /**
   * Class has a javax scoping annotation on a non @Provides method.
   */
  public class TestClass3 {
    public TestClass3(int n) {}

    //BUG: Suggestion includes "remove"
    @MyJavaxScopingAnnotation
    String foo() {
      return "";
    }
  }
  
  /**
   * Class has a Guice scoping annotation on a non @Provides method.
   */
  public class TestClass4 {
    public TestClass4(int n) {}

    //BUG: Suggestion includes "remove"
    @MyGuiceScopingAnnotation
    String foo() {
      return "";
    }
  }

  /**
   * Class has a javax scoping annotation on a parameter. The method is a @Provides
   * method, but this is not relevant and nevertheless an error.
   */
  public class TestClass5 {
    public TestClass5(int n) {}

    @Provides
    String provideString(
      //BUG: Suggestion includes "remove"
        @MyJavaxScopingAnnotation
        String s) {
      return s;
    }
  }
  
  /**
   * Class has a Guice scoping annotation on a parameter. The method is a @Provides
   * method, but this is not relevant and nevertheless an error.
   */
  public class TestClass6 {
    public TestClass6(int n) {}

    @Provides
    String provideString(
      //BUG: Suggestion includes "remove"
        @MyGuiceScopingAnnotation
        String s) {
      return s;
    }
  }
  
  @Scope
  public @interface MyJavaxScopingAnnotation {}
  
  @ScopeAnnotation
  public @interface MyGuiceScopingAnnotation {

  }

}
