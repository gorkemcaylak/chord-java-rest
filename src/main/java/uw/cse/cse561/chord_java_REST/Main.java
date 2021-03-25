package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import picocli.CommandLine;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;

public class Main implements Runnable {
    public static boolean MULTI = false;

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

    @CommandLine.Option(names = {"--jh", "--join-node-hostname"},
            description = "The already running node hostname. leave empty if not joining.",
            defaultValue = CommandLine.Option.NULL_VALUE)
    private String joinHostname;

    @CommandLine.Option(names = {"--jp", "--join-node-port"},
            description = "The already running node port. Use same value as current port if not joining.",
            defaultValue = CommandLine.Option.NULL_VALUE)
    private Integer joinPort;

    @CommandLine.Option(names = {"--ji", "--join-node-id"},
            description = "The already running node id",
            defaultValue = CommandLine.Option.NULL_VALUE)
    private Integer joinId;

    @CommandLine.Option(names = {"--join-retry"},
            description = "Time to retry to joining",
            defaultValue = "3")
    private int joinRetry;

    @CommandLine.Option(names = {"-m", "--multi"},
            description = "Run multiple node in one server")
    private boolean multi = false;

    @Override
    public void run() {
        MULTI = multi;
        ResourceConfig rc = null;
        URI uri = UriBuilder.fromPath("/")
                .scheme("http")
                .host(listenAddress)
                .port(port)
                .build();
        LocalChordNode localChordNode = null;
        if (MULTI) {
            rc = ResourceConfig.forApplication(MultiChordApplication.builder().chordLength(chordLength).hostname(hostname).port(port).build());
        } else {
            URI remoteAccessUri = UriBuilder
                    .fromPath("/")
                    .scheme("http")
                    .host(hostname)
                    .port(port)
                    .build();

            localChordNode = LocalChordNode.create(remoteAccessUri, id, chordLength);
            rc = ResourceConfig.forApplication(ChordApplication.builder().chordNode(localChordNode).build());
        }
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        System.out.println(MessageFormat.format("Starting server at {0}....", uri.toString()));
        try {
            server.start();
            if (!MULTI && joinHostname != null && joinPort != null && joinId != null) {
                URI joiningUri = UriBuilder.fromPath("/")
                        .scheme("http")
                        .host(joinHostname)
                        .port(joinPort)
                        .build();

                System.out.println(MessageFormat.format("Trying to join node {0} at {1}.", joinId.toString(), joiningUri));

                RemoteChordNode joiningNode = RemoteChordNode.builder()
                        .id(joinId)
                        .uri(joiningUri)
                        .build();

                for (int i = 0; i < joinRetry; ++i) {
                    if (localChordNode.join(joiningNode)) {
                        break;
                    } else {
                        System.err.println(MessageFormat.format("Failed to join node {0} at {1}.", joinId, joiningUri));
                    }
                }
            }
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Press enter to exit...");
            inputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.shutdownNow();
        }
    }
}
