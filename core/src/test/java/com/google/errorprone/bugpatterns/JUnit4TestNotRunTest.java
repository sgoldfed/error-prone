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

import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.ErrorProneScanner;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class JUnit4TestNotRunTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = new CompilationTestHelper(JUnit4TestNotRun.class);
  }

  @Test
  public void testPositiveCase1() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        new File(this.getClass().getResource("JUnit4TestNotRunPositiveCase1.java").toURI()));
  }

  @Test
  public void testPositiveCase2() throws Exception {
    compilationHelper.assertCompileFailsWithMessages(
        new File(this.getClass().getResource("JUnit4TestNotRunPositiveCase2.java").toURI()));
  }

  /**
   * Test custom test runner class.
   */
  @Test
  public void testPositiveCase3() throws Exception {
    compilationHelper = new CompilationTestHelper(
        new JUnit4TestNotRun("org.junit.runners.Parameterized"));
    compilationHelper.assertCompileFailsWithMessages(
        new File(this.getClass().getResource("JUnit4TestNotRunPositiveCase3.java").toURI()));
  }

  @Test
  public void testNegativeCase1() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("JUnit4TestNotRunNegativeCase1.java").toURI()));
  }

  @Test
  public void testNegativeCase2() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("JUnit4TestNotRunNegativeCase2.java").toURI()));
  }

  @Test
  public void testNegativeCase3() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("JUnit4TestNotRunNegativeCase3.java").toURI()));
  }

  @Test
  public void testNegativeCase4() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("JUnit4TestNotRunNegativeCase4.java").toURI()));
  }

}