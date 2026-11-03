
import { useState } from 'react'

import {
  Cpu,
  Play,
  SkipForward,
  RotateCcw,
  Code2,
  MemoryStick,
  Activity,
  Terminal,
  Zap,
  Database,
  Binary,
  CheckCircle2
} from 'lucide-react'

const API_URL = 'https://riscv-simulator-backend.onrender.com/api/simulator';

const initialRegisters = Array.from({ length: 32 }, (_, i) => ({
  name: `x${i}`,
  value: 0
}))

async function apiRequest(endpoint, options = {}) {
  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    }
  })

  const data = await response.json()

  if (!response.ok) {
    throw new Error(data.message || 'Backend request failed')
  }

  return data
}

// Remove comments and labels to identify executable instructions
function getExecutableInstructions(source) {
  const result = []

  source.split('\n').forEach(line => {
    let cleaned = line.split('#', 2)[0].trim()

    if (!cleaned) return

    while (cleaned.includes(':')) {
      cleaned = cleaned.split(':', 2)[1].trim()
      if (!cleaned) return
    }

    if (cleaned) result.push(cleaned)
  })

  return result
}

function getHistoryItem(message, address) {
  const text = String(message || '')

  const instructionMatch = text.match(/Executed:\s*(.*?)\s*\|/)
  const resultMatch = text.match(/Result:\s*(-?\d+)/)
  const jumpMatch = text.match(/(?:PC:|jump to:)\s*(-?\d+)/)

  return {
    pc: address,
    instruction: instructionMatch
      ? instructionMatch[1]
      : text,
    result: resultMatch ? Number(resultMatch[1]) : null,
    message: text,
    nextPC: jumpMatch ? Number(jumpMatch[1]) : address + 4
  }
}

function App() {

  const [code, setCode] = useState(
`ADDI x1, x0, 10
ADDI x2, x0, 20
ADD  x3, x1, x2
SUB  x4, x2, x1`
  )

  const [registers, setRegisters] = useState(initialRegisters)
  const [pc, setPc] = useState(0)
  const [history, setHistory] = useState([])
  const [memory, setMemory] = useState(null)
  const [instructionDetails, setInstructionDetails] = useState([])
  const [status, setStatus] = useState('Ready')
  const [loading, setLoading] = useState(false)

  const instructions = getExecutableInstructions(code)

  const currentIndex = pc >= 0 ? pc / 4 : -1

  const currentInstruction =
    Number.isInteger(currentIndex)
      ? instructions[currentIndex] || 'Program completed'
      : 'None'

  // Update frontend using backend response
  function updateState(data) {

    if (typeof data.pc === 'number') {
      setPc(data.pc)
    }

    if (Array.isArray(data.registers)) {
      setRegisters(
        data.registers.map((value, i) => ({
          name: `x${i}`,
          value
        }))
      )
    }

    if (Array.isArray(data.memory)) {
      setMemory(data.memory)
    }

    if (Array.isArray(data.instructions)) {
      setInstructionDetails(data.instructions)
    }
  }

  // Assemble program
  async function assemble() {

    if (loading) return

    setLoading(true)
    setHistory([])
    setMemory(null)
    setInstructionDetails([])

    try {

      const data = await apiRequest('/load', {
        method: 'POST',
        body: JSON.stringify({ program: code })
      })

      updateState(data)

      setStatus(data.message || 'Program assembled successfully')

    } catch (error) {

      setStatus(`Error: ${error.message}`)

    } finally {

      setLoading(false)

    }
  }

  // Execute one instruction
  async function executeStep() {

    if (loading) return

    if (pc / 4 >= instructions.length) {
      setStatus('Completed')
      return
    }

    setLoading(true)

    try {

      const currentAddress = pc

      const data = await apiRequest('/step', {
        method: 'POST'
      })

      updateState(data)

      if (data.message?.startsWith('Executed:')) {
        setHistory(old => [
          ...old,
          getHistoryItem(data.message, currentAddress)
        ])
      }

      setStatus(data.message || 'Instruction executed')

    } catch (error) {

      setStatus(`Error: ${error.message}`)

    } finally {

      setLoading(false)

    }
  }

  // Run complete program
  async function runProgram() {

    if (loading) return

    if (pc / 4 >= instructions.length) {
      setStatus('Completed')
      return
    }

    setLoading(true)

    try {

      const startingPC = pc

      const data = await apiRequest('/run', {
        method: 'POST'
      })

      updateState(data)

      let address = startingPC

      const newHistory = (data.history || []).map(message => {

        const item = getHistoryItem(message, address)

        address = item.nextPC

        return item
      })

      setHistory(old => [...old, ...newHistory])

      setStatus(data.message || 'Program execution completed')

    } catch (error) {

      setStatus(`Error: ${error.message}`)

    } finally {

      setLoading(false)

    }
  }

  // Reset simulator
  async function reset() {

    if (loading) return

    setLoading(true)

    try {

      const data = await apiRequest('/reset', {
        method: 'POST'
      })

      updateState(data)

      setHistory([])
      setStatus(data.message || 'Simulator reset')

    } catch (error) {

      setStatus(`Error: ${error.message}`)

    } finally {

      setLoading(false)

    }
  }

  // Display non-zero memory values
  const visibleMemory = Array.isArray(memory)
    ? memory
        .map((value, index) => ({
          address: index * 4,
          value
        }))
        .filter(item => item.value !== 0)
        .slice(0, 32)
    : []

  return (

    <div className="min-h-screen bg-[#090e19] text-slate-200">

      {/* HEADER */}

      <header className="h-16 border-b border-slate-800 flex items-center justify-between px-6">

        <div className="flex items-center gap-3">

          <div className="bg-blue-600 p-2 rounded-lg">
            <Cpu size={22} />
          </div>

          <div>
            <h1 className="font-bold text-lg text-white">
              RISC-V Simulator
            </h1>

            <p className="text-xs text-slate-500">
              RV32I Architecture · Educational CPU Lab
            </p>
          </div>

        </div>

        <div className="flex gap-2 items-center text-xs">
          <span className="h-2 w-2 rounded-full bg-green-500" />
          System Online
        </div>

      </header>

      <main className="p-5 max-w-[1600px] mx-auto space-y-5">

        {/* TOP STATISTICS */}

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">

          {[
            ['ARCHITECTURE', 'RV32I', Cpu],
            ['PROGRAM COUNTER', `${pc} bytes`, Activity],
            ['INSTRUCTIONS EXECUTED', history.length, Terminal]
          ].map(([label, value, Icon]) => (

            <div
              key={label}
              className="bg-[#111b2b] border border-slate-800 rounded-xl p-4"
            >

              <div className="flex justify-between text-xs text-slate-500">
                {label}
                <Icon size={17} />
              </div>

              <p className="text-2xl font-bold text-white mt-2">
                {value}
              </p>

            </div>

          ))}

        </div>

        {/* EDITOR AND REGISTERS */}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

          {/* ASSEMBLY EDITOR */}

          <section className="bg-[#111b2b] border border-slate-800 rounded-xl overflow-hidden">

            <div className="p-4 border-b border-slate-800 flex justify-between">

              <h2 className="font-semibold flex gap-2">
                <Code2 size={18} /> Assembly Editor
              </h2>

              <span className="text-xs text-slate-500">
                RV32I
              </span>

            </div>

            <textarea
              value={code}
              onChange={e => setCode(e.target.value)}
              spellCheck="false"
              disabled={loading}
              className="w-full h-64 bg-[#0b1220] p-5 font-mono text-sm text-green-400 outline-none resize-y leading-8"
            />

            <div className="p-4 border-t border-slate-800 flex flex-wrap gap-2">

              <button
                onClick={assemble}
                disabled={loading}
                className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 px-4 py-2 rounded-lg text-sm flex gap-2 items-center"
              >
                <Zap size={16} /> Assemble
              </button>

              <button
                onClick={executeStep}
                disabled={loading}
                className="bg-green-600 hover:bg-green-500 disabled:opacity-50 px-4 py-2 rounded-lg text-sm flex gap-2 items-center"
              >
                <SkipForward size={16} /> Step
              </button>

              <button
                onClick={runProgram}
                disabled={loading}
                className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 px-4 py-2 rounded-lg text-sm flex gap-2 items-center"
              >
                <Play size={16} /> Run
              </button>

              <button
                onClick={reset}
                disabled={loading}
                className="bg-slate-700 hover:bg-slate-600 disabled:opacity-50 px-4 py-2 rounded-lg text-sm flex gap-2 items-center"
              >
                <RotateCcw size={16} /> Reset
              </button>

            </div>

            <div className="px-4 pb-4 text-xs text-slate-400">

              Status:{' '}

              <span className="text-green-400">
                {status}
              </span>

              {loading && (
                <span className="ml-2">
                  Processing...
                </span>
              )}

            </div>

          </section>

          {/* REGISTER FILE */}

          <section className="bg-[#111b2b] border border-slate-800 rounded-xl overflow-hidden">

            <div className="p-4 border-b border-slate-800 flex justify-between">

              <h2 className="font-semibold flex gap-2">
                <Cpu size={18} /> Register File
              </h2>

              <span className="text-xs text-slate-500">
                32 × 32-bit
              </span>

            </div>

            <div className="h-[365px] overflow-y-auto p-3 grid grid-cols-2 sm:grid-cols-4 gap-2">

              {registers.map(reg => (

                <div
                  key={reg.name}
                  className={`rounded-lg p-2 border ${
                    reg.value !== 0
                      ? 'border-blue-700 bg-blue-950/30'
                      : 'border-slate-800 bg-[#0b1220]'
                  }`}
                >

                  <p className="text-xs text-slate-500">
                    {reg.name}
                  </p>

                  <p className="font-mono text-sm text-white">
                    {reg.value}
                  </p>

                  <p className="text-[10px] text-slate-500">
                    0x{(reg.value >>> 0)
                      .toString(16)
                      .padStart(8, '0')
                      .toUpperCase()}
                  </p>

                </div>

              ))}

            </div>

          </section>

        </div>

        {/* CURRENT INSTRUCTION */}

        <section className="bg-[#111b2b] border border-blue-800 rounded-xl p-4">

          <h2 className="font-semibold flex gap-2 mb-4">
            <Activity size={18} /> Instruction Execution
          </h2>

          <div className="space-y-2">

            {instructions.map((instruction, index) => {

              const address = index * 4

              const isCurrent =
                pc === address && pc / 4 < instructions.length

              const isExecuted = history.some(
                item => item.pc === address
              )

              return (

                <div
                  key={`${address}-${instruction}`}
                  className={`flex items-center gap-3 p-3 rounded-lg border font-mono text-sm ${
                    isCurrent
                      ? 'border-blue-500 bg-blue-950/50'
                      : isExecuted
                        ? 'border-green-900 bg-green-950/20'
                        : 'border-slate-800 bg-[#0b1220]'
                  }`}
                >

                  <span className="text-slate-500 w-12">
                    {String(address).padStart(4, '0')}
                  </span>

                  <span className="flex-1 text-green-400">
                    {instruction}
                  </span>

                  {isCurrent && (
                    <span className="text-blue-400 text-xs">
                      CURRENT
                    </span>
                  )}

                  {isExecuted && !isCurrent && (
                    <CheckCircle2
                      size={16}
                      className="text-green-500"
                    />
                  )}

                </div>

              )
            })}

            {instructions.length === 0 && (
              <p className="text-sm text-slate-500">
                Enter assembly instructions to begin.
              </p>
            )}

          </div>

        </section>

        {/* EXECUTION HISTORY AND PROCESSOR INFORMATION */}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

          {/* EXECUTION HISTORY */}

          <section className="bg-[#111b2b] border border-slate-800 rounded-xl p-4">

            <h2 className="font-semibold flex gap-2 mb-4">
              <Activity size={18} /> Execution History
            </h2>

            <div className="space-y-2 max-h-64 overflow-y-auto">

              {history.length === 0 && (
                <p className="text-sm text-slate-500">
                  No instructions executed yet.
                </p>
              )}

              {history.map((item, i) => (

                <div
                  key={`${item.pc}-${i}`}
                  className="bg-[#0b1220] p-3 rounded-lg font-mono text-xs space-y-2"
                >

                  <div className="flex justify-between gap-2">

                    <span className="text-slate-500">
                      PC: {item.pc}
                    </span>

                    <span className="text-green-400">
                      {item.instruction}
                    </span>

                  </div>

                  <p className="text-slate-400 break-words">
                    {item.message}
                  </p>

                  {item.result !== null && (
                    <p className="text-blue-400">
                      Result: {item.result}
                    </p>
                  )}

                </div>

              ))}

            </div>

          </section>

          {/* PROCESSOR INFORMATION */}

          <section className="bg-[#111b2b] border border-slate-800 rounded-xl p-4">

            <h2 className="font-semibold flex gap-2 mb-4">
              <MemoryStick size={18} /> Processor Information
            </h2>

            <div className="space-y-4 text-sm">

              <div className="flex justify-between">
                <span className="text-slate-400">Architecture</span>
                <span>RISC-V 32-bit</span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-400">Register count</span>
                <span>32</span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-400">Register width</span>
                <span>32 bits</span>
              </div>

              <div className="flex justify-between gap-4">
                <span className="text-slate-400">
                  Current instruction
                </span>

                <span className="text-right text-blue-400 font-mono">
                  {currentInstruction}
                </span>
              </div>

              <div className="flex justify-between">
                <span className="text-slate-400">Program status</span>
                <span className="text-green-400">{status}</span>
              </div>

            </div>

          </section>

        </div>

        {/* MEMORY VISUALIZATION */}

        <section className="bg-[#111b2b] border border-slate-800 rounded-xl p-4">

          <h2 className="font-semibold flex gap-2 mb-4">
            <Database size={18} /> Memory Visualization
          </h2>

          {memory === null ? (

            <p className="text-sm text-slate-500">
              Memory data is not currently available.
            </p>

          ) : visibleMemory.length === 0 ? (

            <p className="text-sm text-slate-500">
              Memory is currently empty.
            </p>

          ) : (

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">

              {visibleMemory.map(item => (

                <div
                  key={item.address}
                  className="bg-[#0b1220] border border-slate-800 rounded-lg p-3"
                >

                  <p className="text-xs text-slate-500">
                    Address: {item.address}
                  </p>

                  <p className="text-lg font-mono text-white mt-1">
                    {item.value}
                  </p>

                  <p className="text-xs text-blue-400">
                    0x{item.address.toString(16).toUpperCase()}
                  </p>

                </div>

              ))}

            </div>

          )}

        </section>

        {/* MACHINE CODE INFORMATION */}

        <section className="bg-[#111b2b] border border-blue-800 rounded-xl p-4">

          <h2 className="font-semibold flex gap-2 mb-4">
            <Binary size={18} /> Instruction Address & Machine Code
          </h2>

          <div className="overflow-x-auto">

            <table className="w-full text-sm text-left">

              <thead className="text-slate-400 border-b border-slate-800">

                <tr>
                  <th className="p-3">Address</th>
                  <th className="p-3">Assembly Instruction</th>
                  <th className="p-3">Hexadecimal</th>
                  <th className="p-3">32-bit Binary</th>
                </tr>

              </thead>

              <tbody>

                {instructionDetails.map((item, index) => {

                  const isCurrent = pc === item.address

                  return (

                    <tr
                      key={`${item.address}-${index}`}
                      className={`border-b border-slate-800 ${
                        isCurrent ? 'bg-blue-950/40' : ''
                      }`}
                    >

                      <td className="p-3 font-mono text-blue-400 whitespace-nowrap">
                        0x{Number(item.address)
                          .toString(16)
                          .padStart(8, '0')
                          .toUpperCase()}
                      </td>

                      <td className="p-3 font-mono text-green-400 whitespace-nowrap">
                        {item.instruction}
                      </td>

                      <td className="p-3 font-mono text-yellow-400 whitespace-nowrap">
                        {item.hex}
                      </td>

                      <td className="p-3 font-mono text-xs text-cyan-400 whitespace-nowrap">
                        {item.binary}
                      </td>

                    </tr>

                  )
                })}

              </tbody>

            </table>

            {instructionDetails.length === 0 && (

              <p className="text-sm text-slate-500 p-4">
                Click Assemble to generate machine-code instructions.
              </p>

            )}

          </div>

          {instructionDetails.length > 0 && (

            <p className="text-xs text-green-400 mt-3">
              Successfully received {instructionDetails.length} encoded instructions from the Java backend.
            </p>

          )}

        </section>

        <footer className="text-center text-xs text-slate-600 py-3">
          RISC-V Instruction Simulator · COA Academic Project
        </footer>

      </main>

    </div>
  )
}

export default App
