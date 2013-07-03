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

import com.google.inject.assistedinject.AssistedInject;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
public class InjectAssistedInjectAndInjectOnConstructorsNegativeCases {
  /**
   * Class has a single constructor with no annotation.
   */
  public class TestClass1 {
    TestClass1() {}
  }

  /**
   * Class has a constructor with a @javax.inject.Inject annotation.
   */
  public class TestClass2 {
    @javax.inject.Inject
    public TestClass2() {}
  }
  
  /**
   * Class has a constructor with a @com.google.injectInject annotation.
   */
  public class TestClass3 {
    @com.google.inject.Inject
    public TestClass3() {}
  }
  
  /**
   * Class has a constructor annotated with @AssistedInject
   */
  public class TestClass4 {
    @AssistedInject
    public TestClass4() {}
  }
  
  /**
   * Class has a constructor with a @AssistedInject annotation as well as an injectable field
   */
  public class TestClass5 {
    @javax.inject.Inject
    private int n;

    @AssistedInject
    public TestClass5() {}
  }
}
