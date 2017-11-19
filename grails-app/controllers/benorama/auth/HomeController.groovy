package benorama.auth

class HomeController {

    def index() {
       [config: grailsApplication.config.grails.plugin.awssdk.cognito]
    }
}
