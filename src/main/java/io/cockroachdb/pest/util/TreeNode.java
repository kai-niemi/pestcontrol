package io.cockroachdb.pest.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A simple directed acyclic graph (DAG) structure (aka tree without cycles)
 * with different traversals.
 *
 * @param <T> the node type
 * @author Kai Niemi
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {
    public static <T> void breadthFirstTraversal(TreeNode<T> root,
                                                 Predicate<TreeNode<T>> filter) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode<T> current = stack.poll();
            if (current != null) {
                if (!filter.test(current)) {
                    break;
                }
                current.children.forEach(stack::push);
            } else {
                break;
            }
        }
    }

    public static <T> void depthFirstTraversal(TreeNode<T> root,
                                               Predicate<TreeNode<T>> filter) {
        Deque<TreeNode<T>> visited = new ArrayDeque<>();
        Deque<TreeNode<T>> stack = new ArrayDeque<>();

        stack.push(root);

        while (!stack.isEmpty()) {
            TreeNode<T> current = stack.pop();
            if (!visited.contains(current)) {
                visited.addFirst(current);
                if (!filter.test(current)) {
                    break;
                }
                current.children
                        .stream()
                        .filter(t -> !visited.contains(t))
                        .forEach(stack::push);
            }
        }
    }

    public static <T> void topologicalSort(TreeNode<T> root,
                                           Predicate<TreeNode<T>> filter) {
        Deque<TreeNode<T>> visited = new ArrayDeque<>();
        topologicalSortR(root, filter, visited);
    }

    private static <T> void topologicalSortR(TreeNode<T> current,
                                             Predicate<TreeNode<T>> filter,
                                             Deque<TreeNode<T>> visited) {
        if (filter.test(current)) {
            current.children
                    .stream()
                    .filter(t -> !visited.contains(t))
                    .forEach(n -> topologicalSortR(n, filter, visited));

            visited.addFirst(current);
        }
    }

    public static <T> TreeNode<T> of(T value) {
        return new TreeNode<>(value);
    }

    private final T value;

    private final List<TreeNode<T>> children = new ArrayList<>();

    private int depth;

    public TreeNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public int getDepth() {
        return depth;
    }

    public TreeNode<T> addChild(T value) {
        TreeNode<T> child = TreeNode.of(value);
        child.depth = depth + 1;
        children.add(child);
        return child;
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return children.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(value, treeNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
               "children=" + children +
               ", value=" + value +
               ", depth=" + depth +
               '}';
    }
}