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

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.bugpatterns.BugChecker.BinaryTreeMatcher;
import static com.google.errorprone.matchers.Matchers.anyOf;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCLiteral;

import java.util.Arrays;
import java.util.List;

/**
 * @author bill.pugh@gmail.com (Bill Pugh)
 * @author eaftan@google.com (Eddie Aftandilian)
 *
 * TODO(eaftan): Support other types of comparisons?  Are there likely to be errors in those?
 */
@BugPattern(name = "ComparisonOutOfRange",
    summary = "Comparison to value that is out of range for the compared type",
    formatSummary = "%ss may have a value in the range %d to %d; therefore, this comparison to " +
    	"%s will always evaluate to %s",
    explanation = "This checker looks for equality comparisons to values that are out of " +
        "range for the compared type.  For example, bytes may have a value in the range " +
        Byte.MIN_VALUE + " to " + Byte.MAX_VALUE + ". Comparing a byte for equality with a value " +
        "outside that range will always evaluate to false and usually indicates an error in the " +
        "code.\n\n" +
        "This checker currently supports checking for bad byte and character comparisons.",
    category = JDK, severity = ERROR, maturity = MATURE)
public class ComparisonOutOfRange extends BugChecker implements BinaryTreeMatcher {

  /**
   * Matches comparisons that are out of range for the given type.  Parameterized based on the
   * type of comparison (byte or char).
   */
  private static class BadComparisonMatcher implements Matcher<BinaryTree> {
    /**
     * The type of bad comparison matcher to create. Must be either Byte.TYPE or Character.TYPE.
     */
    private Class<?> type;

    private boolean initialized = false;
    private Type comparisonType;
    private int maxValue;
    private int minValue;

    public BadComparisonMatcher(Class<?> type) {
      if (type != Byte.TYPE && type != Character.TYPE) {
        throw new IllegalArgumentException("type must be either byte or char, but was "
            + type.getName());
      }
      this.type = type;
    }

    /**
     * Initialize matcher for the specific type.  We can't do this in the constructor because
     * we need the symbol table, which isn't available at that time.
     *
     * @param symbolTable The compiler's symbol table
     */
    private void init(Symtab symbolTable) {
      if (initialized) {
        throw new IllegalStateException("Do not try to initialize twice!");
      }

      // Specialize matcher based on type.
      if (type == Byte.TYPE) {
        comparisonType = symbolTable.byteType;
        maxValue = Byte.MAX_VALUE;
        minValue = Byte.MIN_VALUE;
      } else {
        comparisonType = symbolTable.charType;
        maxValue = Character.MAX_VALUE;
        minValue = Character.MIN_VALUE;
      }
      initialized = true;
    }

    @Override
    public boolean matches(BinaryTree tree, VisitorState state) {
      if (!initialized) {
        init(state.getSymtab());
      }

      // Must be an == or != comparison.
      if (tree.getKind() != Kind.EQUAL_TO && tree.getKind() != Kind.NOT_EQUAL_TO) {
        return false;
      }

      // Match trees that have one literal operand and another of the specified type.
      @SuppressWarnings("unchecked")
      List<ExpressionTree> binaryTreeMatches = ASTHelpers.matchBinaryTree(tree,
          Arrays.asList(Matchers.<ExpressionTree>isInstance(JCLiteral.class),
              Matchers.<ExpressionTree>isSameType(comparisonType)),
          state);
      if (binaryTreeMatches == null) {
        return false;
      }
      JCLiteral literal = (JCLiteral) binaryTreeMatches.get(0);

      // Check whether literal is out of range for the specified type.  Logic is based on
      // JLS 5.6.2 - Binary Numeric Promotion:
      // If either is double, other is converted to double.
      // If either is float, other is converted to float.
      // If either is long, other is converted to long.
      // Otherwise, both are converted to int.
      Object literalValue = literal.getValue();
      switch (literal.getKind()) {
        case DOUBLE_LITERAL:
          double doubleValue = ((Double) literalValue).doubleValue();
          return doubleValue < minValue || doubleValue > maxValue;
        case FLOAT_LITERAL:
          float floatValue = ((Float) literalValue).floatValue();
          return floatValue < minValue || floatValue > maxValue;
        case LONG_LITERAL:
          long longValue = ((Long) literalValue).longValue();
          return longValue < minValue || longValue > maxValue;
        default:
          // JCLiteral.getValue() can return Integer, Character, or Boolean.
          int intValue;
          if (literalValue instanceof Integer) {
            intValue = ((Integer) literalValue).intValue();
          } else if (literalValue instanceof Character) {
            intValue = ((Character) literalValue).charValue();
          } else if (literalValue instanceof Boolean) {
            throw new IllegalStateException("Cannot compare " + comparisonType
                + " to boolean literal");
          } else {
            throw new IllegalStateException("Unexpected literal type: " + literal);
          }
          return intValue < minValue || intValue > maxValue;
      }
    }
  }

  private static final Matcher<BinaryTree> BYTE_MATCHER = new BadComparisonMatcher(Byte.TYPE);
  private static final Matcher<BinaryTree> CHAR_MATCHER = new BadComparisonMatcher(Character.TYPE);

  @SuppressWarnings("unchecked")
  @Override
  public Description matchBinary(BinaryTree tree, VisitorState state) {
    if(anyOf(BYTE_MATCHER, CHAR_MATCHER).matches(tree, state)) {
      return describe(tree, state);
    }
    return Description.NO_MATCH;
  }

  /**
   * Suggested fixes are as follows.  For the byte case, convert the literal to its byte
   * representation. For example, "255" becomes "-1.  For the character case, replace the
   * comparison with "true"/"false" since it's not clear what was intended and that is
   * semantically equivalent.
   *
   * TODO(eaftan): Suggested fixes don't handle side-effecting expressions, such as
   * (d = reader.read()) == -1.  Maybe add special case handling for assignments.
   */
  public Description describe(BinaryTree tree, VisitorState state) {
    @SuppressWarnings("unchecked")
    List<ExpressionTree> binaryTreeMatches = ASTHelpers.matchBinaryTree(tree,
        Arrays.asList(Matchers.<ExpressionTree>isInstance(JCLiteral.class),
            Matchers.<ExpressionTree>anything()),
        state);
    if (binaryTreeMatches == null) {
      throw new IllegalStateException("Expected one of the operands to be a literal");
    }
    JCLiteral literal = (JCLiteral) binaryTreeMatches.get(0);
    JCTree nonLiteralOperand = (JCTree) binaryTreeMatches.get(1);
    boolean byteMatch = state.getTypes().isSameType(nonLiteralOperand.type,
        state.getSymtab().byteType);

    boolean willEvaluateTo = (tree.getKind() != Kind.EQUAL_TO);
    SuggestedFix fix = new SuggestedFix();
    String customDiagnosticMessage;
    if (byteMatch) {
      String replacement = Byte.toString(((Number) literal.getValue()).byteValue());

      // Correct for poor javac 6 literal parsing.
      int actualStart = ASTHelpers.getActualStartPosition(literal, state.getSourceCode());
      if (actualStart != literal.getStartPosition()) {
        fix.replace(literal, replacement, actualStart - literal.getStartPosition(), 0);
      } else {
        fix.replace(literal, replacement);
      }
      customDiagnosticMessage = getDiagnosticMessage("byte", (int) Byte.MIN_VALUE,
          (int) Byte.MAX_VALUE, literal.toString(), Boolean.toString(willEvaluateTo));
    } else {
      fix.replace(tree, Boolean.toString(willEvaluateTo));
      customDiagnosticMessage = getDiagnosticMessage("char", (int) Character.MIN_VALUE,
          (int) Character.MAX_VALUE, literal.toString(), Boolean.toString(willEvaluateTo));
    }
    return new Description(tree, customDiagnosticMessage, fix, pattern.severity());
  }
}
