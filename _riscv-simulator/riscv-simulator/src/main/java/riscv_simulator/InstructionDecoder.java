
package riscv_simulator;

import java.util.Set;

public class InstructionDecoder {

    private static final Set<String> R_TYPE = Set.of(
        "ADD", "SUB", "MUL", "AND", "OR",
        "XOR", "SLL", "SRL", "SRA", "SLT", "SLTU"
    );

    private static final Set<String> I_TYPE = Set.of(
        "ADDI", "ANDI", "ORI", "XORI", "SLTI"
    );

    private static final Set<String> BRANCH_TYPE = Set.of(
        "BEQ", "BNE", "BLT", "BGE"
    );

    public DecodedInstruction decode(String line) {

        line = line.split("#", 2)[0].trim();

        if (line.isEmpty()) {
            return null;
        }

        String[] parts = line.toUpperCase().split("[\\s,]+");
        String operation = parts[0];

        // R-Type instructions
        if (R_TYPE.contains(operation)) {

            if (parts.length != 4) {
                throw new IllegalArgumentException(
                    "Invalid R-type instruction: " + line
                );
            }

            return new DecodedInstruction(
                operation,
                parseRegister(parts[1]),
                parseRegister(parts[2]),
                parseRegister(parts[3]),
                0
            );
        }

        // I-Type instructions
        if (I_TYPE.contains(operation)) {

            if (parts.length != 4) {
                throw new IllegalArgumentException(
                    "Invalid I-type instruction: " + line
                );
            }

            int immediate = Integer.decode(parts[3]);

            if (immediate < -2048 || immediate > 2047) {
                throw new IllegalArgumentException(
                    "Immediate out of range"
                );
            }

            return new DecodedInstruction(
                operation,
                parseRegister(parts[1]),
                parseRegister(parts[2]),
                -1,
                immediate
            );
        }

        // Load and Store instructions
        if (operation.equals("LW") || operation.equals("SW")) {

            if (parts.length != 3) {
                throw new IllegalArgumentException(
                    "Invalid memory instruction: " + line
                );
            }

            int register = parseRegister(parts[1]);
            String address = parts[2];

            if (!address.matches(
                "-?\\d+\\(X([0-9]|[12][0-9]|3[01])\\)"
            )) {
                throw new IllegalArgumentException(
                    "Invalid memory address: " + address
                );
            }

            int bracketStart = address.indexOf('(');

            int offset = Integer.parseInt(
                address.substring(0, bracketStart)
            );

            int baseRegister = parseRegister(
                address.substring(
                    bracketStart + 1,
                    address.length() - 1
                )
            );

            if (offset < -2048 || offset > 2047) {
                throw new IllegalArgumentException(
                    "Memory offset out of range"
                );
            }

            if (operation.equals("LW")) {
                return new DecodedInstruction(
                    "LW", register, baseRegister, -1, offset
                );
            }

            return new DecodedInstruction(
                "SW", -1, baseRegister, register, offset
            );
        }

        // Branch instructions
        if (BRANCH_TYPE.contains(operation)) {

            if (parts.length != 4) {
                throw new IllegalArgumentException(
                    "Invalid branch instruction: " + line
                );
            }

            int rs1 = parseRegister(parts[1]);
            int rs2 = parseRegister(parts[2]);
            int offset = Integer.decode(parts[3]);

            if (offset < -4096 || offset > 4094 || offset % 2 != 0) {
                throw new IllegalArgumentException(
                    "Invalid branch offset: " + offset
                );
            }

            return new DecodedInstruction(
                operation, -1, rs1, rs2, offset
            );
        }

        // JAL instruction
        if (operation.equals("JAL")) {

            if (parts.length != 3) {
                throw new IllegalArgumentException(
                    "Invalid JAL instruction: " + line
                );
            }

            int rd = parseRegister(parts[1]);
            int offset = Integer.decode(parts[2]);

            if (offset < -1048576 ||
                offset > 1048574 ||
                offset % 2 != 0) {

                throw new IllegalArgumentException(
                    "Invalid JAL offset: " + offset
                );
            }

            return new DecodedInstruction(
                "JAL", rd, -1, -1, offset
            );
        }

        // JALR instruction
        if (operation.equals("JALR")) {

            if (parts.length != 4) {
                throw new IllegalArgumentException(
                    "Invalid JALR instruction: " + line
                );
            }

            int rd = parseRegister(parts[1]);
            int rs1 = parseRegister(parts[2]);
            int offset = Integer.decode(parts[3]);

            if (offset < -2048 || offset > 2047) {
                throw new IllegalArgumentException(
                    "Invalid JALR offset: " + offset
                );
            }

            return new DecodedInstruction(
                "JALR", rd, rs1, -1, offset
            );
        }

        throw new IllegalArgumentException(
            "Unsupported instruction: " + operation
        );
    }

    private int parseRegister(String register) {

        if (!register.matches(
            "X([0-9]|[12][0-9]|3[01])"
        )) {
            throw new IllegalArgumentException(
                "Invalid register: " + register
            );
        }

        return Integer.parseInt(
            register.substring(1)
        );
    }

    public static class DecodedInstruction {

        private final String operation;
        private final int rd;
        private final int rs1;
        private final int rs2;
        private final int immediate;

        public DecodedInstruction(
            String operation,
            int rd,
            int rs1,
            int rs2,
            int immediate
        ) {
            this.operation = operation;
            this.rd = rd;
            this.rs1 = rs1;
            this.rs2 = rs2;
            this.immediate = immediate;
        }

        public String getOperation() {
            return operation;
        }

        public int getRd() {
            return rd;
        }

        public int getRs1() {
            return rs1;
        }

        public int getRs2() {
            return rs2;
        }

        public int getImmediate() {
            return immediate;
        }
    }
}
