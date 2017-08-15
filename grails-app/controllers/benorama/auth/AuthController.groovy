package benorama.auth

class AuthController {

    AuthService authService

    def login() {
        String resp = authService.doLogin(request, response, params)
        if (resp) {
            render resp
        }
    }

    def token() {
        String resp = authService.doToken(request, response, params)
        if (resp) {
            render resp
        }
    }

}
