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

import static com.google.errorprone.BugPattern.Category.GUICE;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.sun.source.tree.Tree.Kind.METHOD;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.google.inject.assistedinject.Assisted;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.model.JavacElements;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "GuiceAssistedParameters", summary = "", explanation = "", category = GUICE,
    severity = ERROR, maturity = EXPERIMENTAL)
public class GuiceAssistedParameters extends DescribingMatcher<VariableTree> {

  private static final String ASSISTED_ANNOTATION = "com.google.inject.assistedinject.Assisted";

  private Matcher<VariableTree> constructorParameterMatcher = new Matcher<VariableTree>() {
    @Override
    public boolean matches(VariableTree t, VisitorState state) {
      return ASTHelpers.getSymbol(state.getPath().getParentPath().getLeaf()).isConstructor();
    }
  };

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(VariableTree variableTree, VisitorState state) {
    if (constructorParameterMatcher.matches(variableTree, state)) {
      Assisted thisParametersAssistedAnnotation =
          JavacElements.getAnnotation(ASTHelpers.getSymbol(variableTree), Assisted.class);
      if (thisParametersAssistedAnnotation != null) {
        MethodTree enclosingMethod = (MethodTree) state.getPath().getParentPath().getLeaf();
        // count the number of parameters of this type and value. One is expected since we
        // will be iterating through all parameters including the one we're matching. 
        int numIdentical = 0;
        for (VariableTree parameter : enclosingMethod.getParameters()) {
          if (Matchers.<VariableTree>isSameType(variableTree).matches(parameter, state)) {
            Assisted otherParametersAssistedAnnotation =
                JavacElements.getAnnotation(ASTHelpers.getSymbol(parameter), Assisted.class);
            if (otherParametersAssistedAnnotation
                != null && thisParametersAssistedAnnotation.value()
                .equals(otherParametersAssistedAnnotation.value())) {
              numIdentical++;
            }
          }
        }
        if (numIdentical > 1) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Description describe(VariableTree variableTree, VisitorState state) {
    // find the @Assisted annotation to put the error on 
    AnnotationTree assistedAnnotation = null;
    for (AnnotationTree annotation : variableTree.getModifiers().getAnnotations()) {
      if (ASTHelpers.getSymbol(annotation).equals(state.getSymbolFromString(ASSISTED_ANNOTATION))) {
        assistedAnnotation = annotation;
      }
    }
    if (assistedAnnotation == null) {
      throw new IllegalStateException("Expected to find @Assisted on this parameter");
    }
    return new Description(
        assistedAnnotation, getDiagnosticMessage(), new SuggestedFix().delete(assistedAnnotation));
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<VariableTree> variableMatcher = new GuiceAssistedParameters();

    @Override
    public Void visitVariable(VariableTree variableTree, VisitorState visitorState) {
      evaluateMatch(variableTree, visitorState, variableMatcher);
      return super.visitVariable(variableTree, visitorState);
    }
  }
}
