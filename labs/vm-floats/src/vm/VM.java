package vm;

import java.util.ArrayList;
import java.util.List;

import static vm.Bytecode.BR;
import static vm.Bytecode.BRF;
import static vm.Bytecode.BRT;
import static vm.Bytecode.FADD;
import static vm.Bytecode.FCONST;
import static vm.Bytecode.FEQ;
import static vm.Bytecode.FLT;
import static vm.Bytecode.FMUL;
import static vm.Bytecode.FPRINT;
import static vm.Bytecode.FSUB;
import static vm.Bytecode.GLOAD;
import static vm.Bytecode.GSTORE;
import static vm.Bytecode.HALT;
import static vm.Bytecode.IADD;
import static vm.Bytecode.ICONST;
import static vm.Bytecode.IEQ;
import static vm.Bytecode.ILT;
import static vm.Bytecode.IMUL;
import static vm.Bytecode.ISUB;
import static vm.Bytecode.LOAD;
import static vm.Bytecode.POP;
import static vm.Bytecode.PRINT;
import static vm.Bytecode.STORE;

/** A simple stack-based interpreter */
public class VM {
	public static final int DEFAULT_STACK_SIZE = 1000;
	public static final int FALSE = 0;
	public static final int TRUE = 1;

	// registers
	int ip;             // instruction pointer register
	int sp = -1;  		// stack pointer register
	int fp = -1;        // frame pointer register

	int startip = 0;	// where execution begins

	// memory
	int[] code;         // word-addressable code memory but still bytecodes.
	int[] globals;      // global variable space
	int[] stack;		// Operand stack, grows upwards

	public boolean trace = false;

	public VM(int[] code, int startip, int nglobals) {
		this.code = code;
		this.startip = startip;
		globals = new int[nglobals];
		stack = new int[DEFAULT_STACK_SIZE];
	}

	public void exec() {
		ip = startip;
		cpu();
	}

	/** Simulate the fetch-decode execute cycle */
	protected void cpu() {
		int opcode = code[ip];
		int a,b,addr,offset;
		float x,y;
		while (opcode!= HALT && ip < code.length) {
			if ( trace ) System.err.printf("%-35s", disInstr());
			ip++; //jump to next instruction or to operand
			switch (opcode) {
				case IADD:
					b = stack[sp--];   			// 2nd opnd at top of stack
					a = stack[sp--]; 			// 1st opnd 1 below top
					stack[++sp] = a + b;      	// push result
					break;
				case ISUB:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a - b;
					break;
				case IMUL:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a * b;
					break;
				case ILT :
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a < b) ? TRUE : FALSE;
					break;
				case IEQ :
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a == b) ? TRUE : FALSE;
					break;

				case FADD:
					y = Float.intBitsToFloat(stack[sp--]);
					x = Float.intBitsToFloat(stack[sp--]);
					stack[++sp] = Float.floatToIntBits(x + y);
					break;
				case FSUB:
					y = Float.intBitsToFloat(stack[sp--]);
					x = Float.intBitsToFloat(stack[sp--]);
					stack[++sp] = Float.floatToIntBits(x -y);
					break;
				case FMUL:
					y = Float.intBitsToFloat(stack[sp--]);
					x = Float.intBitsToFloat(stack[sp--]);
					stack[++sp] = Float.floatToIntBits(x * y);
					break;
				case FLT :
					y = Float.intBitsToFloat(stack[sp--]);
					x = Float.intBitsToFloat(stack[sp--]);
					stack[++sp] = (x < y) ? TRUE : FALSE;
					break;
				case FEQ :
					y = Float.intBitsToFloat(stack[sp--]);
					x = Float.intBitsToFloat(stack[sp--]);
					stack[++sp] = (x == y) ? TRUE : FALSE;
					break;
				case FPRINT :
					System.out.println(Float.intBitsToFloat(stack[sp--]));
					break;

				case BR :
					ip = code[ip++];
					break;
				case BRT :
					addr = code[ip++];
					if ( stack[sp--]==TRUE ) ip = addr;
					break;
				case BRF :
					addr = code[ip++];
					if ( stack[sp--]==FALSE ) ip = addr;
					break;
				case FCONST: // same as ICONST!!
				case ICONST:
					stack[++sp] = code[ip++]; // push operand
					break;
				case LOAD : // load local or arg; 1st local is fp+1, args are fp-3, fp-4, fp-5, ...
					offset = code[ip++];
					stack[++sp] = stack[fp+offset];
					break;
				case GLOAD :// load from global memory
					addr = code[ip++];
					stack[++sp] = globals[addr];
					break;
				case STORE :
					offset = code[ip++];
					stack[fp+offset] = stack[sp--];
					break;
				case GSTORE :
					addr = code[ip++];
					globals[addr] = stack[sp--];
					break;
				case PRINT :
					System.out.println(stack[sp--]);
					break;
				case POP:
					--sp;
					break;
				default :
					throw new Error("invalid opcode: "+opcode+" at ip="+(ip-1));
			}
			if ( trace ) System.err.println(stackString());
			opcode = code[ip];
		}
		if ( trace ) System.err.printf("%-35s", disInstr());
		if ( trace ) System.err.println(stackString());
		if ( trace ) dumpDataMemory();
	}

	protected String stackString() {
		StringBuilder buf = new StringBuilder();
		buf.append("stack=[");
		for (int i = 0; i <= sp; i++) {
			int o = stack[i];
			buf.append(" ");
			buf.append(o);
		}
		buf.append(" ]");
		return buf.toString();
	}

	protected String disInstr() {
		int opcode = code[ip];
		String opName = Bytecode.instructions[opcode].name;
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("%04d:\t%-11s", ip, opName));
		int nargs = Bytecode.instructions[opcode].n;
		if ( nargs>0 ) {
			List<String> operands = new ArrayList<String>();
			for (int i=ip+1; i<=ip+nargs; i++) {
				operands.add(String.valueOf(code[i]));
			}
			for (int i = 0; i<operands.size(); i++) {
				String s = operands.get(i);
				if ( i>0 ) buf.append(", ");
				buf.append(s);
			}
		}
		return buf.toString();
	}

	protected void dumpDataMemory() {
		System.err.println("Data memory:");
		int addr = 0;
		for (int o : globals) {
			System.err.printf("%04d: %s\n", addr, o);
			addr++;
		}
		System.err.println();
	}

	protected void dumpCodeMemory() {
		System.err.println("Code memory:");
		int addr = 0;
		for (int o : code) {
			System.err.printf("%04d: %d\n", addr, o);
			addr++;
		}
		System.err.println();
	}
}
