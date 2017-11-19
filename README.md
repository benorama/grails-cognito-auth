## Introduction

Grails version of [AWS Cognito Sample Developer Authentication Sample](https://github.com/awslabs/amazon-cognito-developer-authentication-sample)

This is an implementation of a [developer authenticated identities](https://docs.aws.amazon.com/cognito/latest/developerguide/developer-authenticated-identities.html).

It uses the [Developer Authenticated Identities Authflow](https://docs.aws.amazon.com/cognito/latest/developerguide/authentication-flow.html)

![Developer Authenticated Identities Authflow](https://docs.aws.amazon.com/cognito/latest/developerguide/images/amazon-cognito-dev-auth-enhanced-flow.png)

To use the app locally:
- create an `application-development.yml` in root directory

```yml
grails:
    plugin:
        awssdk:
            accessKey: {YOUR_ACCESS_KEY}
            secretKey: {YOUR_SECRET_KEY}
            region: eu-west-1
            cognito:
                developerProviderName: benorama.auth
                identityPoolId: eu-west-1:{YOUR_IDENTITY_POLL}
                salt: {SOME_SALT}
                sessionDuration: 900
```

- create the tables `GrailsCognitoAuthUser` and `GrailsCognitoAuthDevice` in DynamoDB if required
- run the app
- register a user

Note: to get more info about [Cognito User Pool vs Federated Identities](https://serverless-stack.com/chapters/cognito-user-pool-vs-identity-pool.html) and [OpenID](https://connect2id.com/learn/openid-connect)

## LOGIN API

- try to login by sending a POST to `/login` with the following params:
  - `username` (e.g. *foo*)
  - `uid`, current unique device ID
  - `timestamp`, current request timestamp with ISO format (e.g. *2017-11-16T16:15:17.406Z*)
  - `signature`, to verify the request.
  
Signature signed with hashed password and can be generated server side like this:

```
import grails.plugin.awssdk.cognito.*

timestamp = '2017-11-16T16:15:17.406Z'
hashedPassword = 'd152b7ab632c1d37f182dddfbbf86f9907937ffe04b86383a5f67b5ed3c34df5'

computedSignature = AuthUtilities.sign(timestamp, hashedPassword)
```  
  
This will return an encrypted generated device key for the current user and device.

You can decrypt the response like this:

```groovy
AESEncryption.unwrap(result, hashedPassword.substring(0, 32))
```

Response example:

```json
{
  "key": "6057d8f638e102c13f4d329bf21d9b38"
}
```

## TOKEN API

You can then get an OpenID JWT token by sending a post to `/gettoken` with the following params:
  - `uid`, current unique device ID
  - `timestamp`, current request timestamp with ISO format (e.g. *2017-11-16T16:15:17.406Z*)
  - `provider1`, provider name (e.g. *benorama.auth* or *graph.facebook.com*)
  - `token1`, provider token (e.g. `username` for custom provider or token for others) 
  - `signature`, to verify the request.
    
Signature is signed with device key and can be generated server side like this:

```
import grails.plugin.awssdk.cognito.*

providerName = 'benorama.auth'
timestamp = '2017-11-16T16:15:17.406Z'
deviceKey = '6057d8f638e102c13f4d329bf21d9b38' // Generated when registering the device and returned by /login
stringToSign = "${timestamp}${providerName}${username}"

computedSignature = AuthUtilities.sign(stringToSign, deviceKey)
```

This will return the encrypted JWT-based OpenId token for the current user and device.

You can decrypt the response like this:

```groovy
AESEncryption.unwrap(result, deviceKey.substring(0, 32))
```

Response example:

```json
{
  "identityPoolId": "eu-west-1:f9795799-5721-4e3c-90f3-82ade0ecba90",
  "identityId": "eu-west-1:9b0be988-94f5-416e-be83-400901bc993e",
  "token": "eyJraWQiOiJldS13ZXN0LTExIiwidHlwIjoiSldTIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJldS13ZXN0LTE6OWIwYmU5ODgtOTRmNS00MTZlLWJlODMtNDAwOTAxYmM2NjJmIiwiYXVkIjoiZXUtd2VzdC0xOmY5Nzk1Nzk5LTU3MjEtNGUzYy05MGYzLTgyYWRlMGVjYmE5MCIsImFtciI6WyJhdXRoZW50aWNhdGVkIiwiYmVub3JhbWEuYXV0aCIsImJlbm9yYW1hLmF1dGg6ZXUtd2VzdC0xOmY5Nzk1Nzk5LTU3MjEtNGUzYy05MGYzLTgyYWRlMGVjYmE5MDpiZW4iXSwiaXNzIjoiaHR0cHM6Ly9jb2duaXRvLWlkZW50aXR5LmFtYXpvbmF3cy5jb20iLCJleHAiOjE1MTA5MzQxNzAsImlhdCI6MTUxMDkzMzI3MH0.iDYmDG7RC-61HmV_6SxEoufYHtEScYL7CB6Rymk26Xx8xIpV5CdFduj4wuOoB_vshYfUWfs6YgwVkDk8V1vZ-jTAoYu-9MRhanboPJF3ftbo3obqfHfhQUrSvDLgjBViLU2jYOc1aV2XxXdt7sPwf8CVFa5eAeTJRsoY37d0OBUTRI6enPyh0VydIcxWrGGDsc8rUqm2medyINSp3_hwdrf-eFxSxPXpGu_ExB5_tvvjovoMZx9oXGG9PfjTbZJiYPYAIP58-AaNoxxsAf6WAKNSNxHnhgDQiefF9OgV0qecemNxe5E_A80h0dJK5oIJuFbLLlR3JVsKyWrlyodUwg"
}
```

Decoded JWT Token payload:

```json
{
  "sub": "eu-west-1:9b0be988-94f5-416e-be83-400901bc993e",
  "aud": "eu-west-1:f9795799-5721-4e3c-90f3-82ade0ecba90",
  "amr": [
    "authenticated",
    "benorama.auth",
    "benorama.auth:eu-west-1:f9795799-5721-4e3c-90f3-82ade0ecba90:ben"
  ],
  "iss": "https://cognito-identity.amazonaws.com",
  "exp": 1510934170,
  "iat": 1510933270
}
```

You can then use the ID/session token to configure user AWS credentials.

Javascript example:

```javascript
AWS.config.credentials = new AWS.CognitoIdentityCredentials({
   IdentityPoolId: 'eu-west-1:{YOUR_IDENTITY_POLL}',
   IdentityId: 'eu-west-1:9b0be988-94f5-416e-be83-400901bc993e',
   Logins: {
      'cognito-identity.amazonaws.com': '{TOKEN_RETURNED_FROM_YOUR_PROVIDER}'
   }
});
```

Or you can use the ID/session token to make call to your APIs.
