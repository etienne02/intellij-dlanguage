package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SmartList
import io.github.intellij.dub.Dub
import io.github.intellij.dub.settings.DubProjectSettings
import io.github.intellij.dub.settings.DubSettings
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set

class DubAutoImportAware : ExternalSystemAutoImportAware {
    override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
        if (!changedFileOrDirPath.endsWith("dub.sdl") &&
            !changedFileOrDirPath.endsWith("dub.json")) {
            return null
        }

        val file = File(changedFileOrDirPath)
        if (file.isDirectory) {
            return null
        }

        val manager = ExternalSystemApiUtil.getManager(Dub.SYSTEM_ID)!!

        val systemSettings = manager.settingsProvider.`fun`(project)
        val projectsSettings = systemSettings.linkedProjectsSettings
        if (projectsSettings.isEmpty()) {
            return null
        }
        val rootPaths: MutableMap<String, String> = HashMap()
        for (setting in projectsSettings) {
            if (setting != null) {
                for (path in setting.modules) {
                    rootPaths[File(path).path] = setting.externalProjectPath
                }
            }
        }

        var f = file.parentFile
        while (f != null) {
            val dirPath = f.path
            if (rootPaths.containsKey(dirPath)) {
                return rootPaths[dirPath]
            }
            f = f.parentFile
        }
        return null
    }

    override fun getAffectedExternalProjectFiles(projectPath: String, project: Project): MutableList<File> {
        val files: MutableList<File> = SmartList()

        // add global settings.json
        // TODO add path listed in https://dub.pm/settings


        // add project-specific settings.json
        val projectSettings: DubProjectSettings? =
            DubSettings.getInstance(project).getLinkedProjectSettings(projectPath)
        files.add(
            File(
                if (projectSettings == null) projectPath else projectSettings.externalProjectPath,
                "settings.json"
            )
        )
        return files
    }
}
