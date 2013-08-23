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

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.sun.source.tree.Tree.Kind.METHOD;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "ScopingAnnotationNotOnMethodOrClass",
    summary = "Scoping annotations are only allowed on methods or classes", explanation =
        "Scoping annotations are only allowed on @Provides methods or classes. Perhaps you "
        + "meant to use a  @Qualifier annotation. ", category = INJECT, severity = ERROR,
    maturity = EXPERIMENTAL)
public class InjectScopingAnnotationNotOnMethodOrClass extends BugChecker implements AnnotationTreeMatcher {

  private static final String GUICE_SCOPE_ANNOTATION = "com.google.inject.ScopeAnnotation";
  private static final String JAVAX_SCOPE_ANNOTATION = "javax.inject.Scope";
  private static final String GUICE_BINDING_ANNOTATION = "com.google.inject.BindingAnnotation";
  private static final String JAVAX_QUALIFER_ANNOTATION = "javax.inject.Qualifier";

  /**
   * Matches annotations that are themselves annotated with with @ScopeAnnotation(Guice) or
   * @Scope(javax).
   */
  @SuppressWarnings("unchecked")
  private static final Matcher<AnnotationTree> SCOPE_ANNOTATION_MATCHER =
      Matchers.<AnnotationTree>anyOf(
          hasAnnotation(GUICE_SCOPE_ANNOTATION), hasAnnotation(JAVAX_SCOPE_ANNOTATION));

  @SuppressWarnings("unchecked")
  private static final Matcher<AnnotationTree> QUALIFIER_ANNOTATION_MATCHER =
      Matchers.<AnnotationTree>anyOf(
          hasAnnotation(GUICE_BINDING_ANNOTATION), hasAnnotation(JAVAX_QUALIFER_ANNOTATION));

  @Override
  @SuppressWarnings("unchecked")
  public final Description matchAnnotation(AnnotationTree annotationTree, VisitorState state) {
    if (SCOPE_ANNOTATION_MATCHER.matches(annotationTree, state)
        && !QUALIFIER_ANNOTATION_MATCHER.matches(annotationTree, state)) { // discussed with sameb@
      Tree modified = state.getPath().getParentPath().getParentPath().getLeaf();
      if (!modified.getKind().equals(METHOD) && !(modified instanceof ClassTree)) {
          //code written without usng Tree.Kind since in java6 interface is a Kind.CLASS and in 
          //java 7, it's a Kind.INTERFACE and we want it to be compatible with both
          //interface will be a separate error. 
        return describeMatch(annotationTree, new SuggestedFix().delete(annotationTree));
      }
    }
    return Description.NO_MATCH;
  }
}
