package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.DescribingMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;



/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "InjectScopeAnnotationOnInterfaceOrAbstractClass",
    summary = "Scope annotation on an interface or abstact class is not allowed",
    explanation = "Scoping annotations are not allowed on abstract types.", category = INJECT,
    severity = ERROR, maturity = MATURE)
public class InjectScopeAnnotationOnInterfaceOrAbstractClass
    extends DescribingMatcher<AnnotationTree> {

  private static final String GUICE_SCOPE_ANNOTATION = "com.google.inject.ScopeAnnotation";
  private static final String JAVAX_SCOPE_ANNOTATION = "javax.inject.Scope";

  /**
   * Matches annotations that are themselves annotated with with @ScopeAnnotation(Guice) or
   * @Scope(Javax).
   */
  @SuppressWarnings("unchecked")
  private Matcher<AnnotationTree> scopeAnnotationMatcher = Matchers.<AnnotationTree>anyOf(
      hasAnnotation(GUICE_SCOPE_ANNOTATION), hasAnnotation(JAVAX_SCOPE_ANNOTATION));


  private Matcher<ClassTree> interfaceAndAbstractClassMatcher = new Matcher<ClassTree>() {
    @Override
    public boolean matches(ClassTree classTree, VisitorState state) {
      return classTree.getModifiers().getFlags().contains(ABSTRACT)
          || classTree.getKind().equals(INTERFACE);
    }
  };

  @Override
  @SuppressWarnings("unchecked")
  public final boolean matches(AnnotationTree annotationTree, VisitorState state) {
    Tree modified = state.getPath().getParentPath().getParentPath().getLeaf();
    return (scopeAnnotationMatcher.matches(annotationTree, state) && modified instanceof ClassTree
        && interfaceAndAbstractClassMatcher.matches((ClassTree) modified, state));
  }

  @Override
  public Description describe(AnnotationTree annotationTree, VisitorState state) {
    return new Description(
        annotationTree, getDiagnosticMessage(), new SuggestedFix().delete(annotationTree));
  }


  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<AnnotationTree> annotationMatcher =
        new InjectScopeAnnotationOnInterfaceOrAbstractClass();

    @Override
    public Void visitAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      evaluateMatch(annotationTree, visitorState, annotationMatcher);
      return super.visitAnnotation(annotationTree, visitorState);
    }
  }
}
