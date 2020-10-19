package cz.jr.stravadownloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Created by Jiří Rýdel on 4/14/20, 9:58 AM
 */
public class StravaDownloader {

    private static final Logger LOG = LogManager.getLogger(StravaDownloader.class);

    private static final String STRAVA_URL = "https://www.strava.com/";
    private static final String STRAVA_URL_SESSION = STRAVA_URL + "session";
    private static final String STRAVA_URL_LOGIN = STRAVA_URL + "login";

    private static final String LOGIN_EMAIL = System.getenv("STRAVA_LOGIN");
    private static final String LOGIN_PASSWORD = System.getenv("STRAVA_PASSWORD");

    private final HttpClient httpClient;

    private String utf8;
    private String authenticityToken;

    public StravaDownloader() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieManager)
                .build();
    }

    public void logIn() throws IOException, InterruptedException {
        LOG.info("LOGIN");
        fetchToken();
        HttpRequest sessionRequest = createLoginRequest(utf8, authenticityToken);
        HttpResponse<String> sessionResponse = httpClient.send(sessionRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (!sessionResponse.body().contains("logged-in")) {
            throw new IOException("Login failed. not redirected to STRAVA dashboard.");
        }
    }

    public void logOut() throws IOException, InterruptedException {
        LOG.info("LOGOUT");
        if (authenticityToken == null) {
            throw new IOException("Could not logout, no login before.");
        }
        HttpRequest sessionRequest = createLogout(authenticityToken);
        HttpResponse<String> sessionResponse = httpClient.send(sessionRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (!URI.create(STRAVA_URL).equals(sessionResponse.uri())) {
            throw new IOException("Logout failed. not redirected to STRAVA default page.");
        }
    }

    public String getString(String url) throws IOException, InterruptedException {
        LOG.info("DOWNLOAD: " + url);
        HttpRequest request = createBasicRequst(url);
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return httpResponse.body();
    }

    public Document get(String url) throws IOException, InterruptedException {
        LOG.info("DOWNLOAD: " + url);
        HttpRequest request = createBasicRequst(url);
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return Jsoup.parse(httpResponse.body());
    }

    private void fetchToken() throws IOException, InterruptedException {
        HttpRequest loginRequest = createBasicRequst(STRAVA_URL_LOGIN);
        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Document loginPageDocument = Jsoup.parse(loginResponse.body());
        Element loginForm = loginPageDocument.select("form#login_form").first();
        utf8 = loginForm.select("input[name=utf8]").attr("value");
        authenticityToken = loginForm.select("input[name=authenticity_token]").attr("value");
    }

    private HttpRequest createBasicRequst(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.setHeader("referer", "https://www.strava.com/");
        builder.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        builder.setHeader("accept-language", "cs-CZ,cs;q=0.9,en;q=0.8");
//        builder.setHeader("accept-encoding", "gzip, deflate, br");
        builder.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
        return builder.uri(URI.create(url)).GET().build();
    }

    private HttpRequest createLoginRequest(String utf8, String authenticity_token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.setHeader("referer", "https://www.strava.com/login?cta=log-in&element=global-header&source=registers_show");
        builder.setHeader("origin", "https://www.strava.com/");
        builder.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        builder.setHeader("accept-language", "cs-CZ,cs;q=0.9,en;q=0.8");
        builder.setHeader("content-type", "application/x-www-form-urlencoded");
//        builder.setHeader("accept-encoding", "gzip, deflate, br");
        builder.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
        builder.uri(URI.create(STRAVA_URL_SESSION));

        String content = "utf8=" + URLEncoder.encode(utf8, StandardCharsets.UTF_8) + "&" +
                "authenticity_token=" + URLEncoder.encode(authenticity_token, StandardCharsets.UTF_8) + "&" +
                "plan=" + "&" +
                "email=" + URLEncoder.encode(LOGIN_EMAIL, StandardCharsets.UTF_8) + "&" +
                "password=" + URLEncoder.encode(LOGIN_PASSWORD, StandardCharsets.UTF_8);
        builder.POST(HttpRequest.BodyPublishers.ofString(content, StandardCharsets.UTF_8));
        return builder.build();
    }

    private HttpRequest createLogout(String authenticity_token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.setHeader("referer", "https://www.strava.com/dashboard");
        builder.setHeader("origin", "https://www.strava.com/");
        builder.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        builder.setHeader("accept-language", "cs-CZ,cs;q=0.9,en;q=0.8");
        builder.setHeader("content-type", "application/x-www-form-urlencoded");
//        builder.setHeader("accept-encoding", "gzip, deflate, br");
        builder.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36");
        builder.uri(URI.create(STRAVA_URL_SESSION));

        String content = "_method=" + URLEncoder.encode("delete", StandardCharsets.UTF_8) + "&" + "authenticity_token=" + URLEncoder.encode(authenticity_token, StandardCharsets.UTF_8);
        builder.POST(HttpRequest.BodyPublishers.ofString(content, StandardCharsets.UTF_8));
        return builder.build();
    }
}
