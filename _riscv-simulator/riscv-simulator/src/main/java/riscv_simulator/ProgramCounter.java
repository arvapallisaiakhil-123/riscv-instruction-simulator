
package riscv_simulator;

public class ProgramCounter {

    private int pc;

    public ProgramCounter() {
        pc = 0;
    }

    public int getPC() {
        return pc;
    }

    // Move to the next 32-bit instruction
    public void increment() {
        pc += 4;
    }

    // Jump to a specific instruction address
    public void setPC(int address) {
        if (address % 4 != 0) {
            throw new IllegalArgumentException(
                "Instruction address must be 4-byte aligned"
            );
        }

        pc = address;
    }

    public void reset() {
        pc = 0;
    }
}
