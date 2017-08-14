package benorama.auth

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import groovy.transform.Canonical
import grails.plugin.awssdk.cognito.AuthDevice as CognitoAuthDevice

@Canonical
@DynamoDBTable(tableName="GrailsCognitoAuthDevice")
class AuthDevice implements CognitoAuthDevice {

    @DynamoDBHashKey
    String uid

    @DynamoDBAttribute
    String key
    @DynamoDBAttribute
    String username

}
