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

import com.google.inject.ScopeAnnotation;

import javax.inject.Scope;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectScopingAnnotationNotOnMethodOrClassPositiveCases {

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
   * Class has a javax scoping annotation on a parameter.
   */
  public class TestClass5 {

    String method(
        //BUG: Suggestion includes "remove"
        @MyJavaxScopingAnnotation
        String s) {
      return s;
    }
  }
  
  /**
   * Class has a Guice scoping annotation on a parameter.
   */
  public class TestClass6 {

    String method(
        //BUG: Suggestion includes "remove"
        @MyGuiceScopingAnnotation
        String s) {
      return s;
    }
  }
  
  @Scope
  public @interface MyJavaxScopingAnnotation {}
  
  @ScopeAnnotation
  public @interface MyGuiceScopingAnnotation {}
}
