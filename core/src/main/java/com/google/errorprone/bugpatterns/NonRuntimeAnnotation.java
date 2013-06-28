/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.TypeSymbol;

import java.lang.annotation.Retention;

/**
 * @author scottjohnson@google.com (Scott Johnson)
 */
@BugPattern(name = "NonRuntimeAnnotation",
    summary = "Calling getAnnotation on an annotation that is not retained at runtime.",
    explanation = "Calling getAnnotation on an annotation that does not have its Retention set to "
        + "RetentionPolicy.RUNTIME will always return null.", 
    category = JDK, severity = ERROR, maturity = EXPERIMENTAL)
public class NonRuntimeAnnotation extends DescribingMatcher<MethodInvocationTree> {

  @SuppressWarnings("deprecation")
  @Override
  public boolean matches(MethodInvocationTree tree, VisitorState state) {
    if (!methodSelect(
        instanceMethod(Matchers.<ExpressionTree>isSubtypeOf("java.lang.Class"), "getAnnotation"))
        .matches(tree, state)) {
      return false;
    }
    MemberSelectTree memTree = (MemberSelectTree) tree.getArguments().get(0);
    TypeSymbol annotation = ASTHelpers.getSymbol(memTree.getExpression()).type.tsym;

    Retention retention = annotation.getAnnotation(Retention.class);
    return retention == null || !retention.value().equals(RUNTIME);
  }

  @Override
  public Description describe(MethodInvocationTree tree, VisitorState state) {
    return new Description(tree, getDiagnosticMessage(), new SuggestedFix().replace(tree, "null"));
  }

  /**
   * Scanner for {@link NonRuntimeAnnotation}.
   *
   * @author scottjohnson@google.com (Scott Johnson)
   */
  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<MethodInvocationTree> nonRuntimeAnnotation =
        new NonRuntimeAnnotation();

    @Override
    public Void visitMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      evaluateMatch(tree, state, nonRuntimeAnnotation);
      return super.visitMethodInvocation(tree, state);
    }
  }
}
