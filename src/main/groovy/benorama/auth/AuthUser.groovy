package benorama.auth

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import groovy.transform.Canonical

@Canonical
@DynamoDBTable(tableName="BenoramaAuthUser")
class AuthUser {

    @DynamoDBHashKey
    String username

    @DynamoDBAttribute
    Boolean enabled = true
    @DynamoDBAttribute
    String hashedPassword

}
