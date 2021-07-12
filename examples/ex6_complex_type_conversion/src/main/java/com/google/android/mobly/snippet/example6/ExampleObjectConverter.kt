package com.google.android.mobly.snippet.example6

import com.google.android.mobly.snippet.SnippetObjectConverter
import com.google.protobuf.GeneratedMessageLite
import java.lang.reflect.Type
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * Example showing how to supply custom object converter to Mobly Snippet Lib.
 */
class ExampleObjectConverter : SnippetObjectConverter {

  private val protoBytesKey = "proto_bytes"

  private fun isProtoLiteMessage(c: Class<*>): Boolean =
    c.superclass == GeneratedMessageLite::class.java

  @Throws(JSONException::class)
  override fun serialize(anyObject: Any): JSONObject? {
    val result = JSONObject()
    if (anyObject is CustomType) {
      result.put("Value", anyObject.myValue)
      return result
    }

    (anyObject.javaClass).takeIf { isProtoLiteMessage(it) }?.let {
      val protoBytes = it.getMethod("toByteArray").invoke(anyObject) as ByteArray
      result.put(protoBytesKey, JSONArray().apply {
        protoBytes.mapIndexed { index, byte -> put(index, byte.toInt() and 0xFF) }
      })
      return result
    }
    return null
  }

  @Throws(JSONException::class)
  override fun deserialize(jsonObject: JSONObject, type: Type): Any? {
    if (type == CustomType::class.java) {
      return CustomType(jsonObject.getString("Value"))
    }

    (type as Class<*>).takeIf { isProtoLiteMessage(it) }?.let {
      val jsonArray = jsonObject.getJSONArray(protoBytesKey)
      val protoBytes = ByteArray(jsonArray.length()) { i -> jsonArray.getInt(i).toByte() }
      val methodParseFrom = it.getMethod("parseFrom", ByteArray::class.java)
      return methodParseFrom.invoke(null, protoBytes)
    }

    return null
  }
}