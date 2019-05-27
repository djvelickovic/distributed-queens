package com.crx.kids.project.node.api;

import java.util.Map;

public class Command {
    private CommandType command;
    private Map<String, String> parameters;

    public CommandType getCommand() {
        return command;
    }

    public void setCommand(CommandType command) {
        this.command = command;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
