
package riscv_simulator;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "http://localhost:5173")
public class SimulatorController {

    private final ExecutionEngine engine = new ExecutionEngine();

    // Load assembly program
    @PostMapping("/load")
    public Map<String, Object> loadProgram(
            @RequestBody Map<String, String> request) {

        String program = request.get("program");

        if (program == null || program.isBlank()) {
            throw new IllegalArgumentException(
                "Program cannot be empty"
            );
        }

        engine.loadProgram(program);

        return getState("Program loaded successfully.");
    }

    // Execute one instruction
    @PostMapping("/step")
    public Map<String, Object> step() {

        String message = engine.step();

        return getState(message);
    }

    // Execute remaining instructions
    @PostMapping("/run")
    public Map<String, Object> run() {

        List<String> history = engine.run();

        Map<String, Object> state = getState(
            "Program execution completed."
        );

        state.put("history", history);

        return state;
    }

    // Reset simulator
    @PostMapping("/reset")
    public Map<String, Object> reset() {

        engine.reset();

        return getState("Simulator reset successfully.");
    }

    // Get current state
    @GetMapping("/state")
    public Map<String, Object> getCurrentState() {

        return getState("Current simulator state.");
    }

    // Prepare response
    private Map<String, Object> getState(String message) {

        List<Integer> registerValues =
            Arrays.stream(engine.getRegisters())
                  .boxed()
                  .toList();

        Map<String, Object> state = new HashMap<>();

        state.put("message", message);
        state.put("pc", engine.getPC());
        state.put("registers", registerValues);

        // Machine-code information
        state.put(
            "instructions",
            engine.getInstructionDetails()
        );

        // Memory information
        state.put(
            "memory",
            engine.getMemory()
        );

        return state;
    }
}
