package com.longtech.arouter.applike.router.arouter.json

import android.content.Context
import com.alibaba.android.arouter.facade.service.SerializationService

import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import java.lang.reflect.Type

@Route(path = "/ARouterJson/service/json")
class JsonServiceImpl : SerializationService {
    private var gson: Gson? = null
    override fun <T> json2Object(input: String, clazz: Class<T>): T? {
        return gson?.fromJson(input, clazz)
    }

    override fun object2Json(instance: Any): String? {
        return gson?.toJson(instance)
    }

    override fun <T> parseObject(input: String?, clazz: Type?): T? {
        return gson?.fromJson(input, clazz)
    }

    override fun init(context: Context?) {
        gson = Gson()
    }
}