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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;

/**
 * This checker matches if a class has more than one annotation that is a scope annotation(that is,
 * the annotation is either annotated with Guice's @ScopeAnnotation or Javax's @Scope).
 *
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */

@BugPattern(name = "InjectMoreThanOneScopeAnnotationOnClass",
    summary = "A class can be annotated with at most one scope annotation", 
    explanation = "Annotating a class with more than one scope annotation is "
        + "invalid according to the JSR-330 specification. ", category = INJECT, severity = ERROR,
    maturity = EXPERIMENTAL)
public class InjectMoreThanOneScopeAnnotationOnClass extends DescribingMatcher<AnnotationTree> {

  private static final String GUICE_SCOPE_ANNOTATION = "com.google.inject.ScopeAnnotation";
  private static final String JAVAX_SCOPE_ANNOTATION = "javax.inject.Scope";

  /**
   * Matches annotations that are themselves annotated with with @ScopeAnnotation(Guice) or
   * @Scope(Javax).
   */
  @SuppressWarnings("unchecked")
  private Matcher<AnnotationTree> scopeAnnotationMatcher = Matchers.<AnnotationTree>anyOf(
      hasAnnotation(GUICE_SCOPE_ANNOTATION), hasAnnotation(JAVAX_SCOPE_ANNOTATION));


  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(AnnotationTree annotationTree, VisitorState state) {
    int numberOfScopeAnnotations = 0;
    // check if this annotation is on a class and is a scope annotation
    if (scopeAnnotationMatcher.matches(annotationTree, state)
        && state.getPath().getParentPath().getParentPath().getLeaf() instanceof ClassTree) {
      for (AnnotationTree annotation :
          ((ModifiersTree) state.getPath().getParentPath().getLeaf()).getAnnotations()) {
        if (scopeAnnotationMatcher.matches(annotation, state)) {
          numberOfScopeAnnotations++;
        }
      }
    }
    return (numberOfScopeAnnotations > 1);
  }

  @Override
  public Description describe(AnnotationTree annotationTree, VisitorState state) {
    return new Description(
        annotationTree, getDiagnosticMessage(), new SuggestedFix().delete(annotationTree));
  }


  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<AnnotationTree> annotationMatcher =
        new InjectMoreThanOneScopeAnnotationOnClass();

    @Override
    public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      evaluateMatch(annotationTree, visitorState, annotationMatcher);
      return super.visitAnnotation(annotationTree, visitorState);
    }
  }
}
