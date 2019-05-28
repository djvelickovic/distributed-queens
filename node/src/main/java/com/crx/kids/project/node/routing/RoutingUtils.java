package com.crx.kids.project.node.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutingUtils {

    /**
     * Divide And Round At Higher
     * @param nodeId
     * @return
     */
    public static int darah(int nodeId) {
        return nodeId / 2 + nodeId % 2;
    }

    public static int min(int n1, int n2) {
        return n1 < n2 ? n1 : n2;
    }

    public static int max(int n1, int n2) {
        return n1 > n2 ? n1 : n2;
    }

    public static List<Integer> chain(int nodeId) {
        List<Integer> chain = new ArrayList<>();

        while (nodeId != 1) {
            chain.add(nodeId);
            nodeId = RoutingUtils.darah(nodeId);
        }
        chain.add(1);
        Collections.reverse(chain);
        return chain;
    }
}
