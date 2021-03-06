package benorama.auth

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import groovy.transform.Canonical
import grails.plugin.awssdk.cognito.AuthUser as CognitoAuthUser

@Canonical
@DynamoDBTable(tableName="GrailsCognitoAuthUser")
class AuthUser implements CognitoAuthUser {

    @DynamoDBHashKey
    String username

    @DynamoDBAttribute
    Boolean enabled = true
    @DynamoDBAttribute
    String hashedPassword

}
