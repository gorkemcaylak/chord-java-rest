package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;

public class Main implements Runnable {
    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    @CommandLine.Option(names = {"-h", "--hostname"}, description = "Hostname", defaultValue = "0.0.0.0")
    private String hostname;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port", defaultValue = "8080")
    private int port;

    @Override
    public void run() {
        String fullHttp = MessageFormat.format("http://{0}/", hostname);
        URI uri = UriBuilder.fromUri(fullHttp).port(port).build();
        ResourceConfig rc = ResourceConfig.forApplication(new ChordApplication());
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        System.out.println(MessageFormat.format("Starting server at {0}....", uri.toString()));
        try {
            server.start();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Press enter to exit...");
            inputReader.readLine();
            server.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
