package andrasferenczi.templater

import andrasferenczi.configuration.ParseWrapper
import andrasferenczi.constants.DART_BASIC_TYPES
import andrasferenczi.ext.*
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager

data class MapTemplateParams(
    val className: String,
    val variables: List<AliasedVariableTemplateParam>,
    val useNewKeyword: Boolean,
    val addKeyMapper: Boolean,
    val noImplicitCasts: Boolean,
    val parseWrapper: ParseWrapper
)

// The 2 will be generated with the same function
fun createMapTemplate(
    templateManager: TemplateManager,
    params: MapTemplateParams
): Template {

    return templateManager.createTemplate(
        TemplateType.MapTemplate.templateKey,
        TemplateConstants.DART_TEMPLATE_GROUP
    ).apply {
        addFromMap(params)
        addNewLine()
        addNewLine()
        addToMap(params)
    }
}

private fun Template.addAssignKeyMapperIfNotValid() {
    addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
    addSpace()
    addTextSegment("??=")
    addSpace()
    withParentheses {
        addTextSegment(TemplateConstants.KEY_VARIABLE_NAME)
    }
    addSpace()
    addTextSegment("=>")
    addSpace()
    addTextSegment(TemplateConstants.KEY_VARIABLE_NAME)
    addSemicolon()
    addNewLine()
    addNewLine()
}

private fun Template.addToMap(params: MapTemplateParams) {
    val (_, variables, _, addKeyMapper, _) = params

    isToReformat = true

    addTextSegment("Map<String, dynamic>")
    addSpace()
    addTextSegment(TemplateConstants.TO_MAP_METHOD_NAME)
    withParentheses {
        if (addKeyMapper) {
            withCurlyBraces {
                addNewLine()
                addTextSegment("String Function(String key)? ${TemplateConstants.KEYMAPPER_VARIABLE_NAME}")
                addComma()
                addNewLine()
            }
        }
    }
    addSpace()
    withCurlyBraces {

        if (addKeyMapper) {
            addAssignKeyMapperIfNotValid()
        }

        addTextSegment("return")
        addSpace()
        withCurlyBraces {
            addNewLine()

            variables.forEach {
                "'${it.mapKeyString}'".also { keyParam ->
                    if (addKeyMapper) {
                        addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                        withParentheses {
                            addTextSegment(keyParam)
                        }
                    } else {
                        addTextSegment(keyParam)
                    }
                }

                addTextSegment(":")
                addSpace()
                toJsonMapValue(it.type, it.variableName)
                addComma()
                addNewLine()
            }
        }
        addSemicolon()
    }
}

private fun Template.toJsonMapValue(type: String, variableName: String) {
    if (type.mainType() in setOf("List", "Set") && type.subType()  !in DART_BASIC_TYPES){
        addTextSegment(variableName)
        addTextSegment(".map((e)=> ")
        toJsonMapValue(type.subType(), "e")
        addTextSegment(").toList()")
    } else if (type.mainType() in DART_BASIC_TYPES) {
        addTextSegment(variableName)
    }  else {
        addTextSegment("$variableName.toMap()")
    }
}

private fun Template.addFromMap(
    params: MapTemplateParams
) {
    val (className, variables, useNewKeyword, addKeyMapper, noImplicitCasts) = params

    isToReformat = true

    addTextSegment("factory")
    addSpace()
    addTextSegment(className)
    addTextSegment(".")
    addTextSegment(TemplateConstants.FROM_MAP_METHOD_NAME)
    withParentheses {
        if (addKeyMapper) {
            addNewLine()
            // New line does not format, no matter what is in this if statement
            addSpace()
        }
        addTextSegment("Map<String, dynamic>")
        addSpace()
        addTextSegment(TemplateConstants.MAP_VARIABLE_NAME)

        if (addKeyMapper) {
            addComma()
            addSpace()
            withCurlyBraces {
                addNewLine()
                addTextSegment("String Function(String ${TemplateConstants.KEY_VARIABLE_NAME})?")
                addSpace()
                addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                addComma()
                addNewLine()
            }
        }
    }
    addSpace()
    withCurlyBraces {

        if (addKeyMapper) {
            addAssignKeyMapperIfNotValid()
        }

        addTextSegment("return")
        addSpace()
        if (useNewKeyword) {
            addTextSegment("new")
            addSpace()
        }
        addTextSegment(className)
        withParentheses {
            addNewLine()
            variables.forEach {
                addTextSegment(it.publicVariableName)
                addTextSegment(":")
                addSpace()

                // 添加map[key]
                val addMapValue = {
                    addTextSegment(TemplateConstants.MAP_VARIABLE_NAME)
                    withBrackets {
                        "'${it.mapKeyString}'".also { keyParam ->
                            if (addKeyMapper) {
                                addTextSegment(TemplateConstants.KEYMAPPER_VARIABLE_NAME)
                                withParentheses {
                                    addTextSegment(keyParam)
                                }
                            } else {
                                addTextSegment(keyParam)
                            }
                        }
                    }
                }

                val isWrapped = withParseWrapper(it.type, params.parseWrapper) {
                    addMapValue()
                }

                // 非空设置默认值
                if (!isWrapped && !it.isNullable) {
                    addSpace()
                    //addTextSegment("as")
                    addTextSegment("??")
                    addSpace()
                    addTextSegment(it.defaultValue)
                }

                addComma()
                addNewLine()
            }
        }
        addSemicolon()
    }
}

// 包裹自定义装换
fun Template.withParseWrapper(
    typeName: String,
    parseWrapper: ParseWrapper,
    action: Template.() -> Unit
): Boolean {
    val parseWrapperMethod = when(typeName){
        "String" -> "parseString"
        "int" -> "parseInt"
        "double" -> "parseDouble"
        "bool" -> "parseBool"
        "Map" -> "parseMap"
        "Map<String, dynamic>" -> "parseMap"
        "List<Int>" -> "parseIntList"
        "List<String>" -> "parseStringList"
        else -> ""
    }
    if (parseWrapperMethod.isNotBlank()) {
        this.addTextSegment("${parseWrapper.parseClassName}.")
        this.addTextSegment(parseWrapperMethod)
        this.addTextSegment("(")
        this.action()
        this.addTextSegment(")")
        return true
    } else if (typeName.startsWith("List")) {
        val subTypeName = typeName.subType()
        this.addTextSegment("${parseWrapper.parseClassName}.")
        this.addTextSegment("parseList")
        this.addTextSegment("(")
        this.action()
        this.addTextSegment(", (e) => ")
        this.withParseWrapper(subTypeName, parseWrapper) {
            this.addTextSegment("e")
        }
        this.addTextSegment(")")
        return true
    } else if (typeName.startsWith("Set")) {
        this.withParseWrapper(typeName.replace("Set", "List"), parseWrapper) {
            action()
        }
        addTextSegment(".toSet()")
        return true
    } else if (typeName == "dynamic"){
        action()
        return false
    } else {
        this.addTextSegment(typeName)
        this.addTextSegment(".fromMap")
        this.withParentheses {
            this.addTextSegment("${parseWrapper.parseClassName}.")
            this.addTextSegment("parseMap")
            this.withParentheses {
                action()
            }
        }
        return true
    }
}
