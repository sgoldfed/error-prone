package com.google.errorprone.bugpatterns;

import com.google.inject.Singleton;
import com.google.inject.Provides;
/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */
public class InjectMoreThanOneScopeAnnotationOnClassNegativeCases {

  /**
   * Class has no annotation. 
   */
  public class TestClass1 {}
  /**
   * Class has a single non scoping annotation. 
   */
  @SuppressWarnings("foo")
  public class TestClass2 {}
  
  /**
   * Class hasa single scoping annotation.
   */
  @Singleton 
  public class TestClass3 {}
  
  /**
   * Class has two annotations, one of which is a scoping annotation.
   */
  @Singleton @SuppressWarnings("foo")
  public class TestClass4 {}
  
  /**
   * Class has two annotations, one of which is a scoping annotation. Class
   * also has a method with a scoping annotation.
   */
   @SuppressWarnings("foo")
  public class TestClass5 {
  @Singleton @Provides
  public void foo(){}
  }
}