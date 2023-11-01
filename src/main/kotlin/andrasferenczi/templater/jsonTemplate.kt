package andrasferenczi.templater

import andrasferenczi.configuration.ConfigurationData
import andrasferenczi.ext.*
import andrasferenczi.utils.mergeCalls
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClass


fun createJsonTemplate(
    templateManager: TemplateManager,
    configuration: ConfigurationData,
    className: String
): Template {

    return templateManager.createTemplate(
        TemplateType.JsonTemplate.templateKey,
        TemplateConstants.DART_TEMPLATE_GROUP
    ).apply {
        addFromJson(configuration, className)
        addNewLine()
        addNewLine()
        addToJson(configuration, className)
    }
}

private fun Template.addToJson(configuration: ConfigurationData, className: String) {

    isToReformat = true

    if (configuration.jsonEqualMap) {
        addTextSegment("Map<String, dynamic>")
    } else {
        addTextSegment("String")
    }

    addSpace()
    addTextSegment(TemplateConstants.TO_JSON_METHOD_NAME)
    withParentheses{}
    if (configuration.jsonEqualMap) {
        addSpace()
        addTextSegment("=> toMap();")
        return
    }


    addSpace()
    withCurlyBraces {
        addTextSegment("return")
        addSpace()

        val parseClassName = configuration.parseWrapper.parseClassName
        if (parseClassName.isNotBlank()) {
            addTextSegment("${parseClassName}.parseString(${TemplateConstants.TO_MAP_METHOD_NAME}())")
        } else {
            addTextSegment("jsonEncode(${TemplateConstants.TO_MAP_METHOD_NAME})")
        }

        addSemicolon()

    }
}

private fun Template.addFromJson(configuration: ConfigurationData, className: String) {
    isToReformat = true

    addTextSegment("factory")
    addSpace()
    addTextSegment(className)
    addTextSegment(".")
    addTextSegment(TemplateConstants.FROM_JSON_METHOD_NAME)
    withParentheses{
        if (configuration.jsonEqualMap) {
            addTextSegment("Map<String, dynamic>")
        } else {
            addTextSegment("String")
        }
        addSpace()
        addTextSegment(TemplateConstants.JSON_VARIABLE_NAME)
    }
    if (configuration.jsonEqualMap) {
        addSpace()
        addTextSegment("=> $className.fromMap(${TemplateConstants.JSON_VARIABLE_NAME});")
        return
    }

    addSpace()
    withCurlyBraces {
        addTextSegment("return")
        addSpace()

        addTextSegment(className)
        addDot()
        addTextSegment(TemplateConstants.FROM_MAP_METHOD_NAME)
        withParentheses {
            val parseClassName = configuration.parseWrapper.parseClassName
            if (parseClassName.isNotBlank()) {
                addTextSegment("$parseClassName.parseMap(${TemplateConstants.JSON_VARIABLE_NAME})")
            } else {
                addTextSegment("jsonDecode(${TemplateConstants.JSON_VARIABLE_NAME})")
            }

        }

        addSemicolon()
    }

}


fun createJsonDeleteCall(
    dartClass: DartClass
): (() -> Unit)? {

    val toJsonMethod = dartClass.findMethodByName(TemplateConstants.TO_JSON_METHOD_NAME)
    val fromJsonMethod = dartClass.findNamedConstructor(TemplateConstants.FROM_JSON_METHOD_NAME)

    return listOfNotNull(
        toJsonMethod,
        fromJsonMethod
    ).map { { it.delete() } }
        .mergeCalls()
}


