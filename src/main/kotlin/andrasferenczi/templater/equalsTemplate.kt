package andrasferenczi.templater

import andrasferenczi.configuration.ParseWrapper
import andrasferenczi.ext.*
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager

data class EqualsTemplateParams(
    val className: String,
    val variables: List<AliasedVariableTemplateParam>
)

fun createEqualsTemplate(
    templateManager: TemplateManager,
    parseWrapper: ParseWrapper,
    params: EqualsTemplateParams
): Template {
    val (className, selectedVariables) = params

    return templateManager.createDartTemplate(TemplateType.Equals)
        .apply {
            addTextSegment("@override")
            addNewLine()
            addTextSegment("bool")
            addSpace()
            addTextSegment("operator")
            addSpace()
            addTextSegment(TemplateConstants.EQUALS_OPERATOR_METHOD_NAME)
            withParentheses {
                addTextSegment("Object")
                addSpace()
                addTextSegment(TemplateConstants.EQUALS_VARIABLE_NAME)
            }
            addSpace()
            addTextSegment("=>")
            addNewLine()
            addTextSegment("identical")
            withParentheses {
                addTextSegment("this")
                addComma()
                addTextSegment(TemplateConstants.EQUALS_VARIABLE_NAME)
            }
            addSpace()
            addTextSegment("||")

            addNewLine()

            withParentheses {
                addTextSegment(TemplateConstants.EQUALS_VARIABLE_NAME)
                addSpace()
                addTextSegment("is")
                addSpace()
                addTextSegment(className)
                addTextSegment("&&")

                addNewLine()

                val variables: List<AliasedVariableTemplateParam> = mutableListOf<AliasedVariableTemplateParam>().apply {
                    add(
                        AliasedVariableTemplateParamImpl(
                            variableName = "runtimeType",
                            isNullable = false,
                            type = "Type",
                            publicVariableName = "runtimeType"

                        )
                    )
                    addAll(selectedVariables)
                }

                variables.forEachIndexed { index, variable ->
                    val type = variable.type
                    val isCollection = type.startsWith("List") || type.startsWith("Set") || type.startsWith("Map")
                    if (isCollection) {
                        this.addTextSegment("${parseWrapper.parseClassName}.equal(")
                        addTextSegment(variable.variableName)
                        addTextSegment(",")
                        addSpace()
                        addTextSegment(TemplateConstants.EQUALS_VARIABLE_NAME)
                        addDot()
                        addTextSegment(variable.variableName)
                        addTextSegment(")")
                    } else {
                        addTextSegment(variable.variableName)
                        addSpace()
                        addTextSegment("==")
                        addSpace()
                        addTextSegment(TemplateConstants.EQUALS_VARIABLE_NAME)
                        addDot()
                        addTextSegment(variable.variableName)
                    }

                    if (index != variables.lastIndex) {
                        addSpace()
                        addTextSegment("&&")
                        addNewLine()
                    }
                }

                addNewLine()
            }
            addSemicolon()
            addSpace()
        }

}