package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.model.project.ExternalSystemSourceType
import com.intellij.openapi.externalSystem.util.ExternalSystemContentRootContributor
import com.intellij.openapi.module.Module
import io.github.intellij.dlanguage.settings.ToolKey
import io.github.intellij.dub.Dub
import java.nio.file.Path

class DubContentRootContributor : ExternalSystemContentRootContributor {

    override fun isApplicable(systemId: String): Boolean = systemId == Dub.SYSTEM_ID.id

    override fun findContentRoots(
        module: Module,
        sourceTypes: Collection<ExternalSystemSourceType>
    ): Collection<ExternalSystemContentRootContributor.ExternalContentRoot> {
        val dubPath = ToolKey.DUB_KEY.path
        dubPath?: return listOf()
        val dubConfig = DubConfigurationParser(module.project, dubPath, true)
        if (!dubConfig.canUseDub())
            return listOf()

        return listOf(
            ExternalSystemContentRootContributor.ExternalContentRoot(
                Path.of(dubConfig.dubProject.get().rootPackage.path),
                ExternalSystemSourceType.SOURCE)
            )

    }
}
