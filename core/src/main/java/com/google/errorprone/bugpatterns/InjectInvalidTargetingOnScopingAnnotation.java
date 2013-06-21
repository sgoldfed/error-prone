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
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "InjectInvalidTargetingOnScopingAnnotation",
    summary = "The target of a scoping annotation must be set to METHOD and/or TYPE.",
    explanation = "", category = INJECT, severity = ERROR, maturity = EXPERIMENTAL)
public class InjectInvalidTargetingOnScopingAnnotation extends DescribingMatcher<ClassTree> {

  private static final String GUICE_SCOPE_ANNOTATION = "com.google.inject.ScopeAnnotation";
  private static final String JAVAX_SCOPE_ANNOTATION = "javax.inject.Scope";
  private static final String TARGET_ANNOTATION = "java.lang.annotation.Target";

  /**
   * Matches classes that are annotated with @Scope or @ScopeAnnotation.
   */
  @SuppressWarnings("unchecked")
  private Matcher<ClassTree> scopeAnnotationMatcher = Matchers.<ClassTree>anyOf(
      hasAnnotation(GUICE_SCOPE_ANNOTATION), hasAnnotation(JAVAX_SCOPE_ANNOTATION));

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(ClassTree classTree, VisitorState state) {
    if ((ASTHelpers.getSymbol(classTree).flags() & Flags.ANNOTATION) != 0
        && scopeAnnotationMatcher.matches(classTree, state)) {
      Target target = JavacElements.getAnnotation(ASTHelpers.getSymbol(classTree), Target.class);
      boolean hasTypeOrMethodTargeting = false;
      if (target != null) {
        for (ElementType elementType : target.value()) {
          if (elementType != METHOD && elementType != TYPE) {
            return true;
          } else if (elementType == METHOD || elementType == TYPE) {
            hasTypeOrMethodTargeting = true;
          }
        }
        // this will return true if the target of a scoping annotation is specified as @Target({})
        return !hasTypeOrMethodTargeting;
      }
      // no target specified
      return true;
    }
    return false;
  }

  @Override
  public Description describe(ClassTree classTree, VisitorState state) {
    Target target = JavacElements.getAnnotation(ASTHelpers.getSymbol(classTree), Target.class);
    if (target == null) {
      return new Description(classTree, getDiagnosticMessage(), new SuggestedFix().addImport(
          "java.lang.annotation.Target").addStaticImport("java.lang.annotation.ElementType.TYPE")
          .addStaticImport("java.lang.annotation.ElementType.METHOD")
          .prefixWith(classTree, "@Target({TYPE, METHOD})\n"));
    }
    AnnotationTree targetNode = null;
    for (AnnotationTree annotation : classTree.getModifiers().getAnnotations()) {
      if (ASTHelpers.getSymbol(annotation).equals(state.getSymbolFromString(TARGET_ANNOTATION))) {
        targetNode = annotation;
      }
    }
    return new Description(targetNode, getDiagnosticMessage(), new SuggestedFix().addImport(
        "java.lang.annotation.Target").addStaticImport("java.lang.annotation.ElementType.TYPE")
        .addStaticImport("java.lang.annotation.ElementType.METHOD")
        .replace(targetNode, "@Target({TYPE, METHOD})"));
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<ClassTree> classMatcher =
        new InjectInvalidTargetingOnScopingAnnotation();

    @Override
    public Void visitClass(ClassTree classTree, VisitorState visitorState) {
      evaluateMatch(classTree, visitorState, classMatcher);
      return super.visitClass(classTree, visitorState);
    }
  }
}
