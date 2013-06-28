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

package com.google.errorprone.matchers;

import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.MultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.MultiMatcher.MatchType.ANY;
import static org.junit.Assert.assertTrue;

import com.google.errorprone.Scanner;
import com.google.errorprone.VisitorState;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 *
 * TODO(eaftan): Add test for correct matching of nodes.
 */
public class AnnotationTest extends CompilerBasedTest {

  final List<ScannerTest> tests = new ArrayList<ScannerTest>();

  @Before
  public void setUp() throws IOException {
    tests.clear();
    writeFile("SampleAnnotation1.java",
        "package com.google;",
        "public @interface SampleAnnotation1 {}");
    writeFile("SampleAnnotation2.java",
        "package com.google;",
        "public @interface SampleAnnotation2 {}");
  }

  @After
  public void tearDown() {
    for (ScannerTest test : tests) {
      test.assertDone();
    }
  }

  @Test
  public void shouldNotMatchNoAnnotations() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {}");
    assertCompiles(nodeWithAnnotationMatches(false, new Annotation<Tree>(ANY,
        isType("com.google.SampleAnnotation1"))));
    assertCompiles(nodeWithAnnotationMatches(false, new Annotation<Tree>(ALL,
        isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchSingleAnnotationOnClass() throws IOException {
    writeFile("A.java",
      "package com.google;",
      "@SampleAnnotation1",
      "public class A {}");
    assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
        isType("com.google.SampleAnnotation1"))));
    assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
        isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldNotMatchNonmatchingSingleAnnotationOnClass() throws IOException {
    writeFile("A.java",
      "package com.google;",
      "@SampleAnnotation1",
      "public class A {}");
    assertCompiles(nodeWithAnnotationMatches(false, new Annotation<Tree>(ANY,
        isType("com.google.WrongAnnotation"))));
    assertCompiles(nodeWithAnnotationMatches(false, new Annotation<Tree>(ALL,
        isType("com.google.WrongAnnotation"))));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldMatchAllAnnotationsOnClass() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "@SampleAnnotation1 @SampleAnnotation2",
        "public class A {}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          Matchers.<AnnotationTree>anyOf(isType("com.google.SampleAnnotation1"),
              isType("com.google.SampleAnnotation2")))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          Matchers.<AnnotationTree>anyOf(isType("com.google.SampleAnnotation1"),
              isType("com.google.SampleAnnotation2")))));
  }

  @Test
  public void matchOneAnnotationsOnClass() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "@SampleAnnotation1 @SampleAnnotation2",
        "public class A {}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(false, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnInterface() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "@SampleAnnotation1",
        "public interface A {}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnEnum() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "@SampleAnnotation1",
        "public enum A {}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnField() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  @SampleAnnotation1",
        "  public int i;",
        "}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnMethod() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  @SampleAnnotation1",
        "  public void foo() {}",
        "}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnParameter() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  public void foo(@SampleAnnotation1 int i) {}",
        "}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnConstructor() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  @SampleAnnotation1",
        "  public A() {}",
        "}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnLocalVariable() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  public void foo() {",
        "    @SampleAnnotation1",
        "    int i = 0;",
        "  }",
        "}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnAnnotation() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "@SampleAnnotation1",
        "public @interface A {}");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }

  @Test
  public void shouldMatchAnnotationOnPackage() throws IOException {
    writeFile("package-info.java",
        "@SampleAnnotation1",
        "package com.google;");
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ANY,
          isType("com.google.SampleAnnotation1"))));
      assertCompiles(nodeWithAnnotationMatches(true, new Annotation<Tree>(ALL,
          isType("com.google.SampleAnnotation1"))));
  }


  private abstract class ScannerTest extends Scanner {
    public abstract void assertDone();
  }

  private Scanner nodeWithAnnotationMatches(final boolean shouldMatch,
      final Annotation<Tree> toMatch) {
    ScannerTest test = new ScannerTest() {
      private boolean matched = false;

      @Override
      public Void visitAnnotation(AnnotationTree node, VisitorState visitorState) {
        TreePath currPath = getCurrentPath().getParentPath();
        Tree parent = currPath.getLeaf();
        if (parent.getKind() == Kind.MODIFIERS) {
          currPath = currPath.getParentPath();
          parent = currPath.getLeaf();
        }
        if (toMatch.matches(parent, visitorState)) {
          matched = true;
        }
        return super.visitAnnotation(node, visitorState);
      }

      @Override
      public void assertDone() {
        assertTrue(shouldMatch == matched);
      }
    };
    tests.add(test);
    return test;
  }

}
