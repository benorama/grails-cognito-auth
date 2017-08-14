package benorama.auth

import grails.plugin.awssdk.cognito.AbstractCognitoAuthService

class AuthService extends AbstractCognitoAuthService {

    AuthDeviceDBService authDeviceDBService
    AuthUserDBService authUserDBService

    @Override
    protected boolean registerDevice(String uid, String encryptionKey, String username) {
        authDeviceDBService.register(uid, encryptionKey, username)
    }

    @Override
    protected grails.plugin.awssdk.cognito.AuthDevice loadDevice(String uid) {
        return authDeviceDBService.load(uid)
    }

    @Override
    protected grails.plugin.awssdk.cognito.AuthUser loadUser(String username) {
        return authUserDBService.load(username)
    }

    def getCognitoConfig() {
        grailsApplication.config.grails.plugin.awssdk.cognito
    }
}
