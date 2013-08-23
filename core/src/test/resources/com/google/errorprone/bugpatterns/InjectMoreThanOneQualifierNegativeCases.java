/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.bugpatterns;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;

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
  @Foo1
  public class TestClass2 {
    @Foo1
    private int n;

    @Foo1
    public TestClass2() {}

    @Foo1
    public void setN(@Foo1 int n) {}
  }

  /**
   * A class in which a single com.google.inject.BindingAnnotation annotation is on the class, on a
   * constructor, on a field, on a method, and on a method parameter.
   */
  @Bar1
  public class TestClass3 {
    @Bar1
    private int n;

    @Bar1
    public TestClass3() {}

    @Bar1
    public void setN(@Bar1 int n) {}
  }
  
  /**
   * A suppression test for a class in which the class, a constructor, a field, 
   * a method, and a method parameter each have  two com.google.inject.BindingAnnotation
   *  annotations
   * 
   * TODO(sgoldfeder) Change so that the error only shows up on elements where qualifiers are 
   * allowed--i.e. methods and classes.
   */
  @SuppressWarnings("MoreThanOneQualifier")
  @Foo1
  @Foo2
  public class TestClass4 {
    @Foo1
    @Foo2
    private int n;

    @Foo1
    @Foo2
    public TestClass4() {}

    @Foo1
    @Foo2
    public void setN(@Foo1 @Foo2 int n) {}
  }


  @Qualifier
  @Retention(RUNTIME)
  public @interface Foo1 {}
  
  @Qualifier
  @Retention(RUNTIME)
  public @interface Foo2 {}
  
  @BindingAnnotation
  @Retention(RUNTIME)
  public @interface Bar1 {}
  
  @BindingAnnotation
  @Retention(RUNTIME)
  public @interface Bar2 {}
}
