package benorama.auth

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import groovy.transform.Canonical

@Canonical
@DynamoDBTable(tableName="BenoramaAuthDevice")
class AuthDevice {

    @DynamoDBHashKey
    String uid

    @DynamoDBAttribute
    String key
    @DynamoDBAttribute
    String username

}
