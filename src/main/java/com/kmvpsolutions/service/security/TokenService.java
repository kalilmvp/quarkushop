package com.kmvpsolutions.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.RequestScoped;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RequestScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "undefined")
    Provider<String> jwtIssuerUrlProvider;

    @ConfigProperty(name = "keycloak.credentials.client-id", defaultValue = "undefined")
    Provider<String> clientIdProvider;

    public String getAccessToken(String userName, String password, String clientSecret)
            throws IOException, InterruptedException {

        String keyCloakTokenEndpoint = this.jwtIssuerUrlProvider.get()
                .concat("/protocol/openid-connect/token");

        String requestBody = "username=".concat(userName).concat("&password=").concat(password)
                .concat("&grant_type=password").concat("&client_id=").concat(this.clientIdProvider.get());

        if (clientSecret != null) {
            requestBody = requestBody.concat("&client_secret=").concat(clientSecret);
        }

        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(keyCloakTokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        String acessToken = null;

        if (response.statusCode() == 200) {
            acessToken = new ObjectMapper().readTree(response.body()).get("access_token").textValue();
        } else {
            throw new UnauthorizedException();
        }

        return acessToken;
    }
}
