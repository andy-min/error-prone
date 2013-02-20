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

package com.google.errorprone.matchers;

import static com.google.errorprone.matchers.Matchers.isPrimitiveType;
import static com.google.errorprone.matchers.Matchers.variableType;
import static org.junit.Assert.assertTrue;

import com.google.errorprone.Scanner;
import com.google.errorprone.VisitorState;

import com.sun.source.tree.MethodTree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class MethodHasParametersTest extends CompilerBasedTest {

  final List<ScannerTest> tests = new ArrayList<ScannerTest>();

  @Before
  public void setUp() throws IOException {
    tests.clear();
    writeFile("SampleAnnotation1.java",
        "package com.google;",
        "public @interface SampleAnnotation1 {}"
    );
    writeFile("SampleAnnotation2.java",
        "package com.google;",
        "public @interface SampleAnnotation2 {}"
    );
  }

  @After
  public void tearDown() {
    for (ScannerTest test : tests) {
      test.assertDone();
    }
  }

  @Test
  public void shouldMatchSingleParameter() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  public void A(int i) {}",
        "}");
    assertCompiles(methodMatches(true, new MethodHasParameters(true, variableType(
        isPrimitiveType()))));
    assertCompiles(methodMatches(true, new MethodHasParameters(false, variableType(
        isPrimitiveType()))));
  }

  @Test
  public void shouldNotMatchNoParameters() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  public void A() {}",
        "}");
    assertCompiles(methodMatches(false, new MethodHasParameters(true, variableType(
        isPrimitiveType()))));
    assertCompiles(methodMatches(false, new MethodHasParameters(false, variableType(
        isPrimitiveType()))));
  }

  @Test
  public void shouldNotMatchNonmatchingParameter() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  public void A(Object obj) {}",
        "}");
    assertCompiles(methodMatches(false, new MethodHasParameters(true, variableType(
        isPrimitiveType()))));
    assertCompiles(methodMatches(false, new MethodHasParameters(false, variableType(
        isPrimitiveType()))));
  }

  @Test
  public void testMultipleParameters() throws IOException {
    writeFile("A.java",
        "package com.google;",
        "public class A {",
        "  private A() {}",
        "  public void A(int i, Object obj) {}",
        "}");
    assertCompiles(methodMatches(true, new MethodHasParameters(true, variableType(
        isPrimitiveType()))));
    assertCompiles(methodMatches(false, new MethodHasParameters(false, variableType(
        isPrimitiveType()))));
  }


  private abstract class ScannerTest extends Scanner {
    public abstract void assertDone();
  }

  private Scanner methodMatches(final boolean shouldMatch, final MethodHasParameters toMatch) {
    ScannerTest test = new ScannerTest() {
      private boolean matched = false;

      @Override
      public Void visitMethod(MethodTree node, VisitorState visitorState) {
        if (toMatch.matches(node, visitorState)) {
          matched = true;
        }
        return super.visitMethod(node, visitorState);
      }

      @Override
      public void assertDone() {
        assertTrue(shouldMatch == matched);
      }
    };
    tests.add(test);
    return test;
  }

}
