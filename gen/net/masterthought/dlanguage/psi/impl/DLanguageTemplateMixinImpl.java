// This is a generated file. Not intended for manual editing.
package net.masterthought.dlanguage.psi.impl;

import net.masterthought.dlanguage.psi.interfaces.DLanguageIdentifier;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static net.masterthought.dlanguage.psi.DLanguageTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import net.masterthought.dlanguage.psi.*;

public class DLanguageTemplateMixinImpl extends ASTWrapperPsiElement implements DLanguageTemplateMixin {

  public DLanguageTemplateMixinImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DLanguageVisitor) ((DLanguageVisitor)visitor).visitTemplateMixin(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DLanguageIdentifier getIdentifier() {
    return findChildByClass(DLanguageIdentifier.class);
  }

  @Override
  @NotNull
  public DLanguageMixinTemplateName getMixinTemplateName() {
    return findNotNullChildByClass(DLanguageMixinTemplateName.class);
  }

  @Override
  @Nullable
  public DLanguageTemplateArguments getTemplateArguments() {
    return findChildByClass(DLanguageTemplateArguments.class);
  }

  @Override
  @NotNull
  public PsiElement getKwMixin() {
    return findNotNullChildByType(KW_MIXIN);
  }

  @Override
  @NotNull
  public PsiElement getOpScolon() {
    return findNotNullChildByType(OP_SCOLON);
  }

}
