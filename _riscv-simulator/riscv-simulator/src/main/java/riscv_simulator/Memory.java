
package riscv_simulator;

import java.util.Arrays;

public class Memory {

    private final int[] memory;

    public Memory() {
        memory = new int[1024]; // 4 KB memory
        reset();
    }

    // Store a 32-bit word
    public void storeWord(int address, int value) {

        validateAddress(address);

        memory[address / 4] = value;
    }

    // Load a 32-bit word
    public int loadWord(int address) {

        validateAddress(address);

        return memory[address / 4];
    }

    // Clear memory
    public void reset() {
        Arrays.fill(memory, 0);
    }

    // Return all memory values
    public int[] getAllMemory() {
        return memory.clone();
    }

    private void validateAddress(int address) {

        if (address < 0 || address >= 4096) {
            throw new IllegalArgumentException(
                "Memory address out of range: " + address
            );
        }

        if (address % 4 != 0) {
            throw new IllegalArgumentException(
                "Word address must be 4-byte aligned: " + address
            );
        }
    }
}
