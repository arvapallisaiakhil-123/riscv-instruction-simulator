
package riscv_simulator;

public class ALU {

    public int execute(String operation, int a, int b) {

        switch (operation.toUpperCase()) {

            case "ADD":
                return a + b;

            case "SUB":
                return a - b;

            case "MUL":
                return a * b;

            case "AND":
                return a & b;

            case "OR":
                return a | b;

            case "XOR":
                return a ^ b;

            case "SLL":
                return a << (b & 0x1F);

            case "SRL":
                return a >>> (b & 0x1F);

            case "SRA":
                return a >> (b & 0x1F);

            case "SLT":
                return a < b ? 1 : 0;

            case "SLTU":
                return Integer.compareUnsigned(a, b) < 0 ? 1 : 0;

            default:
                throw new IllegalArgumentException(
                    "Unsupported ALU operation: " + operation
                );
        }
    }
}
