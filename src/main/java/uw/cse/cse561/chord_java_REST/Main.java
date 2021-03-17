package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import picocli.CommandLine;
import uw.cse.cse561.chord_java_REST.Test.TestStabilize;
import uw.cse.cse561.chord_java_REST.chord.ChordNode;
import uw.cse.cse561.chord_java_REST.chord.LocalChordNode;
import uw.cse.cse561.chord_java_REST.chord.RemoteChordNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main implements Runnable {
    public static void main(String[] args) {
        TestStabilize.main();
//        new CommandLine(new Main()).execute(args);
    }

    @CommandLine.Option(names = {"-a", "--address"}, description = "Listening address", defaultValue = "0.0.0.0")
    private String listenAddress;

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port", defaultValue = "8080")
    private int port;

    @CommandLine.Option(names = {"-i", "--ids"}, description = "Chord node ids", defaultValue = "0", split = ",")
    private List<Integer> ids;

    @CommandLine.Option(names = {"-u", "--url"}, description = "Chord node URLs", defaultValue = "", split = ",")
    private List<String> urls;

    @CommandLine.Option(names = {"-s", "--size"}, description = "Key space size", defaultValue = "128")
    private int keySpaceSize;

    @Override
    public void run() {
        URI listenURI = UriBuilder.fromPath("/").scheme("http").host(listenAddress).port(port).build();

        Map<Integer, ChordNode> chordNodes = new HashMap<>();
        ChordApplication application = ChordApplication.builder()
                .chordNodes(chordNodes)
                .build();

        if (ids.size() != urls.size())
            throw new IllegalArgumentException();

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            String url = urls.get(i);

            if (url.equals("local")) {
                LocalChordNode node = LocalChordNode.create(listenURI, id, keySpaceSize, true);

                if (i == 0) {
                    node.join(null);
                } else {
                    node.join(chordNodes.get(ids.get(0)));
                }

                chordNodes.put(id, node);
            } else {
                chordNodes.put(id,
                        RemoteChordNode.builder()
                                .uri(UriBuilder.fromUri(url).build())
                                .id(id)
                                .application(application)
                                .build());
            }
        }

        ResourceConfig rc = ResourceConfig.forApplication(application);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(listenURI, rc);
        System.out.println(MessageFormat.format("Starting server at {0}....", listenURI.toString()));
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
