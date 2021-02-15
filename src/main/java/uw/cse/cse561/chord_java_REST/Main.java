package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import picocli.CommandLine;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.client.NodeClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;

public class Main implements Runnable {
    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    @CommandLine.Option(names = {"-h", "--hostname"}, description = "Hostname", defaultValue = "localhost")
    private String hostname;

    @CommandLine.Option(names = {"-a", "--address"}, description = "Listening address", defaultValue = "0.0.0.0")
    private String listenAddress;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port", defaultValue = "8080")
    private int port;

    @CommandLine.Option(names = {"-i", "--id"}, description = "Chord node id", defaultValue = "0")
    private int id;

    @CommandLine.Option(names = {"-l", "--length"}, description = "Chord length", defaultValue = "128")
    private int chordLength;

    @Override
    public void run() {
        URI uri = UriBuilder.fromPath("/").scheme("http").host(listenAddress).port(port).build();
        URI remoteAccessUri = UriBuilder.fromPath("/").scheme("http").host(hostname).port(port).build();
        LocalChordNode localChordNode = LocalChordNode.create(remoteAccessUri, id, chordLength);
        ResourceConfig rc = ResourceConfig.forApplication(ChordApplication.builder().chordNode(localChordNode).build());
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        System.out.println(MessageFormat.format("Starting server at {0}....", uri.toString()));
        System.out.println(new NodeClient(UriBuilder.fromUri("http://localhost:8181/").build()).ping());
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
