/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.errorprone;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Describes a bug pattern detected by error-prone.  Used to generate compiler error messages,
 * for @SuppressWarnings, and to generate wiki documentation.
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
@Retention(RUNTIME)
public @interface BugPattern {
  /**
   * A unique identifier for this bug, used for @SuppressWarnings and in the compiler error
   * message.
   */
  String name();

  /**
   * Alternate identifiers for this bug, which may also be used in @SuppressWarnings.
   */
  String[] altNames() default {};

  /**
   * The type of link to generate in the compiler error message.
   */
  LinkType linkType() default LinkType.WIKI;

  /**
   * The link URL to use if linkType() is LinkType.CUSTOM.
   */
  String link() default "";

  public enum LinkType {
    /**
     * Link to wiki, autogenerated using the name identifier.
     */
    WIKI,
    /**
     * Custom string.
     */
    CUSTOM,
    /**
     * No link should be displayed.
     */
    NONE
  }

  /**
   * The class of bug this bug checker detects.
   */
  Category category();

  public enum Category {
    /**
     * General Java or JDK errors.
     */
    JDK,
    /**
     * Errors specific to Google Guava.
     */
    GUAVA,
    /**
     * Errors specific to Google Guice.
     */
    GUICE,
    /**
     * Errors specific to JUnit.
     */
    JUNIT,
    /**
     * One-off matchers that are not general errors.
     */
    ONE_OFF,
    /**
     *  JSR-330 errors not specific to Guice.
     */
    INJECT
  }

  /**
   * A short summary of the problem that this checker detects.  Used for the compiler error
   * message and for the wiki.  Must not contain format specifiers.
   *
   * <p>Wiki syntax is not allowed for this element.
   */
  String summary();

  /**
   * A short summary of the problem that this checker detects, used for the compiler error
   * message only.  May contain format specifiers and use format string interpolation when
   * generating the compiler error message.
   *
   * <p>Wiki syntax is not allowed for this element.
   */
  String formatSummary() default "";

  /**
   * A longer explanation of the problem that this checker detects.  Used as the main content
   * in the wiki page for this checker.
   *
   * <p>Wiki syntax is allowed for this element.
   */
  String explanation();

  SeverityLevel severity();

  public enum SeverityLevel {
    ERROR,
    WARNING,
    /**
     * Should not be used for general code.
     */
    NOT_A_PROBLEM
  }

  MaturityLevel maturity();

  public enum MaturityLevel {
    MATURE,
    EXPERIMENTAL
  }

  public class Instance {
    public String name;
    public String summary;
    public String altNames;
    public MaturityLevel maturity;
    public SeverityLevel severity;
  }
}
