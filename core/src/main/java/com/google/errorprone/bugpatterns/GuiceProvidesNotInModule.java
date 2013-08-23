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

import static com.google.errorprone.BugPattern.Category.GUICE;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.AnnotationType;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.IsCastableTo;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;

/**
 * Matches @dagger.Provides annotations used outside of Dagger Modules.
 *
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */

@BugPattern(name = "GuiceProvidesNotInModule",
    summary = "Dagger @Provides methods must be declared in Dagger Modules",
    explanation = "@com.google.inject.Provides will only be regisered by Guice/Gin if it is "
        + "used on methods inside of Guice/Gin Modules. ",
        category = GUICE, severity = ERROR, maturity = EXPERIMENTAL)
public class GuiceProvidesNotInModule extends BugChecker
    implements AnnotationTreeMatcher {

  private static final String GUICE_PROVIDES_ANNOTATION = "com.google.inject.Provides";
  private static final String GUICE_MODULE = "com.google.inject.Module";

  @SuppressWarnings("unchecked")
  private static final Matcher<AnnotationTree> PROVIDES_ANNOTATION_MATCHER =
      Matchers.isType(GUICE_PROVIDES_ANNOTATION);
  
  @SuppressWarnings("unchecked")
  private static final Matcher<ClassTree> GUICE_MODULE_MATCHER =
      Matchers.isCastableTo(GUICE_MODULE);

  @Override
  public Description matchAnnotation(AnnotationTree annotationTree, VisitorState state) {
    if (!PROVIDES_ANNOTATION_MATCHER.matches(annotationTree, state)) {
      return Description.NO_MATCH;
    }
    boolean isModule = GUICE_MODULE_MATCHER.matches(getEnclosingClass(state), state);
    System.out.println(getEnclosingClass(state));
    System.out.println(isModule);
    return isModule ? Description.NO_MATCH
        : describeMatch(annotationTree, new SuggestedFix().delete(annotationTree));
  }

  private static final ClassTree getEnclosingClass(VisitorState state) {
    return (ClassTree) state.getPath().getParentPath().getParentPath().getParentPath().getLeaf();
  }
}
