/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import static com.google.errorprone.BugPattern.Category.DAGGER;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.AnnotationType;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.code.Attribute.Compound;

import javax.lang.model.element.TypeElement;

/**
 * Matches @dagger.Provides annotations used outside of Dagger Modules.
 *
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */

@BugPattern(name = "DaggerProvidesNotInModule",
    summary = "Dagger @Provides methods must be declared in Dagger Modules",
    explanation = "@dagger.Provides will only be regisered by Dagger if it is used on methods "
        + "inside of Dagger Modules. Modules are classes annotated with @dagger.Module",
        category = DAGGER, severity = ERROR, maturity = EXPERIMENTAL)
public class DaggerProvidesNotInModule extends BugChecker
    implements AnnotationTreeMatcher {

  private static final String DAGGER_PROVIDES_ANNOTATION = "dagger.Provides";
  private static final String DAGGER_MODULE_ANNOTATION = "dagger.Module";

  @SuppressWarnings("unchecked")
  private static final Matcher<AnnotationTree> PROVIDES_ANNOTATION_MATCHER =
      new AnnotationType(DAGGER_PROVIDES_ANNOTATION);

  @Override
  public Description matchAnnotation(AnnotationTree annotationTree, VisitorState state) {
    if (!PROVIDES_ANNOTATION_MATCHER.matches(annotationTree, state)) {
      return Description.NO_MATCH;
    }
    boolean isModule = false;
    for (Compound c : ASTHelpers.getSymbol(getEnclosingClass(state)).getAnnotationMirrors()) {
      if (((TypeElement) c.getAnnotationType().asElement()).getQualifiedName()
          .contentEquals(DAGGER_MODULE_ANNOTATION)) {
        isModule = true;
      }
    }
    return isModule ? Description.NO_MATCH
        : describeMatch(annotationTree, new SuggestedFix().delete(annotationTree));
  }

  private static final ClassTree getEnclosingClass(VisitorState state) {
    return (ClassTree) state.getPath().getParentPath().getParentPath().getParentPath().getLeaf();
  }
}
