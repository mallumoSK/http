import kotlinx.coroutines.runBlocking
import org.junit.Test
import tk.mallumo.http.http

class TestPost {

    data class RegisterRequest(val email: String, var password: String)
    data class RegisterResponse(var id: Int = -1, var token: String = "")

    @Test
    fun registerUnsuccessRequest() {
        runBlocking {
            http.post<RegisterResponse>("https://reqres.in/api/register",
                    body = RegisterRequest("eve.holt@reqres.in", ""),
                    loggerIN = true,
                    loggerOUT = true).also {
                assert(it.data == null)
                assert(it.code == 400)
            }
        }
    }

    @Test
    fun registerSuccessRequest() {
        runBlocking {
            http.post<RegisterResponse>("https://reqres.in/api/register",
                    body = RegisterRequest("eve.holt@reqres.in", "pistol")).also {
                assert(it.data?.id ?: 0 > 0)
                assert(it.code == 200)
            }
        }
    }
}