Project created to lear how to code REST API and clean code.

All documentation and possibility to call methods are available in swagger open api: https://app-social-network-java.herokuapp.com/swagger-ui/index.html

Project name: Social Network.

Used programing language: JAVA version 11.
CSV: GitHub
Used frameworks and libraries:
- Spring: Web, JPA, Security (Basic Authentication), Cache, Validation, Test,
- EhCache,
- Swagger openAPI 3.0,
- MySQL, Hibernate,
- JUnit 5, Mockito,
- Mapstruck,
- Lombok,
- Heroku,
- Docker.

Generally application allows:
- for not login users: create a new user and login to app;
- for login users with role user: delete user, update password, update profile picture, find user by keyword in username,
create new post, update post, delete post, add like to post, add dislike to post, get post by id,
get post by keyword in post body, create new comment, update comment, delete comment, add like to comment, add dislike to comment,
get comment by id, get comment by keyword in comment body;
- for login users with role admin: the same as above and additionally: get all users, find user by id, add new admin,
set enable for users, get user by post id.

Endpoint with dto is prepared mainly for users but without dto is prepared for admins.

Security for endpoint was set used spring boot security. I created WebSecurityConfig class
with contains all security settings.

Project line code test coverage is 97% with excluded mappers package because we can't test the interface.

Applications not contains frontend.
The main purpose was created a basic version of facebook app. Login users can see posts with body, name of author
and author profile picture. Post also contains information about numbers of likes, dislikes and comments for specific
post. After click for button user should be able to see comments and add a new one.

- zmienic jeszcze klasy post dto oraz wprowadzic wszystkie zmiany dla comments.