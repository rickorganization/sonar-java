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

import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S2141")
public class ClassWithoutHashCodeInHashStructureCheck extends IssuableSubscriptionVisitor {

  private static final MethodMatchers EQUALS_MATCHER = MethodMatchers.create()
    .ofAnyType()
    .names("equals")
    .addParametersMatcher("java.lang.Object")
    .build();

  private static final MethodMatchers HASHCODE_MATCHER = MethodMatchers.create()
    .ofAnyType()
    .names("hashCode")
    .addWithoutParametersMatcher()
    .build();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.NEW_CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    if (!hasSemantic()) {
      return;
    }
    Type type = ((NewClassTree) tree).symbolType();
    if (type.isParameterized() && useHashDataStructure(type)) {
      Symbol.TypeSymbol symbol = type.typeArguments().get(0).symbol();
      if (implementsEquals(symbol) && !implementsHashCode(symbol)) {
        reportIssue(tree, "Add a \"hashCode()\" method to \"" + symbol.name() + "\" or remove it from this hash.");
      }
    }
  }

  private static boolean useHashDataStructure(Type type) {
    return type.isSubtypeOf("java.util.HashMap") || type.isSubtypeOf("java.util.HashSet") || type.isSubtypeOf("java.util.Hashtable");
  }

  private static boolean implementsEquals(Symbol.TypeSymbol symbol) {
    return symbol.lookupSymbols("equals").stream()
      .filter(s -> !s.isAbstract())
      .anyMatch(EQUALS_MATCHER::matches);
  }

  private static boolean implementsHashCode(Symbol.TypeSymbol symbol) {
    return symbol.lookupSymbols("hashCode").stream().anyMatch(HASHCODE_MATCHER::matches);
  }
}
