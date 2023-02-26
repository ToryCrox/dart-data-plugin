package andrasferenczi.templater

import andrasferenczi.ext.*
import andrasferenczi.utils.mergeCalls
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.jetbrains.lang.dart.psi.DartClass


fun createJsonTemplate(
    templateManager: TemplateManager,
    className: String
): Template {

    return templateManager.createTemplate(
        TemplateType.JsonTemplate.templateKey,
        TemplateConstants.DART_TEMPLATE_GROUP
    ).apply {
        addToJson(className)
        addNewLine()
        addNewLine()
        addFromJson(className)
    }
}

private fun Template.addToJson(className: String) {

    isToReformat = true

    addTextSegment("String")
    addSpace()
    addTextSegment(TemplateConstants.TO_JSON_METHOD_NAME)
    withParentheses{}


    addSpace()
    withCurlyBraces {
        addTextSegment("return")
        addSpace()

        addTextSegment("jsonEncode(${TemplateConstants.TO_MAP_METHOD_NAME})")
        addSemicolon()

    }
}

private fun Template.addFromJson(className: String) {
    isToReformat = true

    addTextSegment("factory")
    addSpace()
    addTextSegment(className)
    addTextSegment(".")
    addTextSegment(TemplateConstants.FROM_JSON_METHOD_NAME)
    withParentheses{
        addTextSegment("String")
        addSpace()
        addTextSegment(TemplateConstants.JSON_VARIABLE_NAME)
    }

    addSpace()
    withCurlyBraces {
        addTextSegment("return")
        addSpace()

        addTextSegment(className)
        addDot()
        addTextSegment(TemplateConstants.FROM_MAP_METHOD_NAME)
        withParentheses {
            addTextSegment("jsonDecode(${TemplateConstants.JSON_VARIABLE_NAME})")
        }

        addSemicolon()
    }

}


fun createJsonDeleteCall(
    dartClass: DartClass
): (() -> Unit)? {

    val toMapMethod = dartClass.findMethodByName(TemplateConstants.TO_JSON_METHOD_NAME)
    val fromMapMethod = dartClass.findNamedConstructor(TemplateConstants.FROM_JSON_METHOD_NAME)

    return listOfNotNull(
        toMapMethod,
        fromMapMethod
    ).map { { it.delete() } }
        .mergeCalls()
}


