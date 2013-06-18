package com.google.errorprone.bugpatterns;

import com.google.inject.BindingAnnotation;
import javax.inject.Qualifier;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectMoreThanOneQualifierPositiveCases {


  /**
   * A class in which the class, a constructor, a field, a method, and a method parameter each have
   * two com.google.inject.BindingAnnotation annotations.
   */
  //BUG: Suggestion includes "remove"
  @Foo1 
  //BUG: Suggestion includes "remove"
  @Foo2
  public class TestClass1 {
    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Foo2
    private int n;

    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Foo2
    public TestClass1() {}

    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Foo2
    public void setN(
        //BUG: Suggestion includes "remove"
        @Foo1 
        //BUG: Suggestion includes "remove"
        @Foo2 
        int n) {}
  }

  /**
   * A class in which the class, a constructor, a field, a method, and a method parameter each have
   * two javax.inject.Qualifier annotations.
   */

  //BUG: Suggestion includes "remove"
  @Bar1 
  //BUG: Suggestion includes "remove"
  @Bar2
  public class TestClass2 {
    //BUG: Suggestion includes "remove"
    @Bar1 
    //BUG: Suggestion includes "remove"
    @Bar2
    private int n;

    //BUG: Suggestion includes "remove"
    @Bar1
    //BUG: Suggestion includes "remove"
    @Bar2
    public TestClass2() {}

    //BUG: Suggestion includes "remove"
    @Bar1 
    //BUG: Suggestion includes "remove"
    @Bar2
    public void setN(
        //BUG: Suggestion includes "remove"
        @Bar1 
        //BUG: Suggestion includes "remove"
         @Bar2
         int n) {}
  }

  /**
   * A class in which the class, a constructor, a field, a method, and a method parameter each have
   * one javax.inject.Qualifier annotation and one com.google.inject.BindingAnnotation annotation.
   */

  //BUG: Suggestion includes "remove"
  @Foo1 
  //BUG: Suggestion includes "remove"
  @Bar1
  public class TestClass3 {
    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Bar1
    private int n;

    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Bar1
    public TestClass3() {}

    //BUG: Suggestion includes "remove"
    @Foo1 
    //BUG: Suggestion includes "remove"
    @Bar1
    public void setN(
        //BUG: Suggestion includes "remove"
        @Foo1 
        //BUG: Suggestion includes "remove"
        @Bar1 
        int n) {}
  }


  @Qualifier
  public @interface Foo1 {
  }
  @Qualifier
  public @interface Foo2 {
  }
  @BindingAnnotation
  public @interface Bar1 {
  }
  @BindingAnnotation
  public @interface Bar2 {
  }

}
