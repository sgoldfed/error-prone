package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.GUICE;
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

import java.util.List;

/**
 * Matches if a class has to constructors annotated with @Inject
 * 
 * @author sgoldfeder@google.com(Steven Goldfeder)
 */
@BugPattern(name = "GuiceMoreThanOneInjectableConstructor",
summary = "A class may not have more than one injectable constructor.",
explanation ="",
category = GUICE, severity = ERROR, maturity = EXPERIMENTAL)
public class GuiceMoreThanOneInjectableConstructor extends DescribingMatcher<ClassTree>  {
  
  private static final String GUICE_INJECT_ANNOTATION = "com.google.inject.Inject";
  private static final String JAVAX_INJECT_ANNOTATION = "javax.inject.Inject";
  
  
    @SuppressWarnings("unchecked")
  Matcher<MethodTree> injectOnMethodMatcher = Matchers.<MethodTree>anyOf(hasAnnotation(GUICE_INJECT_ANNOTATION),
      hasAnnotation(JAVAX_INJECT_ANNOTATION));


  @Override
  public boolean matches(ClassTree classTree, VisitorState state) {
    int numberOfInjectableConstructors = 0;
    for (Tree member : classTree.getMembers()) {
      if (member instanceof MethodTree && ASTHelpers.getSymbol(member).isConstructor() 
          && injectOnMethodMatcher.matches((MethodTree) member, state)){
        numberOfInjectableConstructors++;
      }
    }
    if(numberOfInjectableConstructors > 1){
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Description describe(ClassTree classTree, VisitorState state) {
    int injectedConstructor = 0;
    for (Tree member : classTree.getMembers()) {
      if (member instanceof MethodTree && ASTHelpers.getSymbol(member).isConstructor()
          && injectOnMethodMatcher.matches((MethodTree) member, state)){
        injectedConstructor++; 
        if(injectedConstructor > 1){
        //puts error on second injectable constructor
          MethodTree constructor = (MethodTree)member;
          List<? extends AnnotationTree> annotationsOnConstructor =constructor.getModifiers().getAnnotations();
          for(AnnotationTree annotation: annotationsOnConstructor){
            if(ASTHelpers.getSymbol(annotation).equals(state.getSymbolFromString(GUICE_INJECT_ANNOTATION)) 
                || ASTHelpers.getSymbol(annotation).equals(state.getSymbolFromString(JAVAX_INJECT_ANNOTATION)) ){
              return new Description(annotation,getDiagnosticMessage(),new SuggestedFix().delete(annotation));
            }
          }
        }          
      }
    }
    throw new IllegalStateException("Expected to find more than once constructor annotated with @Inject");
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public DescribingMatcher<ClassTree> classMatcher = new GuiceMoreThanOneInjectableConstructor();

    @Override
    public Void visitClass(ClassTree classTree, VisitorState visitorState) {
      evaluateMatch(classTree, visitorState, classMatcher);
      return super.visitClass(classTree, visitorState);
    }
  }
}