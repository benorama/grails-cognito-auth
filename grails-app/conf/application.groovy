aws {
    cognito {
        developerProviderName = System.getProperty("AWS_COGNITO_DEVELOPER_PROVIDER_NAME")
        identityPoolId = System.getProperty("AWS_COGNITO_IDENTITY_POOL_ID")
        salt = System.getProperty("AWS_COGNITO_SALT")
    }
}

grails.plugin.console.enabled = System.getProperty("GRAILS_PLUGIN_CONSOLE_ENABLED")
