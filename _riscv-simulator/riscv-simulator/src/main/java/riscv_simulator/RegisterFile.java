
package riscv_simulator;

import java.util.Arrays;

public class RegisterFile {

    private final int[] registers;

    public RegisterFile() {
        registers = new int[32];
        reset();
    }

    public int read(int index) {
        validateIndex(index);
        return registers[index];
    }

    public void write(int index, int value) {
        validateIndex(index);

        // x0 must always remain zero
        if (index == 0) {
            return;
        }

        registers[index] = value;
    }

    public void reset() {
        Arrays.fill(registers, 0);
    }

    public int[] getAllRegisters() {
        return registers.clone();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= 32) {
            throw new IllegalArgumentException(
                "Invalid register index: " + index
            );
        }
    }
}
