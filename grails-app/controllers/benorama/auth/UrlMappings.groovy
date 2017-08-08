package benorama.auth

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/login"(controller: "auth", action: "login")
        "/gettoken"(controller: "auth", action: "token")

        "/"(controller: 'home')
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
