package benorama.auth

import benorama.auth.exception.DataAccessException
import benorama.auth.exception.UnauthorizedException

class AuthController {

    AuthService authService

    def login() {
        if (!params.username || !params.timestamp || !params.signature || !params.uid) {
            response.sendError(400, 'Missing required parameters')
            return
        }

        String endpoint = AuthUtilities.getEndPoint(request)
        String signature = params.signature
        String timestamp = params.timestamp
        String username = params.username
        String uid = params.uid
        log.debug "Login with username=$username, timestamp=$timestamp, uid=$uid, endpoint=$endpoint"

        try {
            authService.validateLoginRequest(username, uid, signature, timestamp)
            render authService.getKey(username, uid)
        } catch (DataAccessException e) {
            log.error "Failed to access data", e
            response.sendError(500, 'Failed to access data')
        } catch (UnauthorizedException e) {
            log.warn "Unauthorized access due to: $e.message", e
            response.sendError(401, 'Unauthorized access')
        }
    }

    def token() {
        if (!params.timestamp || !params.signature || !params.uid) {
            response.sendError(400, 'Missing required parameters')
            return
        }

        Map logins = [:]
        String signature = params.signature
        String timestamp = params.timestamp
        String identityId = params.identityId // Not required
        String uid = params.uid

        // build the string to sign
        StringBuilder stringToSign = new StringBuilder()
        stringToSign << timestamp
        // process any login tokens passed in
        boolean foundLogin = true
        int loginNum = 1
        while (foundLogin) {
            String provider = params["provider$loginNum"]
            String token = params["token$loginNum"]

            foundLogin = provider && token
            if (foundLogin) {
                log.debug "Adding token from provide=$provider"
                logins[provider] = token
                stringToSign << provider
                stringToSign << token
                loginNum++
            }
        }

        if (identityId){
            stringToSign << identityId
        }
        log.debug "Get token with uid=$uid and timestamp=$timestamp"

        try {
            authService.validateTokenRequest(uid, signature, timestamp, stringToSign.toString())
            render authService.getToken(uid, logins, identityId)
        } catch (DataAccessException e) {
            log.error "Failed to access data", e
            response.sendError(500, 'Failed to access data')
        } catch (UnauthorizedException e) {
            log.warn "Unauthorized access due to: $e.message"
            response.sendError(401, 'Unauthorized access')
        } catch (Exception e) {
            log.error "Failed to access data", e
            response.sendError(500, 'An error occurred')
        }
    }

}
