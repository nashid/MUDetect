package de.tu_darmstadt.stg.mudetect.model;

import egroum.*;
import org.eclipse.jdt.core.dom.ASTNode;
import utils.JavaASTUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestAUGBuilder {
    private int autoId = 0;

    private final Map<String, EGroumNode> nodeMap;
    private final Set<EGroumEdge> edges;
    private final String name;

    private TestAUGBuilder(String name) {
        this.name = name;
        nodeMap = new HashMap<>();
        edges = new HashSet<>();
    }

    private TestAUGBuilder(TestAUGBuilder baseBuilder) {
        name = baseBuilder.name;
        nodeMap = new HashMap<>(baseBuilder.nodeMap);
        edges = new HashSet<>(baseBuilder.edges);
    }

    public static AUG someAUG() {
        return buildAUG().withActionNode(":dummy:").build();
    }

    public static TestAUGBuilder buildAUG() {
        return new TestAUGBuilder(":AUG:");
    }

    public static TestAUGBuilder buildAUG(String name) {
        return new TestAUGBuilder(name);
    }

    public static TestAUGBuilder extend(TestAUGBuilder baseBuilder) {return new TestAUGBuilder(baseBuilder); }

    public TestAUGBuilder withActionNodes(String... nodeNames) {
        for (String nodeName : nodeNames) {
            withActionNode(nodeName);
        }
        return this;
    }

    public TestAUGBuilder withActionNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withActionNode(nodeName, nodeName);
    }

    public TestAUGBuilder withActionNode(String id, String nodeName) {
        int nodeType;
        if (JavaASTUtil.infixExpressionLables.containsKey(nodeName)) {
            nodeName = JavaASTUtil.infixExpressionLables.get(nodeName);
            nodeType = ASTNode.INFIX_EXPRESSION;
        } else {
            nodeType = ASTNode.METHOD_INVOCATION;
        }
        return withNode(id, new EGroumActionNode(nodeName, nodeType));
    }

    public TestAUGBuilder withDataNode(String nodeName) {
        if (nodeMap.containsKey(nodeName)) {
            throw new IllegalArgumentException("A node with id '" + nodeName + "' already exists, please specify an explicit node id.");
        }
        return withDataNode(nodeName, nodeName);
    }

    public TestAUGBuilder withDataNode(String id, String nodeName) {
        return withNode(id, new EGroumDataNode(nodeName));
    }

    public TestAUGBuilder withNode(String id, EGroumNode node) {
        if (nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' already exists.");
        }
        nodeMap.put(id, node);
        return this;
    }

    private TestAUGBuilder withNodes(EGroumNode... nodes) {
        for (EGroumNode node : nodes) {
            withNode(getNextAutoId(), node);
        }
        return this;
    }

    private String getNextAutoId() {
        return Integer.toString(autoId++);
    }

    public TestAUGBuilder withDataEdge(String sourceId, EGroumDataEdge.Type type, String targetId) {
        edges.add(new EGroumDataEdge(getNode(sourceId), getNode(targetId), type));
        return this;
    }

    public EGroumNode getNode(String id) {
        if (!nodeMap.containsKey(id)) {
            throw new IllegalArgumentException("A node with id '" + id + "' does not exist.");
        }
        return nodeMap.get(id);
    }

    public EGroumEdge getEdge(String sourceNodeId, EGroumDataEdge.Type type, String targetNodeId) {
        for (EGroumEdge edge : edges) {
            if (edge.getSource() == getNode(sourceNodeId) &&
                    edge.getTarget() == getNode(targetNodeId) &&
                    edge.getLabel().equals(EGroumDataEdge.getLabel(type))) {
                return edge;
            }
        }
        throw new IllegalArgumentException("no such edge");
    }

    public AUG build() {
        AUG aug = new AUG(name, ":aug-file-path:");
        for (EGroumNode node : nodeMap.values()) {
            aug.addVertex(node);
        }
        for (EGroumEdge edge : edges) {
            aug.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return aug;
    }
}