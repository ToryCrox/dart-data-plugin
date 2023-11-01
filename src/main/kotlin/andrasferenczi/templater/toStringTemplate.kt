package andrasferenczi.templater

import andrasferenczi.configuration.ConfigurationData
import andrasferenczi.ext.*
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager

data class ToStringTemplateParams(
    val className: String,
    val variables: List<NamedVariableTemplateParam>
)

fun createToStringTemplate(
    templateManager: TemplateManager,
    params: ToStringTemplateParams,
    configuration: ConfigurationData
): Template {
    val (className, variables) = params

    return templateManager.createTemplate(
        TemplateType.ToString.templateKey,
        TemplateConstants.DART_TEMPLATE_GROUP
    ).apply {
        isToReformat = true

        addTextSegment("@override")
        addNewLine()
        addTextSegment("String")
        addSpace()
        addTextSegment(TemplateConstants.TO_STRING_METHOD_NAME)
        withParentheses { }
        addSpace()
        withCurlyBraces {
            addNewLine()
            addTextSegment("return")
            addSpace()
            withSingleQuotes {
                addTextSegment(className)
                addTextSegment("\$")
                addTextSegment("{")
                addTextSegment("${configuration.parseWrapper.parseClassName}.parseString(toMap())")
                addTextSegment("}")
                // Cannot use with, since they are closed in different order
//                addTextSegment("{")
//
//                variables.forEachIndexed { index, variable ->
//
//                    addTextSegment(variable.variableName)
//                    addTextSegment(":")
//                    addSpace()
//                    addTextSegment("\$")
//                    addTextSegment(variable.variableName)
//                    if (index <= variables.size -1) {
//                        addComma()
//                        addSpace()
//                    }
//                    //addNewLine()
//                }
//
//                addTextSegment("}")
            }

            addSemicolon()
        }

        addSpace()
    }

}
