package com.serwisspolecznosciowy.Application;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Social Network",
        version = "v1.0",
        description = "The application allows mainly to create new users and admins, see and add new posts and comments by login users or admins.\n" +
                "URLs with contains 'dto' are intended for users but without for admins.\n" +
                "Admin rights are reserved only for the owner of the application.\n" +
                "To achieve admin right please contact me.",
        contact = @Contact(name = "Szymon Kochanowski", url = "", email = "szymon.piotr.kochanowski@gmail.com"),
        license = @License(name = "Shareware", url = "https://zpe.gov.pl/a/rodzaje-licencji-na-oprogramowanie/D1D76JU8i")))
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
