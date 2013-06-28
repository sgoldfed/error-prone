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

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.parentNode;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

/**
 * An abstract base class to match method invocations in which the return value is not used.
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
abstract class AbstractReturnValueIgnored extends DescribingMatcher<MethodInvocationTree> {

  @SuppressWarnings("unchecked")
  @Override
  public boolean matches(MethodInvocationTree methodInvocationTree, VisitorState state) {
    return allOf(
        parentNode(Matchers.<MethodInvocationTree>kindIs(Kind.EXPRESSION_STATEMENT)),
        specializedMatcher())
    .matches(methodInvocationTree, state);
  }

  /**
   * Match whatever additional conditions concrete subclasses want to match (a list of known
   * side-effect-free methods, has a @CheckReturnValue annotation, etc.).
   */
  public abstract Matcher<MethodInvocationTree> specializedMatcher();

  /**
   * Fixes the error by assigning the result of the call to the receiver reference, or deleting
   * the method call.
   */
  @Override
  public Description describe(MethodInvocationTree methodInvocationTree, VisitorState state) {
    // Find the root of the field access chain, i.e. a.intern().trim() ==> a.
    ExpressionTree identifierExpr = ASTHelpers.getRootAssignable(methodInvocationTree);
    String identifierStr = null;
    Type identifierType = null;
    if (identifierExpr != null) {
      identifierStr = identifierExpr.toString();
      if (identifierExpr instanceof JCIdent) {
        identifierType = ((JCIdent) identifierExpr).sym.type;
      } else if (identifierExpr instanceof JCFieldAccess) {
        identifierType = ((JCFieldAccess) identifierExpr).sym.type;
      } else {
        throw new IllegalStateException("Expected a JCIdent or a JCFieldAccess");
      }
    }

    Type returnType = ASTHelpers.getReturnType(
        ((JCMethodInvocation) methodInvocationTree).getMethodSelect());

    SuggestedFix fix;
    if (identifierStr != null && !"this".equals(identifierStr) && returnType != null &&
        state.getTypes().isAssignable(returnType, identifierType)) {
      // Fix by assigning the assigning the result of the call to the root receiver reference.
      fix = new SuggestedFix().prefixWith(methodInvocationTree, identifierStr + " = ");
    } else {
      // Unclear what the programmer intended.  Delete since we don't know what else to do.
      Tree parent = state.getPath().getParentPath().getLeaf();
      fix = new SuggestedFix().delete(parent);
    }
    return new Description(methodInvocationTree, getDiagnosticMessage(), fix);
  }
}
