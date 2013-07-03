/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.constructor;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.MultiMatcher.MatchType.ANY;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "InjectAssistedInjectAndInjectOnConstructors",
    summary = "@AssistedInject and @Inject cannot be used on constructors in the same class.",
    explanation = "http://google-guice.googlecode.com/git/javadoc/com/google/inject/assistedinject/AssistedInject.html",
    category = INJECT, severity = ERROR, maturity = EXPERIMENTAL)
public class InjectAssistedInjectAndInjectOnConstructors extends DescribingMatcher<AnnotationTree> {

  private static final String GUICE_INJECT_ANNOTATION = "com.google.inject.Inject";
  private static final String JAVAX_INJECT_ANNOTATION = "javax.inject.Inject";
  private static final String ASSISTED_INJECT_ANNOTATION =
      "com.google.inject.assistedinject.AssistedInject";

  /**
   * Matches if any constructor of a class is annotated with an @Inject annotation.
   */
  @SuppressWarnings("unchecked")
  private MultiMatcher<ClassTree, MethodTree> INJECTABLE_CONSTRUCTOR_MATCHER = constructor(
      ANY, Matchers.<MethodTree>anyOf(
          hasAnnotation(GUICE_INJECT_ANNOTATION), hasAnnotation(JAVAX_INJECT_ANNOTATION)));

  /**
   * Matches if any constructor of a class is annotated with an @AssistedInject annotation.
   */
  @SuppressWarnings("unchecked")
  private static MultiMatcher<ClassTree, MethodTree> CONSTRUCTOR_WITH_ASSISTED_INJECT_MATCHER =
      constructor(ANY, Matchers.<MethodTree>hasAnnotation(ASSISTED_INJECT_ANNOTATION));

  /**
   * Matches if a class has a constructor that is annotated with @Inject and a constructor annotated
   * with @AssistedInject.
   */
  @SuppressWarnings("unchecked")
  private Matcher<ClassTree> CONSTRUCTOR_WITH_INJECT_AND_ASSISTED_INJECT_MATCHER =
      Matchers.<ClassTree>allOf(
          INJECTABLE_CONSTRUCTOR_MATCHER, CONSTRUCTOR_WITH_ASSISTED_INJECT_MATCHER);

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(AnnotationTree annotationTree, VisitorState state) {
    Tree modified = state.getPath().getParentPath().getParentPath().getLeaf();
    if (ASTHelpers.getSymbol(modified).isConstructor()) {
      Symbol annotationSymbol = ASTHelpers.getSymbol(annotationTree);
      if (annotationSymbol.equals(state.getSymbolFromString(JAVAX_INJECT_ANNOTATION))
          || annotationSymbol.equals(state.getSymbolFromString(GUICE_INJECT_ANNOTATION))
          || annotationSymbol.equals(state.getSymbolFromString(ASSISTED_INJECT_ANNOTATION))) {
        ClassTree enclosingClass =
            (ClassTree) state.getPath().getParentPath().getParentPath().getParentPath().getLeaf();
        return CONSTRUCTOR_WITH_INJECT_AND_ASSISTED_INJECT_MATCHER.matches(enclosingClass, state);
      }
    }
    return false;
  }

  @Override
  public Description describe(AnnotationTree annotationTree, VisitorState state) {
    return new Description(
        annotationTree, getDiagnosticMessage(), new SuggestedFix().delete(annotationTree));
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<AnnotationTree> annotationMatcher =
        new InjectAssistedInjectAndInjectOnConstructors();

    @Override
    public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      evaluateMatch(annotationTree, visitorState, annotationMatcher);
      return super.visitAnnotation(annotationTree, visitorState);
    }
  }
}
