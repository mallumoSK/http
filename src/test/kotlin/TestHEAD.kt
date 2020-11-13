import kotlinx.coroutines.runBlocking
import org.junit.Test
import tk.mallumo.http.http

class TestHEAD {

    data class RegisterRequest(val email: String, var password: String)
    data class RegisterResponse(var id: Int = -1, var token: String = "")

//    @Test
//    fun registerUnsuccessRequest() {
//        runBlocking {
//            http.post<RegisterResponse>("https://reqres.in/api/register",
//                    body = RegisterRequest("eve.holt@reqres.in", ""),
//                    loggerIN = true,
//                    loggerOUT = true).also {
//                assert(it.data == null)
//                assert(it.code == 400)
//            }
//        }
//    }

    @Test
    fun registerSuccessRequest() {
        runBlocking {
            http.head("http://192.168.100.111/download/%5bEMBER%5d%20King's%20Raid%20-%20Ishi%20o%20Tsugu%20Mono-tachi%20-%2004.mkv")
                .also {
                    println(http.Utils.gson.toJson(it))
                    assert(it.code == 200)
                }

            http.head("http://168.100.111/download/%5bEMBER%5d%20King's%20Raid%20-%20Ishi%20o%20Tsugu%20Mono-tachi%20-%2004.mkv")
                .also {
                    println(http.Utils.gson.toJson(it))
                    assert(it.code != 200)
                }
        }
    }

    @Test
    fun test() {
        runBlocking {
            val data = "kaso.sk"
            val urlsToCheck = arrayListOf<String>()
            urlsToCheck.add("https://$data")
            urlsToCheck.add("http://$data")
            urlsToCheck.add("https://www.$data")
            urlsToCheck.add("http://www.$data")
            urlsToCheck.forEach {
                val resp = http.head(it)
                println("$it : ${resp.isOk}-> ${http.Utils.gson.toJson(resp)}")
            }
        }
    }
}