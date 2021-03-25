package uw.cse.cse561.chord_java_REST.resource;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class VisitedModel {
    private List<Integer> visited;
}
