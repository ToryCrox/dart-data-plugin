package andrasferenczi.action.my

import andrasferenczi.action.StaticActionProcessor
import andrasferenczi.action.data.GenerationData
import andrasferenczi.action.data.PerformAction
import andrasferenczi.configuration.ConfigurationDataManager
import andrasferenczi.declaration.fullTypeName
import andrasferenczi.declaration.isNullable
import andrasferenczi.declaration.variableName
import andrasferenczi.ext.psi.extractClassName
import andrasferenczi.ext.psi.findMethodsByName
import andrasferenczi.templater.*
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClassDefinition

class MyEqualsAction {

    companion object : StaticActionProcessor {

        private fun createDeleteCall(dartClass: DartClassDefinition): (() -> Unit)? {
            val equals = dartClass.findMethodsByName(TemplateConstants.EQUALS_OPERATOR_METHOD_NAME)
                .firstOrNull()
                ?: return null

            return { equals.delete() }
        }

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData

            val project = actionData.project

            val configuration = ConfigurationDataManager.retrieveData(project)
            val templateManager = TemplateManager.getInstance(project)
            val dartClassName = dartClass.extractClassName()

            val template = createEqualsTemplate(
                templateManager,
                configuration.parseWrapper,
                EqualsTemplateParams(
                    className = dartClassName,
                    variables = declarations.map {
                        AliasedVariableTemplateParamImpl(
                            variableName = it.variableName,
                            isNullable = it.isNullable,
                            publicVariableName = it.publicVariableName,
                            type = it.fullTypeName ?: throw RuntimeException("No type is available - this variable should not be assignable from constructor")
                        )
                    }
                )
            )

            return PerformAction(
                createDeleteCall(dartClass),
                template
            )
        }
    }

}