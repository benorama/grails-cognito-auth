package benorama.auth

class HomeController {

    def index() {
       [config: grailsApplication.config.aws.cognito]
    }
}
