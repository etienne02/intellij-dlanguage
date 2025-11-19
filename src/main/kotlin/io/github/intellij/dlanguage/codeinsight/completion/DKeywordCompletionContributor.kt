package io.github.intellij.dlanguage.codeinsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.utils.*

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
                .andNot(IN_FUNCTION_CALL_EXPRESSION),
            DKeywordCompletionProvider("import")
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiComment()
                .withLanguage(DLanguage)
                .andNot(IN_IMPORT_DECLARATION),
            DKeywordCompletionProvider("auto", "void", "int", "uint")
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiComment()
                .withLanguage(DLanguage),
            DKeywordCompletionProvider("try", "catch")
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiComment()
                .withLanguage(DLanguage),
            DKeywordCompletionProvider("if", "else")
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiComment()
                .withLanguage(DLanguage),
            DKeywordCompletionProvider("continue", "break")
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
