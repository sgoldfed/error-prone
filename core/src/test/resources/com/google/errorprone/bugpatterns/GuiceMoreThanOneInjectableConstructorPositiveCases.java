package com.google.errorprone.bugpatterns;

import com.google.inject.Inject;
import java.beans.ConstructorProperties;

/**
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */

public class GuiceMoreThanOneInjectableConstructorPositiveCases {

  public class TestClass1 {
    @Inject
    public TestClass1() {
    }
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass1(int n) {
    }
  }

  public class TestClass2 {
    @Inject 
    public TestClass2() {
    }
    //BUG: Suggestion includes "remove"
    @Inject 
    public TestClass2(int m, int n) {
    }
    public TestClass2(int n,String s) {
    }
  }

  //testing that the error appears on the @Inject annotation even in the presence 
  //of other annotations
  public class TestClass3 {
    private int m,n;
    @Inject
    public TestClass3(){
      m = 0;
      n = 0;
    }
    //BUG: Suggestion includes "remove"
    @Inject 
    @ConstructorProperties({"m","n"})
    public TestClass3(int m, int n) {
      this.m = m;
      this.n = n;
    }
    public int getM() {
      return m;
    }
    public int getN() {
      return n;
    }

  }

  //testing that the error appears on the @Inject annotation even in the presence 
  //of other annotations
  public class TestClass4 {
    private int m,n;
    @Inject
    public TestClass4(){
      m = 0;
      n = 0;
    }
    @ConstructorProperties({"m","n"})
    //BUG: Suggestion includes "remove"
    @Inject
    public TestClass4(int m, int n) {
      this.m = m;
      this.n = n;
    }
    public int getM() {
      return m;
    }
    public int getN() {
      return n;
    }
  }
}