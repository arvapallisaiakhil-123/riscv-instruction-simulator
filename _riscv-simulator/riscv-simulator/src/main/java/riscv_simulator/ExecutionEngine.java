
package riscv_simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionEngine {

    private final RegisterFile registers;
    private final ALU alu;
    private final ProgramCounter pc;
    private final InstructionDecoder decoder;
    private final Memory memory;

    private List<String> instructions = new ArrayList<>();

    public ExecutionEngine() {
        registers = new RegisterFile();
        alu = new ALU();
        pc = new ProgramCounter();
        decoder = new InstructionDecoder();
        memory = new Memory();
    }

    // Load program and resolve assembly labels
    public void loadProgram(String source) {

        instructions.clear();

        Map<String, Integer> labels = new HashMap<>();
        List<String> programLines = new ArrayList<>();

        int address = 0;

        // First pass: identify labels
        for (String line : source.split("\\R")) {

            String cleaned = line.split("#", 2)[0].trim();

            if (cleaned.isEmpty()) {
                continue;
            }

            while (cleaned.contains(":")) {

                String[] parts = cleaned.split(":", 2);
                String label = parts[0].trim().toUpperCase();

                if (!label.matches("[A-Z_][A-Z0-9_]*")) {
                    throw new IllegalArgumentException(
                        "Invalid label: " + label
                    );
                }

                if (labels.containsKey(label)) {
                    throw new IllegalArgumentException(
                        "Duplicate label: " + label
                    );
                }

                labels.put(label, address);
                cleaned = parts[1].trim();

                if (cleaned.isEmpty()) {
                    break;
                }
            }

            if (!cleaned.isEmpty()) {
                programLines.add(cleaned);
                address += 4;
            }
        }

        // Second pass: replace label references with offsets
        for (int i = 0; i < programLines.size(); i++) {

            String line = programLines.get(i);
            String[] parts = line.split("[\\s,]+");

            String operation = parts[0].toUpperCase();

            int targetIndex = -1;

            if (operation.equals("JAL") && parts.length == 3) {
                targetIndex = 2;
            }

            if (
                operation.equals("BEQ") ||
                operation.equals("BNE") ||
                operation.equals("BLT") ||
                operation.equals("BGE")
            ) {
                if (parts.length == 4) {
                    targetIndex = 3;
                }
            }

            if (targetIndex != -1) {

                String target = parts[targetIndex].toUpperCase();

                if (labels.containsKey(target)) {

                    int currentAddress = i * 4;

                    int offset =
                        labels.get(target) - currentAddress;

                    parts[targetIndex] = String.valueOf(offset);
                    line = String.join(" ", parts);
                }
            }

            instructions.add(line);
        }

        reset();
    }

    // Execute one instruction
    public String step() {

        int currentPC = pc.getPC();

        if (
            currentPC < 0 ||
            currentPC % 4 != 0 ||
            currentPC / 4 >= instructions.size()
        ) {
            return "Program execution completed.";
        }

        String instruction = instructions.get(currentPC / 4);

        InstructionDecoder.DecodedInstruction decoded =
            decoder.decode(instruction);

        String operation = decoded.getOperation();

        int result = 0;

        // LOAD WORD
        if (operation.equals("LW")) {

            int address =
                registers.read(decoded.getRs1())
                + decoded.getImmediate();

            result = memory.loadWord(address);
            registers.write(decoded.getRd(), result);
        }

        // STORE WORD
        else if (operation.equals("SW")) {

            int address =
                registers.read(decoded.getRs1())
                + decoded.getImmediate();

            int value = registers.read(decoded.getRs2());

            memory.storeWord(address, value);
            result = value;
        }

        // BRANCH INSTRUCTIONS
        else if (
            operation.equals("BEQ") ||
            operation.equals("BNE") ||
            operation.equals("BLT") ||
            operation.equals("BGE")
        ) {

            int a = registers.read(decoded.getRs1());
            int b = registers.read(decoded.getRs2());

            boolean taken = switch (operation) {
                case "BEQ" -> a == b;
                case "BNE" -> a != b;
                case "BLT" -> a < b;
                case "BGE" -> a >= b;
                default -> false;
            };

            if (taken) {

                int target = currentPC + decoded.getImmediate();
                validateTarget(target);
                pc.setPC(target);

                return "Executed: " + instruction
                    + " | Branch taken | PC: " + target;
            }
        }

        // JAL
        else if (operation.equals("JAL")) {

            int returnAddress = currentPC + 4;
            int target = currentPC + decoded.getImmediate();

            validateTarget(target);

            registers.write(decoded.getRd(), returnAddress);
            pc.setPC(target);

            return "Executed: " + instruction
                + " | JAL jump to: " + target
                + " | Return address: " + returnAddress;
        }

        // JALR
        else if (operation.equals("JALR")) {

            int base = registers.read(decoded.getRs1());
            int returnAddress = currentPC + 4;

            int target =
                (base + decoded.getImmediate()) & ~1;

            validateTarget(target);

            registers.write(decoded.getRd(), returnAddress);
            pc.setPC(target);

            return "Executed: " + instruction
                + " | JALR jump to: " + target
                + " | Return address: " + returnAddress;
        }

        // ARITHMETIC AND LOGICAL INSTRUCTIONS
        else {

            int a = registers.read(decoded.getRs1());

            int b = decoded.getRs2() == -1
                ? decoded.getImmediate()
                : registers.read(decoded.getRs2());

            String aluOperation = switch (operation) {
                case "ADDI" -> "ADD";
                case "ANDI" -> "AND";
                case "ORI" -> "OR";
                case "XORI" -> "XOR";
                case "SLTI" -> "SLT";
                default -> operation;
            };

            result = alu.execute(aluOperation, a, b);

            registers.write(decoded.getRd(), result);
        }

        pc.increment();

        return "Executed: " + instruction + " | Result: " + result;
    }

    // Validate jump and branch address
    private void validateTarget(int target) {

        if (
            target < 0 ||
            target % 4 != 0 ||
            target / 4 >= instructions.size()
        ) {
            throw new IllegalArgumentException(
                "Invalid instruction address: " + target
            );
        }
    }

    // Run complete program
    public List<String> run() {

        List<String> history = new ArrayList<>();

        while (
            pc.getPC() >= 0 &&
            pc.getPC() % 4 == 0 &&
            pc.getPC() / 4 < instructions.size()
        ) {
            history.add(step());
        }

        return history;
    }

    // Reset simulator
    public void reset() {
        registers.reset();
        pc.reset();
        memory.reset();
    }

    public int[] getRegisters() {
        return registers.getAllRegisters();
    }

    public int getPC() {
        return pc.getPC();
    }

    public int[] getMemory() {
        return memory.getAllMemory();
    }

    // Generate machine-code details for loaded instructions
    public List<Map<String, Object>> getInstructionDetails() {

        List<Map<String, Object>> details = new ArrayList<>();

        InstructionEncoder encoder = new InstructionEncoder();

        for (int i = 0; i < instructions.size(); i++) {

            String instruction = instructions.get(i);

            int machineCode = encoder.encode(instruction);

            Map<String, Object> item = new HashMap<>();

            item.put("address", i * 4);
            item.put("instruction", instruction);
            item.put("machineCode", machineCode);
            item.put("binary", encoder.toBinary(machineCode));
            item.put("hex", encoder.toHex(machineCode));

            details.add(item);
        }

        return details;
    }
}
