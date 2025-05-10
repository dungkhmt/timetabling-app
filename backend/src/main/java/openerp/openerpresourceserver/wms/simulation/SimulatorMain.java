package openerp.openerpresourceserver.wms.simulation;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class TokenStorage {
    private Token token;

    public void storeToken(String accessToken, Instant expiresAt) {
        token = new Token(accessToken, expiresAt);
    }

    public Token getToken() {
        return token;
    }

    public static class Token {
        private final String accessToken;
        private final Instant expiresAt;

        public Token(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}

public class SimulatorMain {
    String clientId = "hustack";
    String clientSecret = "wDUXifuN7hjVm23uPexHFG5c0EhV1rnC";
    String realm = "OpenERP-Dev";
    String authServerUrl = "https://erp3.soict.ai/iam";

    private WebClient webClient;
    private TokenStorage tokenStorage;
    private String extractAccessToken(String responseBody) {
        return responseBody.split("\"access_token\":\"")[1].split("\"")[0];
    }

    private long extractExpiresIn(String responseBody) {
        return Long.parseLong(responseBody.split("\"expires_in\":")[1].split(",")[0]);
    }

    private String requestNewAccessToken(String clientId, String clientSecret) {
        String tokenUrl = UriComponentsBuilder
                .fromPath("/realms/" + realm + "/protocol/openid-connect/token")
                .toUriString();

        String response = this.webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=client_credentials")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String accessToken = extractAccessToken(response);
        long expiresIn = extractExpiresIn(response);

        tokenStorage.storeToken(accessToken, Instant.now().plus(expiresIn, ChronoUnit.SECONDS));

        return accessToken;
    }

    public SimulatorMain(){
        webClient = WebClient.builder()
                .baseUrl("https://analytics.soict.ai")
                //.defaultCookie("cookieKey", "cookieValue")
                //.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();
        //this.webClient = webClientBuilder.baseUrl("https://analytics.soict.ai").build();
        System.out.println("Create web client OK");
        tokenStorage = new TokenStorage();
        String accessToken = requestNewAccessToken(clientId,clientSecret);
        System.out.println("accessToken = " + accessToken);
    }
    public static void main(String[] args){
        SimulatorMain app = new SimulatorMain();
    }
}
