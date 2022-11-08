package com.marceloassis.notificacaoacidenteveicular

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.MediaType
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.RequestBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.anko.internals.AnkoInternals.createAnkoContext

class HttpHelper {
    fun post (json:String) : String{

        //Definir URL server
        val URL = "http://192.168.1.10:8080/alert"
        //cabecalho
        val headerHttp = "application/json; charset=utf-8".toMediaType()
        //client
        val client = OkHttpClient()
        // criar o  body
        val body = json.toRequestBody(headerHttp)
        //construi a requisisção
        var request = Request.Builder().url(URL).post(body).build()

        val response = client.newCall(request).execute()

        return response.body.toString()
    }
    fun get (){
        //Definir URL server
        val URL = "http://192.168.1.10:8080/alert"
        //cabecalho
        val headerHttp = "application/json; charset=utf-8".toMediaType()
        //client
        val client = OkHttpClient()
        // criar o  body
        val body = headerHttp.toString()
        //construi a requisisção
        var request = Request.Builder().url(URL).get().build()

        val response = client.newCall(request).execute()
        println(response)

    }
}