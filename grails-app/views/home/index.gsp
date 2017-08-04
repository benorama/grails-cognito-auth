<g:if test="${!config.developerProviderName || !config.identityPoolId}">
    <p class="warning">
        Warning: Your Cognito Developer AuthenticationSample is not properly configured.<br/>
        Please set <i>developerProviderName</i> and <i>identityPoolId</i>.
    </p>
</g:if>
<g:else>
    <fieldset>
        <legend>Congratulations!</legend>
        <p class="message">You have successfully configured the Cognito Developer Authentication Sample.</p>
    </fieldset>
    <fieldset>
        Next steps
        <ul>
            <li><g:link controller="register">Register a user</g:link> to use in the samples.</li>
            <li>Configure the <a href="https://github.com/awslabs/aws-sdk-android-samples/tree/master/CognitoSyncDemo">Android Sample</a> by following the instructions for developer authenticated identities in the ReadMe file.
            </li>
            <li>Configure the <a href="https://github.com/awslabs/aws-sdk-ios-samples/tree/master/CognitoSync-Sample/Objective-C">Objective C sample</a> by following the instructions for developer authenticated identities in the ReadMe file.
            </li>
            <li>Refer to the <a href="https://github.com/awslabs/amazon-cognito-developer-authentication-sample">ReadMe</a> for this application for any issues.
            </li>
        </ul>
    </fieldset>
</g:else>