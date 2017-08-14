package benorama.auth

import grails.plugin.awssdk.cognito.AuthUtilities
import grails.plugin.awssdk.cognito.exception.DataAccessException


class RegisterController {

    AuthUserDBService authUserDBService

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
            boolean result = authUserDBService.register(
                    params.username,
                    params.password,
                    endpoint
            )
            if (!result) {
                forward action: 'index', params: [error: 'Duplicate registration']
                return
            }
            log.info "Registered user: username=$params.username, endpoint=$endpoint"
            forward action: 'success'
        } catch (DataAccessException e) {
            log.error("Failed to register user", e)
            forward action: 'index', params: [error: 'Failed to register user']
            return
        }
    }

    def failure() { }

    def index() { }

    def success() { }

}
