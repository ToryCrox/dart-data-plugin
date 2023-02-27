package andrasferenczi.action

import andrasferenczi.action.data.GenerationData
import andrasferenczi.action.data.PerformAction
import andrasferenczi.action.init.ActionData
import andrasferenczi.action.utils.selectFieldsWithDialog
import andrasferenczi.configuration.ConfigurationDataManager
import andrasferenczi.ext.psi.extractClassName
import andrasferenczi.templater.*
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.lang.dart.psi.DartClassDefinition

class JsonAction : BaseAnAction() {


    override fun processAction(
        event: AnActionEvent,
        actionData: ActionData,
        dartClass: DartClassDefinition
    ): PerformAction? {
        val declarations = selectFieldsWithDialog(actionData.project, dartClass) ?: return null

        return processAction(
            GenerationData(actionData, dartClass, declarations)
        )
    }


    companion object : StaticActionProcessor {

        override fun processAction(generationData: GenerationData): PerformAction? {
            val (actionData, dartClass, declarations) = generationData
            val (project, _, _, _) = actionData

            val toMapMethod = dartClass.findMethodByName(TemplateConstants.TO_MAP_METHOD_NAME)
            val fromMapMethod = dartClass.findNamedConstructor(TemplateConstants.FROM_MAP_METHOD_NAME)


            val mapPerformAction: PerformAction? = if (toMapMethod == null || fromMapMethod == null) {
                MapAction.processAction(generationData);
            } else null

            val templateManager = TemplateManager.getInstance(project)
            val configuration = ConfigurationDataManager.retrieveData(project)
            val dartClassName = dartClass.extractClassName()


            val template = createJsonTemplate(
                templateManager,
                configuration,
                dartClassName
            )

            val deleteCall = createJsonDeleteCall(dartClass)


            return PerformAction(
                {
                    mapPerformAction?.deleteAction?.invoke()
                    deleteCall?.invoke()
                },
                mapPerformAction?.templatesToAdd.orEmpty() + listOf(template)
            )
        }


    }
}