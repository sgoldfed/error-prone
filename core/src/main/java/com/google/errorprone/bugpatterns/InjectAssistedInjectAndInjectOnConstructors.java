package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.INJECT;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import static com.google.errorprone.matchers.Matchers.constructor;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.MultiMatcher.MatchType.ANY;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "AssistedInjectAndInjectOnConstructors",
    summary = "@AssistedInject and @Inject should not be used on different constructors "
    		+ "in the same class.",
    explanation = "Mixing @Inject and @AssistedInject leads to confusing code and the "
    + "documentation specifies not to do it. See " 
    + "http://google-guice.googlecode.com/git/javadoc/com/google/inject/assistedinject/AssistedInject.html",
    category = INJECT, severity = WARNING, maturity = EXPERIMENTAL)
public class InjectAssistedInjectAndInjectOnConstructors extends BugChecker
    implements AnnotationTreeMatcher {

  private static final String GUICE_INJECT_ANNOTATION = "com.google.inject.Inject";
  private static final String JAVAX_INJECT_ANNOTATION = "javax.inject.Inject";
  private static final String ASSISTED_INJECT_ANNOTATION =
      "com.google.inject.assistedinject.AssistedInject";

  /**
   * Matches if any constructor of a class is annotated with an @Inject annotation.
   */
  @SuppressWarnings("unchecked")
  private MultiMatcher<ClassTree, MethodTree> constructorWithInjectMatcher = constructor(
      ANY, Matchers.<MethodTree>anyOf(
          hasAnnotation(GUICE_INJECT_ANNOTATION), hasAnnotation(JAVAX_INJECT_ANNOTATION)));

  /**
   * Matches if any constructor of a class is annotated with an @AssistedInject annotation.
   */
  @SuppressWarnings("unchecked")
  private MultiMatcher<ClassTree, MethodTree> constructorWithAssistedInjectMatcher =
      constructor(ANY, Matchers.<MethodTree>hasAnnotation(ASSISTED_INJECT_ANNOTATION));

  /**
   * Matches if a class has a constructor that is annotated with @Inject and a constructor annotated
   * with @AssistedInject.
   */
  @SuppressWarnings("unchecked")
  private Matcher<ClassTree> constructorsWithInjectAndAssistedInjectMatcher =
      Matchers.<ClassTree>allOf(constructorWithInjectMatcher, constructorWithAssistedInjectMatcher);

  @Override
  @SuppressWarnings("unchecked")
  public final Description matchAnnotation(AnnotationTree annotationTree, VisitorState state) {
    Tree modified = state.getPath().getParentPath().getParentPath().getLeaf();
    if (ASTHelpers.getSymbol(modified).isConstructor()) {
      Symbol annotationSymbol = ASTHelpers.getSymbol(annotationTree);
      if (annotationSymbol.equals(state.getSymbolFromString(JAVAX_INJECT_ANNOTATION))
          || annotationSymbol.equals(state.getSymbolFromString(GUICE_INJECT_ANNOTATION))
          || annotationSymbol.equals(state.getSymbolFromString(ASSISTED_INJECT_ANNOTATION))) {
        ClassTree enclosingClass =
            (ClassTree) state.getPath().getParentPath().getParentPath().getParentPath().getLeaf();
        if (constructorsWithInjectAndAssistedInjectMatcher.matches(enclosingClass, state)) {
          return describeMatch(annotationTree, new SuggestedFix().delete(annotationTree));
        }
      }
    }
    return Description.NO_MATCH;
  }
}
