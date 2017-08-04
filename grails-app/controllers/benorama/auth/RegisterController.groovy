package benorama.auth

import benorama.auth.exception.DataAccessException

class RegisterController {

    AuthService authService

    def create() {
        if (!params.username || !params.password) {
            forward action: 'index', params: [error: 'Required parameters: username and password']
            return
        }
        if (!AuthUtilities.isValidUsername(params.username) || !AuthUtilities.isValidPassword(params.password)) {
            forward action: 'index', params: [error: 'Invalid parameters: username or password']
            return
        }

        String endpoint = AuthUtilities.getEndPoint(request)
        log.info "Registering user: username=$params.username, endpoint=$endpoint"
        try {
            boolean result = authService.registerUser(
                    params.username,
                    params.password,
                    endpoint
            )
            if (!result) {
                forward action: 'index', params: [error: 'Duplicate registration']
                return
            }
        } catch (DataAccessException e) {
            forward action: 'index', params: [error: 'Failed to register user']
            return
        }
        forward action: 'success'
    }

    def failure() { }

    def index() { }

    def success() { }

}