package uw.cse.cse561.chord_java_REST;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/chord-api")
public class ChordApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(HelloResource.class);
    }
}