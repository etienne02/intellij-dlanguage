package io.github.intellij.dlanguage.codeinsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.utils.ForStatement
import io.github.intellij.dlanguage.utils.ForeachTypeList
import io.github.intellij.dlanguage.utils.FunctionCallExpression
import io.github.intellij.dlanguage.utils.ImportDeclaration
import io.github.intellij.dlanguage.utils.LiteralExpression
import io.github.intellij.dlanguage.utils.Parameters

class DKeywordCompletionContributor : CompletionContributor(), DumbAware {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(DLanguage)
                .andNot(IN_IMPORT_DECLARATION)
                .andNot(IN_PARAM_LIST)
                .andNot(IN_STRING_LITERAL)
                .andNot(IN_FOR_STATEMENT)
                .andNot(IN_FOREACH_TYPE_LIST)
                .andNot(IN_FUNCTION_CALL_EXPRESSION)
            ,
            DKeywordCompletionProvider("import")
        )
    }
}

private val IN_IMPORT_DECLARATION = PlatformPatterns.psiElement()
    .inside(PlatformPatterns.psiElement(ImportDeclaration::class.java))

private val IN_PARAM_LIST = PlatformPatterns.psiElement()
    .inside(Parameters::class.java)

private val IN_STRING_LITERAL = PlatformPatterns.psiElement()
    .inside(LiteralExpression::class.java)

private val IN_FOR_STATEMENT = PlatformPatterns.psiElement()
    .inside(ForStatement::class.java)

private val IN_FOREACH_TYPE_LIST = PlatformPatterns.psiElement()
    .inside(ForeachTypeList::class.java)

private val IN_FUNCTION_CALL_EXPRESSION = PlatformPatterns.psiElement()
    .inside(FunctionCallExpression::class.java)

private class DKeywordCompletionProvider(private vararg val keywords: String): CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        for (keyword in keywords) {
            val element = LookupElementBuilder.create(keyword).withBoldness(true)
            result.addElement(element)
        }
    }

}
