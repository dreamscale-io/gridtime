package com.dreamscale.gridtime.core.machine.executor.circuit.alarm;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.ParallelProgram;
import com.dreamscale.gridtime.core.machine.memory.type.CmdType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class AlarmScript implements ParallelProgram {

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

    @Override
    public List<TileInstructions> getInstructionsAtTick(Metronome.TickScope tickScope) {
        return null;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Wire getOutputStreamEventWire() {
        return new DevNullWire();
    }
}
