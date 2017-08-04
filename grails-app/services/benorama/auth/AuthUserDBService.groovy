package benorama.auth

import benorama.auth.exception.DataAccessException
import com.amazonaws.AmazonClientException

class AuthUserDBService extends AbstractDBService {

    /**
     * Authenticates the given username, password combination. Hash of password
     * is matched against the hash value stored for password field
     *
     * @param username
     *            Unique user identifier
     * @param password
     *            user password
     * @param uri
     *            endpoint URI
     * @return true if authentication was successful, false otherwise
     * @throws DataAccessException
     */
    boolean authenticate(String username, String password, String uri) throws DataAccessException {
        if (!username || !password) {
            return false
        }

        AuthUser user = load(username)
        if (!user) {
            return false
        }
        String hashedSaltedPassword = AuthUtilities.getSaltedPassword(username, appName, uri, password)
        hashedSaltedPassword == user.hashedPassword
    }

    /**
     * Authenticates the given username, signature combination. A signature is
     * generated and matched against the given signature. If they match then
     * returns true.
     *
     * @param username
     *            Unique user identifier
     * @param timestamp
     *            Timestamp of the request
     * @param signature
     *            Signature of the request
     * @return true if authentication was successful, false otherwise
     * @throws DataAccessException
     */
    boolean authenticateSignature(String username, String timestamp, String signature) throws DataAccessException {
        AuthUser user = load(username)
        if (!user) {
            return false
        }

        String computedSignature = AuthUtilities.sign(timestamp, user.hashedPassword)
        AuthUtilities.slowStringComparison(signature, computedSignature)
    }

    /**
     * Store the username, password combination in the Identity table. The
     * username will represent the item name and the item will contain a
     * attributes password and userid.
     *
     * @param username
     *            Unique user identifier
     * @param password
     *            user password
     * @param uri
     *            endpoint URI
     * @throws DataAccessException
     */
    protected AuthUser store(String username, String password, String uri) throws DataAccessException {
        if (!username || !password) {
            return
        }

        String hashedSaltedPassword = AuthUtilities.getSaltedPassword(username, appName, uri, password)
        AuthUser user = new AuthUser(
                hashedPassword: hashedSaltedPassword,
                username: username
        )
        try {
            mapper.save(user)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to store user username=$username", e)
        }
        user
    }

    /**
     * Deletes the specified username from the identity table.
     *
     * @param username
     *            Unique user identifier
     * @throws DataAccessException
     */
    void delete(String username) throws DataAccessException {
        try {
            mapper.delete(username)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to delete user username=$username", e)
        }
    }

    /**
     *
     * @param username
     * @return
     * @throws DataAccessException
     */
    AuthUser load(String username) throws DataAccessException {
        try {
            mapper.load(AuthUser, username)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to load user username=$username", e)
        }
    }

    /**
     * Attempts to register the username, password combination. Checks if
     * username not already exist. Returns true if successful, false otherwise.
     *
     * @param username
     *            Unique user identifier
     * @param password
     *            user password
     * @param uri
     *            endpoint URI
     * @return true if successful, false otherwise.
     * @throws DataAccessException
     */
    boolean register(String username,
                     String password,
                     String uri) throws DataAccessException {
        AuthUser user = load(username)
        if (user) {
            return false
        }
        store(username, password, uri)
        true
    }

}
