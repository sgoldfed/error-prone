package com.google.errorprone.bugpatterns;

import com.google.inject.Singleton;
import com.google.inject.servlet.SessionScoped;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */
public class GuiceMoreThanOneScopeAnnotationOnClassPositiveCases {

  /**
   * Class has two scope annotations
   */
  
  //BUG: Suggestion includes "remove" 
  @Singleton 
  //BUG: Suggestion includes "remove"  
  @SessionScoped
  public class TestClass1 {}

  /**
   * Class has three annotations, two of which are scope annotations.
   */
  //BUG: Suggestion includes "remove" 
  @Singleton 
  @SuppressWarnings("foo")
  //BUG: Suggestion includes "remove"  
  @SessionScoped
  public class TestClass2 {}
  
}
