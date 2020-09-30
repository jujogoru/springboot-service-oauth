FROM openjdk:12
VOLUME /tmp
EXPOSE 9100
ADD ./target/springboot-service-oauth-0.0.1-SNAPSHOT.jar service-oauth.jar
ENTRYPOINT ["java","-jar","/service-oauth.jar"]