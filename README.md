# Digital logic simulator 

Event-driven gate-level simulator: schedules only gates whose inputs changed, parses Verilog, and writes **VCD** which is viewable via a built in viewer or [GTKWave](https://gtkwave.sourceforge.net/). Stimulus for the module inputs can also be set. Run with no args (or `--gui`) to open the built-in editor. 

**Gates:** `and`, `or`, `nand`, `nor`, `xor`, `buf` — optional delay `nand#2 g(out, a, b);` (default delay 0).

**Stimulus:** one transition per line: `<time> <wire> <0|1|x>`

### Example: 2:1 multiplexer

```multiplexer.v```

```verilog
module mux2(s, a, b, y);
  input s, a, b;
  output y;
  wire ns, path_a, path_b;
  nand inv_ns(ns, s, s);
  and sel_a(path_a, a, ns);
  and sel_b(path_b, b, s);
  or merge(y, path_a, path_b);
endmodule;
```

`y` follows `a` when `s` is 0 and `b` when `s` is 1

```multiplexer.stim```

```text
// Start s=0: y tracks a while b=1; then s=1 so y tracks b; flip b; return s=0
0 s 0
0 a 0
0 b 1
15 a 1
30 s 1
40 b 0
50 s 0
```

### Example: 5 second delayed XOR from constructed with NANDs

```delayed_xor.v```
```verilog
module xor_nand(a, b, y);
  input a, b;
  output y;
  wire t1, t2, t3;
  nand n1(t1, a, b);
  nand#5 n2(t2, a, t1);
  nand#5 n3(t3, b, t1);
  nand n4(y, t2, t3);
endmodule;
```

```delayed_xor.stim```

```text
0 a 0
0 b 0
15 a 1
30 b 1
45 a 0
```

### Build & run

```powershell
javac -d out src/sim/*.java
java -cp out sim.Main examples/mux2.v examples/mux2.stim out/mux2.vcd 60
java -cp out sim.Main examples/xor_nand.v examples/xor_nand.stim out/xor_nand.vcd 80
```
