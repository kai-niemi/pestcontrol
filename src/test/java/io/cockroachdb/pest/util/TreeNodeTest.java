package io.cockroachdb.pest.util;

import org.junit.jupiter.api.Test;

public class TreeNodeTest {
    @Test
    public void whenBasicTree_expectDAG() {
        TreeNode<String> root = TreeNode.of("root");

        root.addChild("a");
        root.addChild("b");
        root.addChild("c");

        TreeNode<String> d = root.addChild("d");
        d.addChild("d1");
        d.addChild("d2");
        d.addChild("d3");

        TreeNode<String> e = root.addChild("e");
        e.addChild("e1");
        e.addChild("e2");

        TreeNode<String> e3 = e.addChild("e3");
        e3.addChild("f1");
        e3.addChild("f2");

        e.addChild("e4");

        System.out.println("BFS traversal:");
        TreeNode.breadthFirstTraversal(root, s -> {
            System.out.println(s.getValue());
            return true;
        });

        System.out.println("DFS traversal:");
        TreeNode.depthFirstTraversal(root, s -> {
            System.out.println(s.getValue());
            return true;
        });

        System.out.println("TS traversal:");
        TreeNode.topologicalSort(root, n -> {
            String space = new String(new char[n.getDepth()])
                    .replace('\0', '.');
            System.out.println("%s %s".formatted(space, n.getValue()));
            return true;
        });
    }
}
