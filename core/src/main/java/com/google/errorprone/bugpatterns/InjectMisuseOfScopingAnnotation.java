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
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "InjectMisuseOfScopingAnnotation",
    summary = "Scoping annotations are only allowed on @Provides methods or class", explanation =
        "Scoping annotations are only allowed on @Provides methods or classes. Perhaps you "
        + " meant to use a  @Qualifier annotation. ", category = INJECT, severity = ERROR,
    maturity = EXPERIMENTAL)
public class InjectMisuseOfScopingAnnotation extends DescribingMatcher<AnnotationTree> {

  private static final String GUICE_SCOPE_ANNOTATION = "com.google.inject.ScopeAnnotation";
  private static final String JAVAX_SCOPE_ANNOTATION = "javax.inject.Scope";
  private static final String PROVIDES_ANNOTATION = "com.google.inject.Provides";

  /**
   * Matches annotations that are themselves annotated with with @ScopeAnnotation(Guice) or
   * @Scope(javax).
   */
  @SuppressWarnings("unchecked")
  private Matcher<AnnotationTree> scopeAnnotationMatcher = Matchers.<AnnotationTree>anyOf(
      hasAnnotation(GUICE_SCOPE_ANNOTATION), hasAnnotation(JAVAX_SCOPE_ANNOTATION));

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(AnnotationTree annotationTree, VisitorState state) {
    if (scopeAnnotationMatcher.matches(annotationTree, state)) {
      Tree modified = state.getPath().getParentPath().getParentPath().getLeaf();
      if (modified instanceof ClassTree) {
        // scoping annotation on a class is allowed
        return false;
      }
      if (modified instanceof MethodTree) {
        boolean isProvidesMethod = false;
        for (AnnotationTree t : ((MethodTree) modified).getModifiers().getAnnotations()) {
          if (ASTHelpers.getSymbol(t).equals(state.getSymbolFromString(PROVIDES_ANNOTATION))) {
            isProvidesMethod = true;
          }
        }
        return !isProvidesMethod;
      }
      // scoping annotation anywhere else is an error
      return true;
    }
    // not a scoping annotation
    return false;
  }

  @Override
  public Description describe(AnnotationTree annotationTree, VisitorState state) {
    return new Description(
        annotationTree, getDiagnosticMessage(), new SuggestedFix().delete(annotationTree));
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<AnnotationTree> annotationMatcher =
        new InjectMisuseOfScopingAnnotation();

    @Override
    public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      evaluateMatch(annotationTree, visitorState, annotationMatcher);
      return super.visitAnnotation(annotationTree, visitorState);
    }
  }
}
