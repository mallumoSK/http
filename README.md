# http

## Library wrapper of okhttp3. Simplify work with library as much as possible, by function call and suspended functions  

![https://mallumo.jfrog.io/artifactory/gradle-dev-local/tk/mallumo/http/](https://img.shields.io/maven-metadata/v?color=%234caf50&metadataUrl=https%3A%2F%2Fmallumo.jfrog.io%2Fartifactory%2Fgradle-dev-local%2Ftk%2Fmallumo%2Fhttp%2Fmaven-metadata.xml&style=for-the-badge "Version")

```groovy
repositories {
    maven {
        url = uri("https://mallumo.jfrog.io/artifactory/gradle-dev-local")
    }
}

dependencies {
    implementation "tk.mallumo:http:$version"
}
```

### library dependency
```groovy
ext{
    gson = '2.8.5'
    okhttp = '4.8.1'
    coroutines = '1.3.9'
    junit = '4.12'
    kotlin = '1.4.0'
}
dependencies {
    api "org.jetbrains.kotlin:kotlin-stdlib"
    api "com.squareup.okhttp3:okhttp:$okhttp"
    api "com.google.code.gson:gson:$gson"
    api "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
}
```

## GET REQUEST
```kotlin
println(http.get<String>(url = "http://example.com").data)
```

## POST REQUEST
```kotlin
http.post<String>(url = "http://example.com", 
                  body = mapOf("key" to "value"))
                    .data
                    .also{
                         println(it)
                    }                           

```

### POST Function requires body parameter

* Map<*, *> -> parameters will be sent as form body parameters
* okhttp3.MultipartBody -> send without modifications
* okhttp3.FormBody -> send without modifications
* String -> send as json with mime "application/json; charset=utf-8"
* File -> send as multipart request with mime of file
* Object -> convert into json and send with mime "application/json; charset=utf-8"


### EVERY REQUEST, EXPECT RESPONSE TYPE

Response object require type of class

* ByteArray -> response read all bytes and forward back
* String -> response is converted to string
* File -> in temporary files will be created a file and fill with response data
* else -> String from server will be classified as json and convert into forwarded class type

## RESPONSE OBJECT
Library newer falls, **BUT** if happens any exception it wii forward exception inside response object

Response object definition
```kotlin
//path tk.mallumo.http.http.Response
data class Response<T>(
         val data: T?, // response data
         val code: Int,  // server response code
         val exception: Throwable? = null,  // exception (input, connection, ...)
         val message: String?,  // server status message
         val headers: Headers? = null){  // server response headers
       val isOk: Boolean get() = code == 200 && data != null   // validation quick tool
}

```
## QUERY PARAMETERS
Get and post method contains parameter 

``queryParts: SortedMap<String, String> = sortedMapOf()``

This can be used as url path builder

Look at the end of url string in each of example

##### Example 0 (rest)
```kotlin
val url = "https://example.com" // nothing on end
val queryParts = sortedMapOf(
                    "key1"  to "value1",
                    "key2"  to "value2")
// url result -> https://example.com/key1/value1/key2/value2
```

##### Example 1 (rest)
```kotlin
val url = "https://example.com/" // slash on end
val queryParts = sortedMapOf(
                    "key1"  to "value1",
                    "key2"  to "value2")
// url result -> https://example.com/key1/value1/key2/value2
```

##### Example 2 (key-value pairs)
```kotlin
val url = "https://example.com?"  // question mark on end
val queryParts = sortedMapOf(
                    "key1"  to "value1",
                    "key2"  to "value2")
// url result -> https://example.com?key1=value1&key2=value2
```
## OK HTTP CLIENT

Get and post method contains parameter 

``client: OkHttpClient = tk.mallumo.http.http.Utils.client``

Default implementation:
```kotlin
var client = OkHttpClient.Builder()
                .connectTimeout(40L, TimeUnit.SECONDS)
                .callTimeout(30L, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()
```
You can use your own in each request or redefine in ``tk.mallumo.http.http.Utils.client`` 

## HEADERS

Get and post method contains parameter 

``headers: Map<String, String> = mapOf()``

These are request headers to server

## AUTHENTICATION

Get and post method contains parameter 

``auth: http.Auth? = null``

You can use basic authentication (name+password) using class ``tk.mallumo.http.http.AuthBasic``

Or create your own by using ``tk.mallumo.http.http.Auth`` as parent class

## LOGGING

Get and post method contains parameters 

```
loggerOUT: Boolean = false
loggerIN: Boolean = false
```

**loggerOUT** -> print server request as curl command

**loggerIN** -> print server response as plane text
