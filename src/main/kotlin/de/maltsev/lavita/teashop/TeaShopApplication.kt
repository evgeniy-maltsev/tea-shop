package de.maltsev.lavita.teashop

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import java.util.*
import java.util.concurrent.TimeUnit
import org.springframework.http.CacheControl
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy


@SpringBootApplication
@EnableWebFlux
class TeaShopApplication {
    @Autowired
    lateinit var webSocketHandler: WebSocketHandler

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter(webSocketService())
    }

    @Bean
    fun webSocketService(): WebSocketService {
        val strategy = ReactorNettyRequestUpgradeStrategy()
        return HandshakeWebSocketService(strategy)
    }

    @Bean
    fun webSocketHandlerMapping(): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/socket"] = webSocketHandler

        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.order = -1
        handlerMapping.urlMap = map
        return handlerMapping
    }
}

@Configuration
class ApplicationRoutes {

    @Value("classpath:/static/index.html")
    private lateinit var indexHtml: Resource

    @Bean
    fun mainRouter() = router {
        GET("/") {
            ok().contentType(TEXT_HTML).syncBody(indexHtml)
        }
    }
}

@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/public", "classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
    }

}

//@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
//class WebSecurityConfiguration {
//    @Bean
//    fun userDetailsService(): MapReactiveUserDetailsService {
//        val user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("user")
//                .roles("USER")
//                .build()
//        return MapReactiveUserDetailsService(user)
//    }
//
//    @Bean
//    fun authenticationManager() = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService())
//
//    @Bean
//    fun passwordEncoder() = BCryptPasswordEncoder(11)
//
//    @Bean
//    fun securityContextRepository() = WebSessionServerSecurityContextRepository()
//
//
//    @Bean
//    fun securityWebFilterChain(tokenRepository: ServerCsrfTokenRepository, http: ServerHttpSecurity): SecurityWebFilterChain {
//        return http
////                .login`().securityContextRepository(securityContextStorage()).authenticationManager(authenticationManager())
////                .and()
////                .logout().logoutUrl("/logout")
////                .authenticationSuccessHandler { webFilterExchange, authentication ->
////
////                }
////                .authenticationFailureHandler { webFilterExchange, authenticationException ->
////
////                }
////                .and()
//                .authorizeExchange()
//                .pathMatchers("/index.html", "/", "/home", "/login").permitAll()
//                .anyExchange().authenticated()
//                .and()
//                .csrf().csrfTokenRepository(tokenRepository).and()
//                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
//                .addFilterAt(logoutWebFilter(), SecurityWebFiltersOrder.LOGOUT)
//                .build()
//    }
//
//    @Bean
//    fun authenticationWebFilter(): AuthenticationWebFilter {
//        val filter = AuthenticationWebFilter(authenticationManager())
//
//        filter.setSecurityContextRepository(securityContextRepository())
//        filter.setAuthenticationConverter(jsonBodyAuthenticationConverter())
//        filter.setAuthenticationSuccessHandler(RedirectServerAuthenticationSuccessHandler("/home"))
//        filter.setAuthenticationFailureHandler(
//                ServerAuthenticationEntryPointFailureHandler(
//                        RedirectServerAuthenticationEntryPoint("/authentication-failure")
//                )
//        )
//        filter.setRequiresAuthenticationMatcher(
//                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/signin")
//        )
//
//        return filter
//    }
//
//    fun logoutWebFilter(): LogoutWebFilter {
//        val logoutWebFilter = LogoutWebFilter()
//
//        val logoutHandler = SecurityContextServerLogoutHandler()
//        logoutHandler.setSecurityContextRepository(securityContextRepository())
//
//        val logoutSuccessHandler = RedirectServerLogoutSuccessHandler()
//        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/"))
//
//        logoutWebFilter.setLogoutHandler(logoutHandler)
//        logoutWebFilter.setLogoutSuccessHandler(logoutSuccessHandler)
//        logoutWebFilter.setRequiresLogoutMatcher(
//                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout")
//        )
//
//        return logoutWebFilter
//    }
//
//    fun jsonBodyAuthenticationConverter(): (ServerWebExchange) -> Mono<Authentication> {
//        return { exchange: ServerWebExchange ->
//            exchange
//                    .request
//                    .body
//                    .single()
//                    .ofType<LoginForm>()
//                    .map {
//                        UsernamePasswordAuthenticationToken(it.username, it.password)
//                    }
//        }
//    }
//
//}
//
//data class LoginForm(
//        val username: String,
//        val password: String
//)


@EnableReactiveMongoRepositories
class MongoConfig : AbstractReactiveMongoConfiguration() {
    @Bean
    override fun reactiveMongoClient(): MongoClient = MongoClients.create()

    override fun getDatabaseName() = "TeaShop"

}

fun main(args: Array<String>) {
    runApplication<TeaShopApplication>(*args)
}
