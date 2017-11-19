package benorama.auth

import agorapulse.libs.awssdk.util.AwsClientUtil
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import grails.core.GrailsApplication
import org.springframework.beans.factory.InitializingBean

abstract class AbstractDBService implements InitializingBean {

    static SERVICE_NAME = AmazonDynamoDB.ENDPOINT_PREFIX

    GrailsApplication grailsApplication
    AmazonDynamoDBClient client
    DynamoDBMapper mapper

    void afterPropertiesSet() throws Exception {
        // Set region
        Region region = AwsClientUtil.buildRegion(config, serviceConfig)
        assert region?.isServiceSupported(SERVICE_NAME)

        // Create client
        def credentials = AwsClientUtil.buildCredentials(config, serviceConfig)
        ClientConfiguration configuration = AwsClientUtil.buildClientConfiguration(config, serviceConfig)
        client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentials)
                .withClientConfiguration(configuration)
                .build()
        mapper = new DynamoDBMapper(client)
    }

    /**
     *
     * @param classToCreate
     * @param readCapacityUnits
     * @param writeCapacityUnits
     */
    void createTable(Class classToCreate,
                     Long readCapacityUnits = 10,
                     Long writeCapacityUnits = 5) {
        DynamoDBTable table = classToCreate.getAnnotation(DynamoDBTable.class)

        try {
            // Check if the table exists
            client.describeTable(table.tableName())
        } catch (ResourceNotFoundException e) {
            CreateTableRequest createTableRequest = mapper.generateCreateTableRequest(classToCreate) // new CreateTableRequest().withTableName(table.tableName())

            // ProvisionedThroughput
            ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
                    .withReadCapacityUnits(readCapacityUnits)
                    .withWriteCapacityUnits(writeCapacityUnits)
            createTableRequest.setProvisionedThroughput(provisionedThroughput)

            log.info "Creating DynamoDB table: ${createTableRequest}"

            client.createTable(createTableRequest)
        }
    }

    // PRIVATE

    def getConfig() {
        grailsApplication.config.grails?.plugin?.awssdk ?: grailsApplication.config.grails?.plugins?.awssdk
    }

    def getServiceConfig() {
        config[SERVICE_NAME]
    }

}