package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ModifiersTree;



/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "InjectMoreThanOneQualifier",
    summary = "Using more than one qualifier annotation on the same element is not allowed.",
    explanation = "An element can be qualified by at most one qualifier.", category = INJECT,
    severity = ERROR, maturity = EXPERIMENTAL)
public class InjectMoreThanOneQualifier extends DescribingMatcher<AnnotationTree> {


  private static final String GUICE_BINDING_ANNOTATION = "com.google.inject.BindingAnnotation";
  private static final String JAVAX_QUALIFER_ANNOTATION = "javax.inject.Qualifier";

  @SuppressWarnings("unchecked")
  Matcher<AnnotationTree> qualifierAnnotationMatcher = Matchers.<AnnotationTree>anyOf(
      hasAnnotation(GUICE_BINDING_ANNOTATION), hasAnnotation(JAVAX_QUALIFER_ANNOTATION));

  @Override
  public boolean matches(AnnotationTree annotationTree, VisitorState state) {
    int numberOfQualifiers = 0;
    if (qualifierAnnotationMatcher.matches(annotationTree, state)) {
      for (AnnotationTree t :
          ((ModifiersTree) state.getPath().getParentPath().getLeaf()).getAnnotations()) {
        if (qualifierAnnotationMatcher.matches(t, state)) {
          numberOfQualifiers++;
        }
      }
    }
    return (numberOfQualifiers > 1);
  }

  @Override
  public Description describe(AnnotationTree annotation, VisitorState state) {
    return new Description(
        annotation, getDiagnosticMessage(), new SuggestedFix().delete(annotation));
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<AnnotationTree> annotationMatcher = new InjectMoreThanOneQualifier();

    @Override
    public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      evaluateMatch(annotationTree, visitorState, annotationMatcher);
      return super.visitAnnotation(annotationTree, visitorState);
    }
  }
}
