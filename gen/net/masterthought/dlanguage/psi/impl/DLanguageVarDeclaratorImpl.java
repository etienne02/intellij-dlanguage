// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi.impl;

import net.masterthought.dlanguage.psi.interfaces.DLanguageIdentifier;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import net.masterthought.dlanguage.psi.*;

public class DLanguageVarDeclaratorImpl extends ASTWrapperPsiElement implements DLanguageVarDeclarator {

  public DLanguageVarDeclaratorImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DLanguageVisitor) ((DLanguageVisitor)visitor).visitVarDeclarator(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DLanguageBasicType2 getBasicType2() {
    return findChildByClass(DLanguageBasicType2.class);
  }

  @Override
  @NotNull
  public DLanguageIdentifier getIdentifier() {
    return findNotNullChildByClass(DLanguageIdentifier.class);
  }

}
