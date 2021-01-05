/*
 * SonarQube Java
 * Copyright (C) 2012-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.java.CheckTestUtils;
import org.sonar.java.ast.JavaAstScanner;
import org.sonar.java.checks.methods.AbstractMethodDetection;
import org.sonar.java.model.JavaTree;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodReferenceTree;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractMethodDetectionTest {

  @Test
  void detected() {
    Visitor visitor = new Visitor(
      MethodMatchers.create().ofTypes("A").names("method")
        .addParametersMatcher("int")
        .addParametersMatcher("java.lang.String[]")
        .build());
    JavaAstScanner.scanSingleFileForTests(CheckTestUtils.inputFile("src/test/files/checks/AbstractMethodDetection.java"), new VisitorsBridge(visitor));

    assertThat(visitor.lines)
      .hasSize(3)
      .containsExactly(15, 17, 19);
  }

  @Test
  void withAnyParameters() throws Exception {
    Visitor visitor = new Visitor(
      MethodMatchers.create().ofTypes("A").names("method").withAnyParameters().build());
    JavaAstScanner.scanSingleFileForTests(CheckTestUtils.inputFile("src/test/files/checks/AbstractMethodDetection.java"), new VisitorsBridge(visitor));

    assertThat(visitor.lines).containsExactly(14, 15, 16, 17, 19);
  }

  @Test
  void withoutParameter() throws Exception {
    Visitor visitor = new Visitor(
      MethodMatchers.create().ofTypes("A").names("method").addWithoutParametersMatcher().build());
    JavaAstScanner.scanSingleFileForTests(CheckTestUtils.inputFile("src/test/files/checks/AbstractMethodDetection.java"), new VisitorsBridge(visitor));

    assertThat(visitor.lines).containsExactly(14);
  }

  class Visitor extends AbstractMethodDetection {

    public List<Integer> lines = new ArrayList<>();
    private MethodMatchers methodInvocationMatchers;

    public Visitor(MethodMatchers methodInvocationMatchers) {
      this.methodInvocationMatchers = methodInvocationMatchers;
    }

    @Override
    protected MethodMatchers getMethodInvocationMatchers() {
      return methodInvocationMatchers;
    }

    @Override
    protected void onMethodInvocationFound(MethodInvocationTree tree) {
      lines.add(((JavaTree) tree).getLine());
    }

    @Override
    protected void onMethodReferenceFound(MethodReferenceTree methodReferenceTree) {
      lines.add(((JavaTree) methodReferenceTree).getLine());
    }
  }

}
