package com.marceloassis.notificacaoacidenteveicular.http

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.RequestBuilder
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class HttpHelper {

    fun post (json: String):String{
        val URL = "http://192.168.1.10:8080/alert"
        val headerHttp = MediaType.parse("application/json; charset=utf-8")
        //criando cliente
        val client = OkHttpClient()
        val body = RequestBody.create(headerHttp, json)
        var request = Request.Builder().url(URL).post(body).build()

        val response = client.newCall(request).execute()

        return response.body().toString()
    }
}