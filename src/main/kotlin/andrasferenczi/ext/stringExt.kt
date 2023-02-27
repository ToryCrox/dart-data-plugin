package andrasferenczi.ext


/**
 * 主类型，例如List<String>中的List
 */
fun String.mainType(): String {
    return substringBefore('<');
}

fun String.subType(): String {
    return substringAfter('<').substringBeforeLast('>').split(',').last()
}