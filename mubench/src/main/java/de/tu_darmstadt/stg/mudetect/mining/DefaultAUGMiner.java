package de.tu_darmstadt.stg.mudetect.mining;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tu_darmstadt.stg.mudetect.aug.*;
import de.tu_darmstadt.stg.mudetect.aug.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.patterns.AggregateDataNode;
import mining.Configuration;
import mining.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultAUGMiner implements AUGMiner {
    private final Configuration config;

    private PrintStream out = null;

    public DefaultAUGMiner(Configuration config) {
        this.config = config;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    private void disableOut() {
        this.out = new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {}
        });
    }

    public Model mine(Collection<APIUsageExample> examples) {
        return mine(new ArrayList<>(examples));
    }

    private Model mine(ArrayList<APIUsageExample> examples) {
        Fragment.nextFragmentId = 0;
        Fragment.numofFragments = 0;

        if (config.disableSystemOut) {
            disableOut();
        }

        PrintStream originalOut = System.out;
        try {
            if (out != null) {
                System.setOut(out);
            }
            mining.Miner miner = new mining.Miner("-subgraph-finder-", config);
            return toModel(miner.mine(examples));
        } finally {
            System.setOut(originalOut);
        }
    }

    private Model toModel(Set<mining.Pattern> patterns) {
        return () -> patterns.stream().map(DefaultAUGMiner::toAUGPattern).collect(Collectors.toSet());
    }

    private static APIUsagePattern toAUGPattern(mining.Pattern pattern) {
        Set<Location> exampleLocations = new HashSet<>();
        for (Fragment example : pattern.getFragments()) {
            exampleLocations.add(example.getGraph().getLocation());
        }

        APIUsagePattern augPattern = new APIUsagePattern(pattern.getFreq(), exampleLocations);

        Fragment f = pattern.getRepresentative();
        List<Node> nodes = f.getNodes();
        Map<Node, Node> nodeMap = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Node newNode = node;

            if (node instanceof DataNode) {
                Multiset<String> values = HashMultiset.create();
                for (Fragment fragment : pattern.getFragments()) {
                    DataNode eqivalentNode = (DataNode) fragment.getNodes().get(i);
                    values.add(eqivalentNode.getValue());
                }
                newNode = new AggregateDataNode(((DataNode) node).getType(), values);
            }
            nodeMap.put(node, newNode);
            augPattern.addVertex(newNode);
        }
        for (Node node : f.getNodes()) {
            APIUsageGraph graph = node.getGraph();
            for (Edge e : graph.incomingEdgesOf(node)) {
                Node source = graph.getEdgeSource(e);
                if (f.getNodes().contains(source))
                    augPattern.addEdge(nodeMap.get(source), nodeMap.get(graph.getEdgeTarget(e)), e);
            }
        }

        return augPattern;
    }
}
