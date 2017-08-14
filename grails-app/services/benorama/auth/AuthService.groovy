package benorama.auth

import benorama.auth.exception.DataAccessException
import benorama.auth.exception.UnauthorizedException
import com.amazonaws.regions.RegionUtils
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityRequest
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityResult
import grails.core.GrailsApplication
import grails.util.Metadata

import javax.annotation.PostConstruct

class AuthService {

    AmazonCognitoIdentityClient client
    AuthDeviceDBService authDeviceDBService
    AuthUserDBService authUserDBService
    GrailsApplication grailsApplication

    @PostConstruct
    init() {
        client = new AmazonCognitoIdentityClient()
        client.setRegion(RegionUtils.getRegion(cognitoConfig.region ?: 'eu-west-1'))
    }

    /**
     * Generate key for device UID. The key is encrypted by hash of salted
     * password of the user. Encrypted key is then wrapped in JSON object before
     * returning it. This function is useful in Identity mode
     *
     * @param username
     *            Unique user identifier
     * @param uid
     *            Unique device identifier
     * @return encrypted key as JSON object
     * @throws DataAccessException
     * @throws UnauthorizedException
     */
    public String getKey(String username, String uid) throws DataAccessException, UnauthorizedException {
        AuthDevice device = authDeviceDBService.load(uid)
        if (!device) {
            throw new UnauthorizedException("Couldn't find device: $uid")
        }

        AuthUser user = authUserDBService.load(username)
        if (!user) {
            throw new UnauthorizedException("Couldn't find user: $username")
        }

        log.debug "Responding with encrypted key for UID : $uid"
        AuthUtilities.prepareJsonResponseForKey(device.key, user.hashedPassword)
    }

    /**
     * Generate tokens for given UID. The tokens are encrypted using the key
     * corresponding to UID. Encrypted tokens are then wrapped in JSON object
     * before returning it. Useful in Anonymous and Identity modes
     *
     * @param uid
     *            Unique device identifier
     * @return encrypted tokens as JSON object
     * @throws Exception
     */
     String getToken(String uid,
                     Map<String,String> logins,
                     String identityId) {

        AuthDevice device = authDeviceDBService.load(uid)
        if (!device) {
            throw new UnauthorizedException("Couldn't find device: $uid")
        }

        AuthUser user = authUserDBService.load(device.username)
        if (!user) {
            throw new UnauthorizedException("Couldn't find user: $device.username")
        }

        if (user && user.username != logins[cognitoConfig.developerProviderName]) {
            throw new UnauthorizedException("User mismatch for device and logins map")
        }

        log.debug "Creating temporary credentials"
        GetOpenIdTokenForDeveloperIdentityResult result = getOpenIdTokenFromCognito(
                user.username,
                logins,
                identityId
        )

        log.debug "Generating session tokens for UID : $uid"
        AuthUtilities.prepareJsonResponseForTokens(result, device.key, cognitoConfig.identityPoolId)
    }

    /**
     * Allows users to register.
     *
     * @param username
     *            Unique alphanumeric string of length between 3 to 128
     *            characters with special characters limited to underscore (_),
     *            period (.) and (@).
     * @param password
     *            String of length between 6 to 128 characters
     * @param endpoint
     *            DNS name of host machine
     * @return boolean indicating if the registration was successful or not
     * @throws DataAccessException
     */
    boolean registerUser(String username,
                         String password,
                         String endpoint) throws DataAccessException {
        authUserDBService.register(username, password, endpoint)
    }

    /**
     * Verify if the login request is valid. Username and UID are authenticated.
     * The timestamp is checked to see it falls within the valid timestamp
     * window. The signature is computed and matched against the given
     * signature. Also its checked to see if the UID belongs to the username.
     * This function is useful in Identity mode
     *
     * @param username
     *            Unique user identifier
     * @param uid
     *            Unique device identifier
     * @param signature
     *            Base64 encoded HMAC-SHA256 signature derived from hash of
     *            salted-password and timestamp
     * @param timestamp
     *            Timestamp of the request in ISO8601 format
     * @return status code indicating if login request is valid or not
     * @throws DataAccessException
     * @throws UnauthorizedException
     */
    void validateLoginRequest(String username, String uid, String signature, String timestamp)
            throws DataAccessException, UnauthorizedException {
        if (!AuthUtilities.isTimestampValid(timestamp)) {
            throw new UnauthorizedException("Invalid timestamp: $timestamp")
        }

        // Validate signature
        log.debug "Validate signature: $signature"
        AuthUser user = authUserDBService.load(username)
        if (!user) {
            throw new UnauthorizedException("Couldn't find user: $username")
        }

        if (!validateSignature(timestamp, user.hashedPassword, signature)) {
            throw new UnauthorizedException("Invalid signature: $signature")
        }

        // Register device
        AuthDevice device = regenerateKey(uid, user.username)

        if (user.username != device.username) {
            throw new UnauthorizedException("User [$user.username] doesn't match the device's owner [$device.username]")
        }
    }

    /**
     * Verify if the given signature is valid.
     *
     * @param stringToSign
     *            The string to sign
     * @param key
     *            The key used in the signature process
     * @param signature
     *            Base64 encoded HMAC-SHA256 signature derived from key and
     *            string
     * @return true if computed signature matches with the given signature,
     *         false otherwise
     */
    boolean validateSignature(String stringToSign,
                              String key,
                              String targetSignature) {
        boolean valid = false
        try {
            String computedSignature = AuthUtilities.sign(stringToSign, key)
            valid = AuthUtilities.slowStringComparison(targetSignature, computedSignature)
        } catch (Exception e) {
            log.error "Exception during sign", e
        }
        valid
    }

    /**
     * Verify if the token request is valid. UID is authenticated. The timestamp
     * is checked to see it falls within the valid timestamp window. The
     * signature is computed and matched against the given signature. Useful in
     * Anonymous and Identity modes
     *
     * @param uid
     *            Unique device identifier
     * @param signature
     *            Base64 encoded HMAC-SHA256 signature derived from key and
     *            timestamp
     * @param timestamp
     *            Timestamp of the request in ISO8601 format
     * @throws DataAccessException
     * @throws UnauthorizedException
     */
     void validateTokenRequest(String uid,
                               String signature,
                               String timestamp,
                               String stringToSign) throws DataAccessException, UnauthorizedException {
        if (!AuthUtilities.isTimestampValid(timestamp)) {
            throw new UnauthorizedException("Invalid timestamp: $timestamp")
        }

        AuthDevice device = authDeviceDBService.load(uid)
        if (!device) {
            throw new UnauthorizedException("Couldn't find device: $uid")
        }

        if (!validateSignature(stringToSign, device.key, signature)) {
            log.debug "String to sign: $stringToSign"
            throw new UnauthorizedException("Invalid signature: $signature")
        }
    }

    // PRIVATE

    private getCognitoConfig() {
        grailsApplication.config.aws.cognito
    }

    private GetOpenIdTokenForDeveloperIdentityResult getOpenIdTokenFromCognito(String username,
                                                                               Map<String,String> logins,
                                                                               String identityId) {
        if (!cognitoConfig.identityPoolId || !username) {
            return null
        }
        try {
            GetOpenIdTokenForDeveloperIdentityRequest tokenGetRequest = new GetOpenIdTokenForDeveloperIdentityRequest()
                .withIdentityPoolId(cognitoConfig.identityPoolId)
                .withTokenDuration((cognitoConfig.sessionDuration ?: '900')?.toLong())
                .withLogins(logins)
            if (identityId){
                tokenGetRequest.identityId = identityId
            }
            log.debug "Requesting identity Id: $identityId"
            GetOpenIdTokenForDeveloperIdentityResult  result = client.getOpenIdTokenForDeveloperIdentity(tokenGetRequest)
            log.debug "Response identity Id: $result.identityId"
            return result
        } catch (Exception exception) {
            log.error "Exception during getTemporaryCredentials", exception
            throw exception
        }
    }

    /**
     * This method regenerates the key each time. It lookups up device details
     * of a registered device. Also registers device if it is not already
     * registered.
     *
     * @param uid
     *            Unique device identifier
     * @param username
     *            Userid of the current user
     * @return device info i.e. key and userid
     * @throws DataAccessException
     */
    private AuthDevice regenerateKey(String uid, String username)
            throws DataAccessException {
        AuthDevice deviceInfo
        log.debug "Generating encryption key"
        String encryptionKey = AuthUtilities.generateRandomString()

        if (authDeviceDBService.register(uid, encryptionKey, username)) {
            deviceInfo = authDeviceDBService.load(uid)
        }
        deviceInfo
    }

    String getSaltedPassword(String password, String username, String endpoint) {
        String salt = cognitoConfig.salt ?: (username + appName + endpoint.toLowerCase())
        return AuthUtilities.sign(salt, password);
    }


    protected static String getAppName() {
        Metadata.current.'app.name'
    }
}
