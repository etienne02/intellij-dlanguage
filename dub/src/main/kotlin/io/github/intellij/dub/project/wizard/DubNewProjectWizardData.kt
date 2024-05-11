package io.github.intellij.dub.project.wizard

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.projectRoots.Sdk

interface DubNewProjectWizardData {

    val sdkProperty: GraphProperty<Sdk?>
    val dubDslProperty: GraphProperty<DubNewProjectWizardStep.DubDsl>
    val dubProjectTypeProperty: GraphProperty<DubNewProjectWizardStep.DubProjectType>

    var sdk: Sdk?

    var dubDsl: DubNewProjectWizardStep.DubDsl
    var dubProjectType: DubNewProjectWizardStep.DubProjectType
}
