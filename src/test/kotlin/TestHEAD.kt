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
            http.head("https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Eichh%C3%B6rnchen_D%C3%BCsseldorf_Hofgarten_edit.jpg/250px-Eichh%C3%B6rnchen_D%C3%BCsseldorf_Hofgarten_edit.jpg")
                .also {
                    println(http.Utils.gson.toJson(it))
                    assert(it.code == 200)
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

    @Test
    fun registerSuccessRequest2() {
        runBlocking {
            http.head("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Eastern_Gray_Squirrel_800.jpg/220px-Eastern_Gray_Squirrel_800.jpg")
                .also {
                    println(http.Utils.gson.toJson(it))
                    println(it.headers?.get("Content-Type"))
                    assert(it.code == 200)
                }
        }
    }
}