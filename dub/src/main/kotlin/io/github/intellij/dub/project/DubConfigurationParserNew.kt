package io.github.intellij.dub.project

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.isDirectory
import io.github.intellij.dub.tools.DescribeParser
import io.github.intellij.dub.tools.DescribeParserException
import io.github.intellij.dub.tools.DescribeParserImpl
import io.github.intellij.dub.tools.DubProcessListener
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.isDirectory

/**
 * This class is used to run 'dub describe' which outputs project info in json format. We parse the json
 * to gather up information about the projects dependencies
 */
class DubConfigurationParserNew @JvmOverloads constructor(
    private val projectBasePath: String,
    private val dubBinaryPath: String,
    private val silentMode: Boolean = false
) {
    private val parser: DescribeParser
    /**
     * @param project A valid D project
     * @param dubBinaryPath The location of the dub binary
     * @param silentMode When set to true notifications will not show in the UI
     * @since v1.16
     */
    /**
     * DO NOT REMOVE - This is required for backward compatibility
     * @param project A valid D project
     * @param dubBinaryPath The location of the dub binary
     */
    init {
        parser = DescribeParserImpl()
    }

    val dubProjectImportPaths: List<String?>
        /**
         * Runs <pre>dub describe --import-paths --vquiet</pre> to get import paths.
         * @return Source paths for the current project and any included dub dependencies
         * @since v1.28.1
         */
        get() {
            if (canUseDub()) {
                val optionalFuture = ApplicationManager.getApplication()
                    .executeOnPooledThread<List<String?>> {
                        importPaths(
                            projectBasePath, dubBinaryPath
                        )
                    }
                try {
                    return optionalFuture.get()
                } catch (e: InterruptedException) {
                    LOG.error("There was a problem running 'dub describe --import-paths --vquiet'", e)
                } catch (e: ExecutionException) {
                    LOG.error("There was a problem running 'dub describe --import-paths --vquiet'", e)
                }
            }
            throw RuntimeException(String.format("dub binary '%s' is not usable", dubBinaryPath))
        }
    val dubProject: Optional<DubProject>
        /**
         *
         * @return a [io.github.intellij.dub.project.DubProject] is a data class containing lots of useful details returned from 'dub describe'
         *
         * @since v1.16.2
         */
        get() {
            if (canUseDub()) {
                return parseDubConfiguration(silentMode)
            }
            return Optional.empty()
        }

    @get:Deprecated("")
    val dubPackage: Optional<DubPackage>
        /**
         * This method is now Deprecated as it makes more sense to users of this class to use getDubProject()
         * @return the root DubPackage for this project
         */
        get() {
            LOG.warn("Call to deprecated method getDubPackage()")
            val dubProject = dubProject
            return dubProject.map(DubProject::rootPackage)
        }

    /**
     * @return true if a path to a dub binary is configured and the project has a dub.sdl or dub.json
     */
    fun canUseDub(): Boolean {
        // For wsl, dub command can have any file name not only dub/dub.exe
        val dubPathValid = StringUtil.isNotEmpty(dubBinaryPath)
        return dubPathValid && Paths.get(projectBasePath).listDirectoryEntries().stream()
            .filter { f: Path -> !f.isDirectory(LinkOption.NOFOLLOW_LINKS) }
            .anyMatch { file: Path ->
                "dub.json".equals(
                    file.name,
                    ignoreCase = true
                ) || "dub.sdl".equals(file.name, ignoreCase = true)
            }
    }

    @get:Deprecated("")
    val dubPackageDependencies: List<DubPackage>
        /**
         * This method is now Deprecated as it makes more sense to users of this class to use getDubProject()
         * @return a list of DubPackage that the root DubPackage depends on. These may be sub-packages, libs, or other dub packages
         */
        get() {
            val dubProject = dubProject
            return dubProject.map(DubProject::packages).orElse(emptyList())
        }
    val packageTree: TreeNode?
        /**
         * lazilly evaluated. This isn't actually used yet so is open for changes. Plan is to use it from DUB plugin
         * @return A Tree for use in Swing UI components
         */
        get() {
            val dubProject = dubProject
            return dubProject.map { (_, rootPackage): DubProject -> buildDependencyTree(rootPackage) }
                .orElse(null)
        }

    private fun buildDependencyTree(dubPackage: DubPackage): DefaultMutableTreeNode {
        val treeNode = DefaultMutableTreeNode(dubPackage, !dubPackage.dependencies.isEmpty())
        dubPackageDependencies.stream()
            .filter { (name): DubPackage -> dubPackage.dependencies.contains(name) }
            .forEach { dependency: DubPackage -> treeNode.add(buildDependencyTree(dependency)) }
        return treeNode
    }

    /*
     * dub describe --import-paths --vquiet
     */
    @Throws(com.intellij.execution.ExecutionException::class)
    private fun importPaths(workingDirectory: String?, dubBinaryPath: String): List<String?> {
        val cmd = GeneralCommandLine()
            .withWorkDirectory(workingDirectory)
            .withExePath(dubBinaryPath)
            .withParameters("describe", "--import-paths", "--vquiet")
        val processHandler = CapturingProcessHandler(cmd)
        return processHandler.runProcess().getStdoutLines(true)
    }

    private fun parseDubConfiguration(silentMode: Boolean): Optional<DubProject> {
        if (StringUtil.isEmptyOrSpaces(dubBinaryPath)) {
            return Optional.empty()
        }
        if (!Files.isExecutable(
                Paths.get(
                    StringUtil.trim(
                        dubBinaryPath
                    )
                )
            )
        ) {
            LOG.warn("Cannot run dub as path is not executable")
            return Optional.empty()
        }
        try {
            val cmd = GeneralCommandLine()
                .withWorkDirectory(projectBasePath)
                .withExePath(StringUtil.trim(dubBinaryPath))
                .withParameters("describe")
            val dubCommand = cmd.commandLineString
            val listener = DubProcessListener()
            val proc = cmd.createProcess()
            val process = OSProcessHandler(proc, dubCommand, Charset.defaultCharset())
            process.addProcessListener(listener)
            process.startNotify()
            process.waitFor()
            val exitCode = process.exitCode
            val errors = listener.getErrors()
            if (exitCode != null && exitCode == 0) {
                if (errors.isEmpty()) {
                    LOG.info(String.format("%s exited without errors", dubCommand))
                } else {
                    if (!silentMode) {
                        LOG.warn(String.format("%s exited with %s errors", dubCommand, errors.size))
                    } else {
                        LOG.debug(String.format("%s exited with %s errors", dubCommand, errors.size))
                    }
                }
                return Optional.of(parser.parse(listener.getStdOut()))
            } else {
                errors.forEach(Consumer { message: String? -> LOG.warn(message) })
                val message = String.format("%s exited with %s:\n%s", dubCommand, exitCode, errors[0])
                LOG.warn(message)
            }
        } catch (e: com.intellij.execution.ExecutionException) {
            LOG.error("Unable to parse dub configuration", e)
            e.printStackTrace()
        } catch (e: DescribeParserException) {
            LOG.error("Unable to parse dub configuration", e)
            e.printStackTrace()
        }
        return Optional.empty()
    }

    companion object {
        private val LOG = Logger.getInstance(
            DubConfigurationParserNew::class.java
        )
    }
}
