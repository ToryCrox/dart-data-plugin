package andrasferenczi.configuration

import andrasferenczi.templater.TemplateConstants

// Input
// Default values
data class ConfigurationData(
    val copyWithMethodName: String,
    val useRequiredAnnotation: Boolean,
    val useNewKeyword: Boolean,
    val useConstForConstructor: Boolean,
    val optimizeConstCopy: Boolean,
    val addKeyMapperForMap: Boolean,
    val noImplicitCasts: Boolean,
    val nullSafety: Boolean,
    /**
     * 是否使用下划线的Json名称
     */
    val useUnderlineJsonName: Boolean,
    /**
     * Json方法是否和Map方法相同
     */
    val jsonEqualMap: Boolean,
    val parseWrapper: ParseWrapper
) {
    companion object {
        val DEFAULT_DATA = ConfigurationData(
            copyWithMethodName = TemplateConstants.COPYWITH_DEFAULT_METHOD_NAME,
            useRequiredAnnotation = true,
            useNewKeyword = false,
            useConstForConstructor = true,
            optimizeConstCopy = false,
            addKeyMapperForMap = false,
            noImplicitCasts = true,
            nullSafety = true,
            useUnderlineJsonName = true,
            jsonEqualMap = true,
            parseWrapper = ParseWrapper(parseClassName = "TypeUtil")
        )

        val TEST_DATA = DEFAULT_DATA.copy(
            copyWithMethodName = "testData",
            useConstForConstructor = false
        )

    }
}

data class ParseWrapper(
    val parseClassName: String = ""
)