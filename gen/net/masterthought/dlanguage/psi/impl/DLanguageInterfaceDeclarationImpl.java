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

public class DLanguageInterfaceDeclarationImpl extends ASTWrapperPsiElement implements DLanguageInterfaceDeclaration {

  public DLanguageInterfaceDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DLanguageVisitor) ((DLanguageVisitor)visitor).visitInterfaceDeclaration(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DLanguageAggregateBody getAggregateBody() {
    return findChildByClass(DLanguageAggregateBody.class);
  }

  @Override
  @Nullable
  public DLanguageBaseInterfaceList getBaseInterfaceList() {
    return findChildByClass(DLanguageBaseInterfaceList.class);
  }

  @Override
  @Nullable
  public DLanguageIdentifier getIdentifier() {
    return findChildByClass(DLanguageIdentifier.class);
  }

  @Override
  @Nullable
  public DLanguageInterfaceTemplateDeclaration getInterfaceTemplateDeclaration() {
    return findChildByClass(DLanguageInterfaceTemplateDeclaration.class);
  }

  @Override
  @Nullable
  public PsiElement getKwInterface() {
    return findChildByType(KW_INTERFACE);
  }

  @Override
  @Nullable
  public PsiElement getOpScolon() {
    return findChildByType(OP_SCOLON);
  }

}
