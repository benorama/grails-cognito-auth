<html>
<head>
    <title>Amazon Cognito Developer Authentication Sample - Welcome</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0">
    <asset:stylesheet src="styles.css"/>
</head>

<body class="success">

    <div id="header">
        <h1>Amazon Cognito Developer Authentication Sample</h1>
    </div>

    <div id="body">
        <g:layoutBody/>
    </div>

    <g:if test="${!request.isSecure()}">
        <p class="warning">
            Warning: You are not running SSL.
        </p>
    </g:if>

    <div id="footer">
        <p class="footnote">
            <g:meta name="info.app.name"/>
            v<g:meta name="info.app.version"/>
            - AWSCognitoDeveloperAuthenticationSample
        </p>
    </div>

</body>
</html>