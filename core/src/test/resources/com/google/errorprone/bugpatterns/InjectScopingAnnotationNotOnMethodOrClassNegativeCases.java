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

import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.inject.Qualifier;
import javax.inject.Scope;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectScopingAnnotationNotOnMethodOrClassNegativeCases {

  /**
   * Class has no scoping annotation.
   */
  public class TestClass1 {}

  /**
   * has a scoping annotation on the class
   */
  @Singleton
  public class TestClass2 {}

  /**
   * Class has scoping annotation on a method
   */
  public class TestClass3 {

    @Singleton
    String method() {
      return "";
    }
  }
  
  /**
   * field has scoping annotation, but it's also a qualifier
   */
  public class TestClass4 {
    @ScopeAndQualifier
    int field;
  }
  
  @Scope
  @Qualifier
  @interface ScopeAndQualifier {}
  
}
