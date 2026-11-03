# RISC-V Instruction Simulator

An interactive RISC-V instruction simulator built using **React, Tailwind CSS, Java, and Spring Boot**. The project helps students understand how RISC-V assembly instructions are decoded and executed through a visual interface.

## Features

- **Assembly Code Execution:** Load and execute RISC-V assembly programs.
- **Step-by-Step Execution:** Execute instructions one at a time.
- **Run Program:** Execute the complete program.
- **Register Visualization:** View all 32 RISC-V registers and their values.
- **Program Counter:** Track the current instruction address.
- **Memory Visualization:** View memory values and load/store operations.
- **Instruction Encoding:** Display machine code in hexadecimal and binary.
- **Execution History:** Track the instructions executed by the simulator.
- **Control Flow:** Support conditional branches and jump instructions.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React, JavaScript |
| Styling | Tailwind CSS |
| Backend | Java, Spring Boot |
| Build Tool | Maven |
| Development Tools | VS Code, RARS |

## Project Structure

```text
RISC-V/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
└── _riscv-simulator/
    └── riscv-simulator/
        ├── src/
        │   ├── main/
        │   │   ├── java/riscv_simulator/
        │   │   └── resources/
        │   └── test/
        ├── pom.xml
        └── mvnw.cmd
```

## Supported Instructions

**Arithmetic and Logical Instructions**
- ADD, SUB, MUL
- ADDI
- AND, OR, XOR
- ANDI, ORI, XORI
- SLL, SRL, SRA
- SLT, SLTI, SLTU

**Memory Instructions**
- LW
- SW

**Branch Instructions**
- BEQ
- BNE
- BLT
- BGE

**Jump Instructions**
- JAL
- JALR

The simulator implements an educational subset of RISC-V instructions, not the complete RISC-V specification.

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/arvapallisaiakhil-123/riscv-instruction-simulator.git
cd riscv-instruction-simulator
```

### 2. Start the Backend

Open a terminal:

```powershell
cd _riscv-simulator/riscv-simulator
```

Set your Java environment variables if needed:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Run Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend URL:

`http://localhost:8081`

### 3. Start the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the frontend in your browser:

`http://localhost:5173`

## API Endpoints

Base URL: `http://localhost:8081/api/simulator`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/load` | Load an assembly program |
| POST | `/step` | Execute one instruction |
| POST | `/run` | Execute the program |
| POST | `/reset` | Reset simulator state |
| GET | `/state` | Retrieve simulator state |

## Example Assembly Program

```asm
ADDI x1, x0, 10
ADDI x2, x0, 20
ADD x3, x1, x2
SUB x4, x2, x1
```

Expected results:

- x1 = 10
- x2 = 20
- x3 = 30
- x4 = 10

## Learning Objectives

- Understand RISC-V instruction formats.
- Learn register-based operations.
- Understand instruction decoding and execution.
- Explore program counter updates.
- Understand memory access and control flow.
- Learn how assembly instructions are represented as machine code.

## Future Improvements

- Add more RISC-V instruction types.
- Improve instruction-level visualization.
- Add pipeline execution visualization.
- Add data hazard and control hazard demonstrations.
- Add more sample assembly programs.

## Author

**A. Sai Akhil**

GitHub: [@arvapallisaiakhil-123](https://github.com/arvapallisaiakhil-123)

---

If you find this project useful, consider giving the repository a ⭐.
