package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.alarm;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.CmdType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class AlarmScript {

    List<Command> instructions = DefaultCollections.list();

    public void addInstruction(CmdType cmdType, String argName, String argValue) {
        instructions.add(new Command(cmdType, argName, argValue));
    }

    public static class Command {
        CmdType cmdType;
        Map<String, String> args = DefaultCollections.map();

        public Command(CmdType cmdType) {
            this.cmdType = cmdType;
        }

        public Command(CmdType cmdType, String argName, String argValue) {
            this.cmdType = cmdType;
            this.args.put(argName, argValue);
        }
    }
}
