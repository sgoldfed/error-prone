package com.google.errorprone.bugpatterns;

import com.google.inject.Singleton;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */
public class GuiceMoreThanOneScopeAnnotationOnClassNegativeCases {

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
}