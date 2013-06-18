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
 * Matches if a class has to constructors annotated with @Inject
 *
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */
@BugPattern(name = "InjectMoreThanOneInjectableConstructor",
    summary = "A class may not have more than one injectable constructor.", explanation =
        "Having more than one injectable constructor will throw a runtime error in"
        + " compliant JSR-330 frameworks such as Guice or Dagger", category = INJECT,
    severity = ERROR, maturity = EXPERIMENTAL)
public class InjectMoreThanOneInjectableConstructor extends DescribingMatcher<MethodTree> {

  private static final String GUICE_INJECT_ANNOTATION = "com.google.inject.Inject";
  private static final String JAVAX_INJECT_ANNOTATION = "javax.inject.Inject";


  @SuppressWarnings("unchecked")
  Matcher<MethodTree> injectOnMethodMatcher = Matchers.<MethodTree>anyOf(
      hasAnnotation(GUICE_INJECT_ANNOTATION), hasAnnotation(JAVAX_INJECT_ANNOTATION));

  @Override
  public boolean matches(MethodTree methodTree, VisitorState state) {
    int numberOfInjectableConstructors = 0;
    if (ASTHelpers.getSymbol(methodTree).isConstructor()
        && injectOnMethodMatcher.matches(methodTree, state)) {
      for (Tree member : ((ClassTree) state.getPath().getParentPath().getLeaf()).getMembers()) {
        if (member instanceof MethodTree && ASTHelpers.getSymbol(member).isConstructor()
            && injectOnMethodMatcher.matches((MethodTree) member, state)) {
          numberOfInjectableConstructors++;
        }
      }
    }
    return (numberOfInjectableConstructors > 1);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Description describe(MethodTree methodTree, VisitorState state) {
    for (AnnotationTree annotation : methodTree.getModifiers().getAnnotations()) {
      if (ASTHelpers.getSymbol(annotation)
          .equals(state.getSymbolFromString(GUICE_INJECT_ANNOTATION)) || ASTHelpers.getSymbol(
          annotation).equals(state.getSymbolFromString(JAVAX_INJECT_ANNOTATION))) {
        return new Description(
            annotation, getDiagnosticMessage(), new SuggestedFix().delete(annotation));
      }
    }
    throw new IllegalStateException(
        "Expected to find more than once constructor annotated with @Inject");
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<MethodTree> methodMatcher =
        new InjectMoreThanOneInjectableConstructor();

    @Override
    public Void visitMethod(MethodTree methodTree, VisitorState visitorState) {
      evaluateMatch(methodTree, visitorState, methodMatcher);
      return super.visitMethod(methodTree, visitorState);
    }
  }
}
