
package riscv_simulator;

import java.util.Map;

public class InstructionEncoder {

    private final InstructionDecoder decoder =
        new InstructionDecoder();

    // R-type funct3 values
    private static final Map<String, Integer> R_FUNCT3 =
        Map.ofEntries(
            Map.entry("ADD",  0b000),
            Map.entry("SUB",  0b000),
            Map.entry("MUL",  0b000),
            Map.entry("SLL",  0b001),
            Map.entry("SLT",  0b010),
            Map.entry("SLTU", 0b011),
            Map.entry("XOR",  0b100),
            Map.entry("SRL",  0b101),
            Map.entry("SRA",  0b101),
            Map.entry("OR",   0b110),
            Map.entry("AND",  0b111)
        );

    // I-type funct3 values
    private static final Map<String, Integer> I_FUNCT3 =
        Map.of(
            "ADDI", 0b000,
            "SLTI", 0b010,
            "XORI", 0b100,
            "ORI",  0b110,
            "ANDI", 0b111,
            "LW",   0b010,
            "JALR", 0b000
        );

    // Branch funct3 values
    private static final Map<String, Integer> B_FUNCT3 =
        Map.of(
            "BEQ", 0b000,
            "BNE", 0b001,
            "BLT", 0b100,
            "BGE", 0b101
        );

    // Convert assembly instruction into machine code
    public int encode(String instruction) {

        InstructionDecoder.DecodedInstruction d =
            decoder.decode(instruction);

        if (d == null) {
            throw new IllegalArgumentException(
                "Empty instruction"
            );
        }

        String op = d.getOperation();

        int rd = d.getRd();
        int rs1 = d.getRs1();
        int rs2 = d.getRs2();
        int imm = d.getImmediate();

        // R-TYPE
        if (R_FUNCT3.containsKey(op)) {

            int funct7 = switch (op) {
                case "SUB", "SRA" -> 0b0100000;
                case "MUL" -> 0b0000001;
                default -> 0b0000000;
            };

            return (funct7 << 25)
                | (rs2 << 20)
                | (rs1 << 15)
                | (R_FUNCT3.get(op) << 12)
                | (rd << 7)
                | 0b0110011;
        }

        // I-TYPE, LW AND JALR
        if (I_FUNCT3.containsKey(op)) {

            int opcode = switch (op) {
                case "LW" -> 0b0000011;
                case "JALR" -> 0b1100111;
                default -> 0b0010011;
            };

            return ((imm & 0xFFF) << 20)
                | (rs1 << 15)
                | (I_FUNCT3.get(op) << 12)
                | (rd << 7)
                | opcode;
        }

        // S-TYPE: SW
        if (op.equals("SW")) {

            int immediate = imm & 0xFFF;

            return ((immediate >> 5) << 25)
                | (rs2 << 20)
                | (rs1 << 15)
                | (0b010 << 12)
                | ((immediate & 0x1F) << 7)
                | 0b0100011;
        }

        // B-TYPE: BRANCH
        if (B_FUNCT3.containsKey(op)) {

            int value = imm & 0x1FFF;

            return (((value >> 12) & 1) << 31)
                | (((value >> 5) & 0x3F) << 25)
                | (rs2 << 20)
                | (rs1 << 15)
                | (B_FUNCT3.get(op) << 12)
                | (((value >> 1) & 0xF) << 8)
                | (((value >> 11) & 1) << 7)
                | 0b1100011;
        }

        // J-TYPE: JAL
        if (op.equals("JAL")) {

            int value = imm & 0x1FFFFF;

            return (((value >> 20) & 1) << 31)
                | (((value >> 1) & 0x3FF) << 21)
                | (((value >> 11) & 1) << 20)
                | (((value >> 12) & 0xFF) << 12)
                | (rd << 7)
                | 0b1101111;
        }

        throw new IllegalArgumentException(
            "Encoding not supported for: " + op
        );
    }

    // Convert to 32-bit binary
    public String toBinary(int machineCode) {

        return String.format(
            "%32s",
            Integer.toBinaryString(machineCode)
        ).replace(' ', '0');
    }

    // Convert to hexadecimal
    public String toHex(int machineCode) {

        return String.format(
            "0x%08X",
            machineCode
        );
    }

    public String encodeBinary(String instruction) {
        return toBinary(encode(instruction));
    }

    public String encodeHex(String instruction) {
        return toHex(encode(instruction));
    }
}
