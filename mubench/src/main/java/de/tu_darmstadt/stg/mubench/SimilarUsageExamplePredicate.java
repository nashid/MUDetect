package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import de.tu_darmstadt.stg.mudetect.src2aug.EGroumGraph;
import mining.TypeUsageExamplePredicate;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SimilarUsageExamplePredicate extends TypeUsageExamplePredicate {
    private final Set<String> labels;

    public static SimilarUsageExamplePredicate examplesSimilarTo(APIUsageExample misuseInstance, API api) {
        Set<String> labels = misuseInstance.vertexSet().stream().map(Node::getLabel).collect(Collectors.toSet());
        return new SimilarUsageExamplePredicate(labels, api);
    }

    private SimilarUsageExamplePredicate(Set<String> labels, API api) {
        super(api.getName());
        this.labels = labels;
    }

    @Override
	public boolean matches(EGroumGraph graph) {
        return labels.isEmpty() || !Collections.disjoint(graph.getNodeLabels(), labels);
    }
}
