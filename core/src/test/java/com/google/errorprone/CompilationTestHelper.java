/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import static com.google.errorprone.DiagnosticTestHelper.assertHasDiagnosticOnAllMatchingLines;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Utility class for tests that need to build using error-prone.
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class CompilationTestHelper {

  private DiagnosticTestHelper diagnosticHelper;
  private ErrorProneCompiler compiler;

  public CompilationTestHelper(Scanner scanner) {
    diagnosticHelper = new DiagnosticTestHelper();
    compiler = new ErrorProneCompiler.Builder()
        .report(scanner)
        .listenToDiagnostics(diagnosticHelper.collector)
        .build();
  }

  public void assertCompileSucceeds(File source) {
    int exitCode = compiler.compile(new String[]{"-Xjcov", source.getAbsolutePath()});
    assertThat(exitCode, is(0));
  }

  /**
   * Assert that the compile fails, and that for each line of the test file that contains
   * the pattern //BUG("foo"), the diagnostic at that line contains "foo".
   */
  public void assertCompileFailsWithMessages(File source) throws IOException {
    int exitCode = compiler.compile(new String[]{"-Xjcov", "-encoding", "UTF-8", source.getAbsolutePath()});
    assertThat("Compiler returned an unexpected error code", exitCode, is(1));
    assertHasDiagnosticOnAllMatchingLines(diagnosticHelper.getDiagnostics(), source);
  }

  /**
   * Constructs the absolute paths to the given files, so they can be passed as arguments to the
   * compiler.
   */
  public static String[] sources(Class<?> klass, String... files) throws URISyntaxException {
    String[] result = new String[files.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = new File(klass.getResource("/" + files[i]).toURI()).getAbsolutePath();
    }
    return result;
  }
  
  public static File fileFromResource(Class<?> loadingClass, String resourceName) throws URISyntaxException{
    return new File(loadingClass.getResource(resourceName).toURI());
  }
}
