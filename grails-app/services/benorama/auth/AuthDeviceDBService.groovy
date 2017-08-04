package benorama.auth

import benorama.auth.exception.DataAccessException
import com.amazonaws.AmazonClientException

class AuthDeviceDBService extends AbstractDBService {

    /**
     * Authenticates the given UID, Key combination. If the password in the item
     * identified by the item name 'UID' matches the Key given then true is
     * returned, false otherwise.
     *
     * @param uid
     *            Unique device identifier
     * @param key
     *            encryption key associated with UID
     * @return true if authentication was successful, false otherwise
     * @throws DataAccessException
     */
    boolean authenticate(String uid, String key) throws DataAccessException {
        AuthDevice device = load(uid)
        device && key == device.key
    }

    /**
     * Store the UID, Key, username combination in the Identity table. The UID
     * will represent the item name and the item will contain attributes key and
     * username.
     *
     * @param uid
     *            Unique device identifier
     * @param key
     *            encryption key associated with UID
     * @param username
     *            Unique user identifier
     * @throws DataAccessException
     */
    AuthDevice create(String uid, String key, String username) throws DataAccessException {
        if (!username) {
            return
        }

        AuthDevice device = new AuthDevice(
                key: key,
                uid: uid,
                username: username
        )
        try {
            mapper.save(device)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to store device uid=$uid and username=$username", e)
        }
        device
    }

    /**
     * Deletes the specified UID from the identity table.
     *
     * @param uid
     *            Unique device identifier
     * @throws DataAccessException
     */
    void delete(String uid) throws DataAccessException {
        try {
            mapper.delete(uid)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to delete device uid=$uid", e)
        }
    }

    /**
     * Returns device info for given device ID (UID)
     *
     * @param uid
     *            Unique device identifier
     * @return device info for the given uid
     * @throws DataAccessException
     */
    AuthDevice load(String uid) throws DataAccessException {
        try {
            mapper.load(AuthDevice, uid)
        } catch (AmazonClientException e) {
            throw new DataAccessException("Failed to load device uid=$uid", e)
        }
    }

    /**
     * Attempts to register the UID, Key and username combination. Returns true
     * if successful, false otherwise.
     *
     * @param uid
     *            Unique device identifier
     * @param key
     *            encryption key associated with UID
     * @param username
     *            Unique user identifier
     * @return true if device registration was successful, false otherwise
     * @throws DataAccessException
     */
    boolean register(String uid, String key, String username) throws DataAccessException {
        AuthDevice device = load(uid)
        if (device && device.username != username) {
            return false
        }
        create(uid, key, username)
        true
    }
}
