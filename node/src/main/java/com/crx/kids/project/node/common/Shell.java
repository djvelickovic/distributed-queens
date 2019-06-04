package com.crx.kids.project.node.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Shell {
    public static final Map<Integer, Ghost> ghostsInTheShell = new ConcurrentHashMap<>();
    public static Ghost host;
}
