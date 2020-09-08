import kotlinx.coroutines.runBlocking
import org.junit.Test
import tk.mallumo.http.http
import java.io.File

class TestGET {

    data class JsonObject(var data: Data = Data(), var ad: Ad = Ad()) {
        data class Data(
            var id: String = "",
            var email: String = "",
            var first_name: String = "",
            var last_name: String = ""
        )

        data class Ad(
            var company: String = "",
            var url: String = "",
            var text: String = "",
        )
    }

    @Test
    fun jsonRequest() {
        runBlocking {
            http.get<JsonObject>("https://reqres.in/api/users/2").also {
//                println(it.data)
                assert(it.code == 200)
                assert(!it.data?.data?.email.isNullOrEmpty())
                assert(it.exception == null)
            }
        }
    }

    @Test
    fun stringRequest() {
        runBlocking {
            http.get<String>("https://reqres.in/api/users/2").also {
                assert(it.code == 200)
                assert(!it.data.isNullOrEmpty())
                assert(it.exception == null)
            }
        }
    }

    @Test
    fun fileTempRequest() {
        runBlocking {
            http.get<File>("https://reqres.in/api/users/2").also {
                assert(it.code == 200)
                assert(it.data?.exists() == true)
                assert(it.exception == null)
                println("Temporary file: ${it.data?.absolutePath}")
            }
        }
    }

    @Test
    fun bytearrayRequest() {
        runBlocking {
            http.get<ByteArray>("https://reqres.in/api/users/2").also {
                assert(it.code == 200)
                assert(it.data?.size?:0 > 100)
                assert(it.exception == null)
            }
        }
    }
}