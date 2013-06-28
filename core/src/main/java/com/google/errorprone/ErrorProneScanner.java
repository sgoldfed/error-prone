/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.errorprone;

import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;

import com.google.errorprone.bugpatterns.ArrayEquals;
import com.google.errorprone.bugpatterns.ArrayToString;
import com.google.errorprone.bugpatterns.BadShiftAmount;
import com.google.errorprone.bugpatterns.CollectionIncompatibleType;
import com.google.errorprone.bugpatterns.ComparisonOutOfRange;
import com.google.errorprone.bugpatterns.CovariantEquals;
import com.google.errorprone.bugpatterns.DeadException;
import com.google.errorprone.bugpatterns.EmptyIfStatement;
import com.google.errorprone.bugpatterns.EmptyStatement;
import com.google.errorprone.bugpatterns.FallThroughSuppression;
import com.google.errorprone.bugpatterns.LongLiteralLowerCaseSuffix;
import com.google.errorprone.bugpatterns.OrderingFrom;
import com.google.errorprone.bugpatterns.PreconditionsCheckNotNull;
import com.google.errorprone.bugpatterns.PreconditionsCheckNotNullPrimitive;
import com.google.errorprone.bugpatterns.PreconditionsExpensiveString;
import com.google.errorprone.bugpatterns.ReturnValueIgnored;
import com.google.errorprone.bugpatterns.SelfAssignment;
import com.google.errorprone.bugpatterns.SelfEquals;
import com.google.errorprone.bugpatterns.SuppressWarningsDeprecated;
import com.google.errorprone.bugpatterns.UnneededConditionalOperator;
import com.google.errorprone.matchers.DescribingMatcher;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans the parsed AST, looking for violations of any of the enabled checks.
 * @author Alex Eagle (alexeagle@google.com)
 */
public class ErrorProneScanner extends Scanner {

  /**
   * Selects which checks should be enabled when the compile is run.
   */
  public interface EnabledPredicate {
    boolean isEnabled(Class<? extends DescribingMatcher<?>> check, BugPattern annotation);

    /**
     * Selects all checks which are annotated with maturity = ON_BY_DEFAULT.
     */
    public static final EnabledPredicate DEFAULT_CHECKS = new EnabledPredicate() {
      @Override
      public boolean isEnabled(Class<? extends DescribingMatcher<?>> check, BugPattern annotation) {
        return annotation.maturity() == MATURE;
      }
    };
  }

  private final Iterable<DescribingMatcher<MethodInvocationTree>> methodInvocationMatchers;
  private final Iterable<DescribingMatcher<NewClassTree>> newClassMatchers;
  private final Iterable<DescribingMatcher<AnnotationTree>> annotationMatchers;
  private final Iterable<DescribingMatcher<EmptyStatementTree>> emptyStatementMatchers;
  private final Iterable<DescribingMatcher<Tree>> assignmentMatchers;
  private final Iterable<DescribingMatcher<Tree>> variableMatchers;
  private final Iterable<DescribingMatcher<MethodTree>> methodMatchers;
  private final Iterable<DescribingMatcher<LiteralTree>> literalMatchers;
  private final Iterable<DescribingMatcher<ConditionalExpressionTree>>
      conditionalExpressionMatchers;
  private final Iterable<DescribingMatcher<BinaryTree>> binaryExpressionMatchers;

  @SuppressWarnings("unchecked")
  public ErrorProneScanner(EnabledPredicate enabled) {
    try {
      this.methodInvocationMatchers = createChecks(enabled,
          SelfEquals.class,
          OrderingFrom.class,
          PreconditionsCheckNotNull.class,
          PreconditionsExpensiveString.class,
          PreconditionsCheckNotNullPrimitive.class,
          CollectionIncompatibleType.class,
          ArrayEquals.class,
          ArrayToString.class,
          ReturnValueIgnored.class);
      this.newClassMatchers = createChecks(enabled, DeadException.class);
      this.annotationMatchers = createChecks(enabled,
          FallThroughSuppression.class,
          SuppressWarningsDeprecated.class);
      this.emptyStatementMatchers = createChecks(enabled,
          EmptyIfStatement.class,
          EmptyStatement.class);
      this.binaryExpressionMatchers = createChecks(enabled,
          BadShiftAmount.class,
          ComparisonOutOfRange.class);
      this.assignmentMatchers = createChecks(enabled, SelfAssignment.class);
      this.variableMatchers = createChecks(enabled, SelfAssignment.class);
      this.methodMatchers = createChecks(enabled, CovariantEquals.class);
      this.literalMatchers = createChecks(enabled, LongLiteralLowerCaseSuffix.class);
      this.conditionalExpressionMatchers = createChecks(enabled, UnneededConditionalOperator.class);
    } catch (Exception e) {
      throw new RuntimeException("Could not reflectively create error prone matchers", e);
    }
  }

  private static <T extends Tree> Iterable<DescribingMatcher<T>> createChecks(
      EnabledPredicate predicate, Class<? extends DescribingMatcher<T>>... matchers)
      throws IllegalAccessException, InstantiationException {
    List<DescribingMatcher<T>> result = new ArrayList<DescribingMatcher<T>>();
    for (Class<? extends DescribingMatcher<T>> matcher : matchers) {
      if (predicate.isEnabled(matcher, matcher.getAnnotation(BugPattern.class))) {
        result.add(matcher.newInstance());
      }
    }
    return result;
  }

  @Override
  public Void visitMethodInvocation(
      MethodInvocationTree methodInvocationTree, VisitorState state) {
    for (DescribingMatcher<MethodInvocationTree> matcher : methodInvocationMatchers) {
      evaluateMatch(methodInvocationTree, state, matcher);
    }
    return super.visitMethodInvocation(methodInvocationTree, state);
  }

  @Override
  public Void visitBinary(BinaryTree binaryExpressionTree,  VisitorState state) {
    for (DescribingMatcher<BinaryTree> matcher : binaryExpressionMatchers) {
      evaluateMatch(binaryExpressionTree, state, matcher);
    }
    return super.visitBinary(binaryExpressionTree, state);
  }

  @Override
  public Void visitNewClass(NewClassTree newClassTree, VisitorState visitorState) {
    for (DescribingMatcher<NewClassTree> matcher : newClassMatchers) {
      evaluateMatch(newClassTree, visitorState, matcher);
    }
    return super.visitNewClass(newClassTree, visitorState);
  }

  @Override
  public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
    for (DescribingMatcher<AnnotationTree> matcher : annotationMatchers) {
      evaluateMatch(annotationTree, visitorState, matcher);
    }
    return super.visitAnnotation(annotationTree, visitorState);
  }

  @Override
  public Void visitEmptyStatement(
      EmptyStatementTree emptyStatementTree, VisitorState visitorState) {
    for (DescribingMatcher<EmptyStatementTree> matcher : emptyStatementMatchers) {
      evaluateMatch(emptyStatementTree, visitorState, matcher);
    }
    return super.visitEmptyStatement(emptyStatementTree, visitorState);
  }

  @Override
  public Void visitAssignment(AssignmentTree assignmentTree, VisitorState visitorState) {
    for (DescribingMatcher<Tree> matcher : assignmentMatchers) {
      evaluateMatch(assignmentTree, visitorState, matcher);
    }
    return super.visitAssignment(assignmentTree, visitorState);
  }
  
  @Override
  public Void visitVariable(VariableTree variableTree, VisitorState visitorState) {
    for (DescribingMatcher<Tree> matcher : variableMatchers) {
      evaluateMatch(variableTree, visitorState, matcher);
    }
    return super.visitVariable(variableTree, visitorState);
  }

  @Override
  public Void visitMethod(MethodTree node, VisitorState visitorState) {
    for (DescribingMatcher<MethodTree> matcher : methodMatchers) {
      evaluateMatch(node, visitorState, matcher);
    }
    return super.visitMethod(node, visitorState);
  }

  @Override
  public Void visitLiteral(LiteralTree literalTree, VisitorState visitorState) {
    for (DescribingMatcher<LiteralTree> matcher : literalMatchers) {
      evaluateMatch(literalTree, visitorState, matcher);
    }
    return super.visitLiteral(literalTree, visitorState);
  }

  @Override
  public Void visitConditionalExpression(
      ConditionalExpressionTree conditionalExpressionTree, VisitorState visitorState) {
    for (DescribingMatcher<ConditionalExpressionTree> matcher : conditionalExpressionMatchers) {
      evaluateMatch(conditionalExpressionTree, visitorState, matcher);
    }
    return super.visitConditionalExpression(conditionalExpressionTree, visitorState);
  }
}
