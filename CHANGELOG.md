# Changelog

## Unreleased
- Instruction: refactored to `Value`-based operands/results; added assignment (`name = expr`), `parse`, `sum`, and `print(expr)`; added `value[n]` token addressing; `write(...)` can emit current `result`.
- ProcessContext: now stores `Value` fields and a variable map for runtime assignment/lookup.
- ProgramCode: added `fromScript(...)` to compile `script.txt` line-by-line (skipping blanks and `#` comments).
- ProgramCode script syntax (general rules):
  ```
  # one instruction per line; blank lines and lines starting with # are ignored
  print("text")         # print string literal
  print(expr)           # print a variable or expression
  read("path")          # read file into ctx.content
  parse                # split ctx.content into value[0], value[1], ...
  sum                  # sum numeric tokens in ctx.content into result
  write("path")         # write current result to file
  name = expr           # assign variable (expr can use value[n], variables, literals)
  expr                 # bare expression (e.g., a + b) updates result
  ```
  Example:
  ```
  print("Reading numbers")
  read("src/read_in.txt")
  parse
  sum = 0
  a = value[0]
  b = value[1]
  sum = sum + a
  sum = sum + b
  write("src/out.txt")
  ```
- CPUEx: runs `Instruction` instances from `ProgramCode` as part of the kernel execution path.

## 2026-01-10
- `src/Execution/CPUEx.java`: time-sharing uses a 20ms slice and can execute multiple CPU instructions per slice before preemption; termination now reports through the kernel.
- `src/Kernel/Kernel.java`: scheduler polls READY with a timeout to allow auto-stop, tracks active processes, defines priority aging and behavior-based reclassification, and updates priority semantics so larger values mean higher priority.
- `src/Execution/IOEx.java`: documents I/O sleep as device latency and suppresses the busy-wait lint warning.
- `src/Main.java`: removes the busy-wait loop; main now just joins the kernel thread since the kernel self-stops.

Priority assignment logic (conceptual):
```
initialize_process(proc):
    proc.priority           = 5
    proc.cpu_ticks          = 0
    proc.wait_ticks         = 0
    proc.io_ratio           = 0.0
    proc.ready_wait_steps   = 0
    proc.reclassify_counter = 0

on scheduling_event(proc, state):
    if state == RUNNING:
        proc.cpu_ticks += 1
        proc.ready_wait_steps = 0
        proc.reclassify_counter += 1
    else if state == BLOCKED:
        proc.wait_ticks += 1
        proc.ready_wait_steps = 0
        proc.reclassify_counter += 1
    else if state == READY:
        proc.ready_wait_steps += 1

    # Priority aging (starvation prevention)
    if proc.ready_wait_steps >= 10:
        proc.priority = min(proc.priority + 1, MAX_PRIORITY)
        proc.ready_wait_steps = 0

    # Behavior-based reclassification (every N = 4 slices)
    if proc.reclassify_counter >= 4:
        total = proc.cpu_ticks + proc.wait_ticks
        if total > 0:
            proc.io_ratio = proc.wait_ticks / total
            if proc.io_ratio > 0.6:
                proc.priority = min(proc.priority + 1, MAX_PRIORITY)
            else if proc.io_ratio < 0.4:
                proc.priority = max(proc.priority - 1, MIN_PRIORITY)
        proc.cpu_ticks  = 0
        proc.wait_ticks = 0
        proc.reclassify_counter = 0
```
