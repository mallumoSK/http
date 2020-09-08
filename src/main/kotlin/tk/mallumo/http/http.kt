@file:Suppress("ClassName")

package tk.mallumo.http

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.closeQuietly
import tk.mallumo.http.http.Utils.MT_JSON
import tk.mallumo.http.http.Utils.buildRequestUrl
import tk.mallumo.http.http.Utils.buildResponse
import tk.mallumo.log.log
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass


object http {

    /**
     * Tool for create your own authentication data class
     * @param key usually "Authorization"
     * @param value your custom authenification value
     */
    open class Auth(val key: String, val value: String)

    /**
     * ### Implementation of Basic authentication
     *
     * @param name user name
     * @param pass user password
     */
    @Suppress("unused")
    class AuthBasic(name: String, pass: String) : Auth(
        "Authorization",
        Credentials.basic(name, pass, charset("UTF-8"))
    )

    /**
     * ### Response holder of request
     * @param data nullable server response
     * @param code server response code
     * @param exception is exception happens, this library newer throw exception **BUT** it will be returned by this parameter
     * @param message status message from server of extracted error message
     * @param headers server response headers
     * @param isOk simply validation of data: ``code == 200 && data != null``
     */
    data class Response<T>(
        val data: T?,
        val code: Int,
        val exception: Throwable? = null,
        val message: String?,
        val headers: Headers? = null,
        val isOk: Boolean = code == 200 && data != null
    )

    /**
     * ### Request method type of GET
     *
     * #### Function requires type of response classes
     *
     * * ByteArray -> response cast read all bydes and forward back
     * * String -> response is converted to string
     * * File -> in temporary files will be created file and fill with response data
     * * else -> String from server will be classified as json and convert into forwarded type object type
     *
     * @param url server url
     * @param queryParts if url ends with '?' this parameters will be used as url...?key1=value1&key2=value2 otherwise  url.../key1/value1/key2/value2
     * @param headers yours request headers
     * @param auth helper for authentication see http.AuthBasic
     * @param client jou can use your custom OkHttpClient instance, or use default http.Utils.client
     * @param loggerOUT in console will be printed request
     * @param loggerIN in console will be printed response
     *
     * @see Response
     * @see AuthBasic
     * @see Auth
     * @see Utils.gson
     * @see Utils.client
     */
    suspend inline fun <reified T : Any> get(
        url: String,
        queryParts: SortedMap<String, String> = sortedMapOf(),
        headers: Map<String, String> = mapOf(),
        auth: Auth? = null,
        client: OkHttpClient = Utils.client,
        loggerOUT: Boolean = false,
        loggerIN: Boolean = false
    ): Response<T> = withContext(Dispatchers.IO) {

        val id = Utils.requestID.getAndIncrement()
        if (loggerOUT) Utils.loggerOutGET(id, url, queryParts, headers, auth)

        val request = Request.Builder().apply {
            url(buildRequestUrl(url, queryParts))
            headers(headers)
            get()
            auth?.also {
                addHeader(it.key, it.value)
            }
        }.build()


        try {
            buildResponse(client.newCall(request).await(), T::class).also {
                if (loggerIN) log("($id) ${Utils.gson.toJson(it)}")
            }
        } catch (e: Throwable) {
            Response<T>(null, code =-1, exception =  e,  message =e.message).also {
                if (loggerIN) log("($id) ${Utils.gson.toJson(it)}")
            }

        }
    }


    suspend fun Call.await(): okhttp3.Response =
        suspendCoroutine { content ->
            enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    content.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    content.resume(response)
                }

            })
        }

    /**
     * ### Request method type of POST
     *
     * #### Function requires type of response classes
     *
     * * ByteArray -> response cast read all bydes and forward back
     * * String -> response is converted to string
     * * File -> in temporary files will be created file and fill with response data
     * * else -> String from server will be classified as json and convert into forwarded type object type
     *
     * #### Function requires body element
     *
     * * Map<*, *> -> parameters wiil be send as form body parameters
     * * String -> send as json with mime "application/json; charset=utf-8"
     * * Object -> convert into json and send with mime "application/json; charset=utf-8"
     *
     * @param url server url
     * @param body see description above
     * @param queryParts if url ends with '?' this parameters will be used as url...?key1=value1&key2=value2 otherwise  url.../key1/value1/key2/value2
     * @param headers yours request headers
     * @param auth helper for authentication see http.AuthBasic
     * @param client jou can use your custom OkHttpClient instance, or use default http.Utils.client
     * @param loggerOUT in console will be printed request
     * @param loggerIN in console will be printed response
     *
     * @see Response
     * @see AuthBasic
     * @see Auth
     * @see Utils.gson
     * @see Utils.client
     */
    @Suppress("unused")
    suspend inline fun <reified T : Any> post(
        url: String,
        body: Any,
        queryParts: SortedMap<String, String> = sortedMapOf(),
        headers: Map<String, String> = mapOf(),
        auth: Auth? = null,
        client: OkHttpClient = Utils.client,
        loggerOUT: Boolean = false,
        loggerIN: Boolean = false
    ): Response<T> = withContext(Dispatchers.IO) {

        val id = Utils.requestID.getAndIncrement()
        if (loggerOUT) Utils.loggerOutPOST(id, url, body, queryParts, headers, auth)

        val request = Request.Builder().apply {
            url(buildRequestUrl(url, queryParts))
            headers(headers)
            when (body) {
                is Map<*, *> -> {
                    val formBody = FormBody.Builder().apply {
                        body.filterKeys { it is String }
                            .filterValues { it != null }
                            .forEach {
                                add(it.key.toString(), it.value.toString())
                            }
                    }.build()
                    post(formBody)
                }
                is String -> {
                    post(body.toRequestBody(MT_JSON))
                }
                else -> {
                    post(Utils.gson.toJson(body).toRequestBody(MT_JSON))
                }
            }
            auth?.also {
                addHeader(it.key, it.value)
            }
        }.build()

        try {
            buildResponse(client.newCall(request).await(), T::class).also {
                if (loggerIN) log("($id) ${Utils.gson.toJson(it)}")
            }
        } catch (e: Throwable) {
            Response<T>(null, -1, exception =  e, message =e.message).also {
                if (loggerIN) log("($id) ${Utils.gson.toJson(it)}")
            }
        }
    }


    /**
     * headers builder
     */
    fun Request.Builder.headers(headers: Map<String, String>): Request.Builder {
        headers.filter { it.key.isNotEmpty() && it.value.isNotEmpty() }
            .forEach {
                addHeader(it.key, it.value)
            }
        return this
    }

    object Utils {

        /**
         * used for logging request, every request ginned atomic id, useful in multiple parallel requests
         */
        val requestID = AtomicInteger()


        /**
         * constant of json mimetype
         */
        val MT_JSON by lazy {
            "application/json; charset=utf-8".toMediaTypeOrNull()
        }


        /**
         * customizable Gson instance
         */
        var gson = Gson()

        /**
         * customizable OkHttpClient instance
         */
        var client = OkHttpClient.Builder()
            .connectTimeout(40L, TimeUnit.SECONDS)
            .callTimeout(30L, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()


        /**
         * url path builder
         */
        fun buildRequestUrl(url: String, queryParts: SortedMap<String, String>): String {
            return if (queryParts.isNotEmpty()) {
                if (url.endsWith("?")) {
                    url + queryParts.map {
                        "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
                    }.joinToString("&")
                } else {
                    url + queryParts.map {
                        "${it.key}/${URLEncoder.encode(it.value, "UTF-8")}"
                    }.joinToString("/")
                }
            } else url
        }


        /**
         * main request, converts server response into refilled type
         */
        @Suppress("UNCHECKED_CAST")
        suspend fun <T : Any> buildResponse(
            it: okhttp3.Response,
            clazz: KClass<T>
        ): Response<T> = withContext(Dispatchers.IO) {

            @Suppress("BlockingMethodInNonBlockingContext")
            val resp = if (it.isSuccessful) {
                when (clazz) {
                    ByteArray::class -> {
                        it.body?.use { body ->
                            Response(body.byteStream().readBytes() as T?, code = it.code,  message =it.message, headers = it.headers)
                        } ?: Response(null as T?, code = it.code, message = it.message, headers = it.headers)
                    }
                    File::class -> {

                        val outFile =  File.createTempFile("http.", ".cache")

                        if (!outFile.exists()) outFile.createNewFile()

                        buildResponseStreamConvert(it, outFile.outputStream())
                        Response(outFile as T, code = it.code, message = it.message, headers = it.headers)
                    }
                    String::class -> {
                        it.body?.use { body ->
                            Response(
                                String(body.byteStream().readBytes()) as T?,
                                it.code,
                                message = it.message,
                                headers = it.headers
                            )
                        } ?: Response(null as T?,code = it.code, message = it.message, headers = it.headers)
                    }
                    else -> {
                        it.body?.use { body ->
                            Response(
                                gson.fromJson(String(body.byteStream().readBytes()), clazz.java),
                                it.code,
                                message = it.message,
                                headers = it.headers
                            )
                        } ?: Response(null as T?,code = it.code,  message =it.message, headers = it.headers)
                    }
                }
            } else {
                Response(null as T?, code = it.code, message = it.message, headers = it.headers)
            }
            it.closeQuietly()
            resp
        }

        /**
         * write server response into output stream
         */
        private fun buildResponseStreamConvert(it: okhttp3.Response, out: OutputStream) {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var len: Int

            out.buffered(DEFAULT_BUFFER_SIZE).use { _out ->
                it.body.use {
                    it?.byteStream()?.buffered(DEFAULT_BUFFER_SIZE)?.use { _in ->
                        while (true) {
                            len = _in.read(buffer)
                            if (len <= 0) break
                            _out.write(buffer, 0, len)
                        }
                    }
                }
            }
        }

        /**
         * curl builder of request POST
         */
        fun loggerOutPOST(
            id: Int,
            url: String,
            body: Any,
            queryParts: SortedMap<String, String>,
            headers: Map<String, String>,
            auth: Auth?
        ) {

            val builder = StringBuilder("($id) curl -X POST \\")
            auth?.also {
                builder.append(" -H '${it.key}: ${it.value}'")
            }
            headers.forEach {
                builder.append(" -H '${it.key}: ${it.value}'")
            }
            if (body is String) {
                builder.append(" -d '${body.toRequestBody(MT_JSON)}'")
            } else {
                builder.append(" -d '${gson.toJson(body)}'")
            }
            builder.append(" ${buildRequestUrl(url, queryParts)}")
            log(builder.toString())

        }
        /**
         * curl builder of request GET
         */
        fun loggerOutGET(
            id: Int,
            url: String,
            queryParts: SortedMap<String, String>,
            headers: Map<String, String>,
            auth: Auth?
        ) {
            val builder = StringBuilder("($id) curl -X GET \\")
            auth?.also {
                builder.append(" -H '${it.key}: ${it.value}'")
            }
            headers.forEach {
                builder.append(" -H '${it.key}: ${it.value}'")
            }
            builder.append(" ${buildRequestUrl(url, queryParts)}")
            log(builder.toString())

        }
    }


}

