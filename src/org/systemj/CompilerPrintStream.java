package org.systemj;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class CompilerPrintStream extends PrintStream {


	public CompilerPrintStream(OutputStream out) {
		super(out);
	}

	private static boolean v = false;
	private static boolean vv = false;

	public static void setVerbose() { v = true; }
	public static void setDefaultVerbose() { vv = true; }
	public static void resetVerbose() { v = vv; }


	@Override
	public void println(Object x){
		if(v)
			super.println(x);
	}

	@Override
	public void println(String x){
		if(v)
			super.println(x);
	}

	@Override
	public void println(int x){
		if(v)
			super.println(x);
	}
	@Override
	public void write(int b) {
		if(v)
			super.write(b);
	}
	@Override
	public void write(byte[] buf, int off, int len) {
		if(v)
			super.write(buf, off, len);
	}
	@Override
	public void print(boolean b) {
		if(v)
			super.print(b);
	}
	@Override
	public void print(char c) {
		if(v)
			super.print(c);
	}
	@Override
	public void print(int i) {
		if(v)
			super.print(i);
	}
	@Override
	public void print(long l) {
		if(v)
			super.print(l);
	}
	@Override
	public void print(float f) {
		if(v)
			super.print(f);
	}
	@Override
	public void print(double d) {
		if(v)
			super.print(d);
	}
	@Override
	public void print(char[] s) {
		if(v)
			super.print(s);
	}
	@Override
	public void print(String s) {
		if(v)
			super.print(s);
	}
	@Override
	public void print(Object obj) {
		if(v)
			super.print(obj);
	}
	@Override
	public void println() {
		if(v)
			super.println();
	}
	@Override
	public void println(boolean x) {
		if(v)
			super.println(x);
	}
	@Override
	public void println(char x) {
		if(v)
			super.println(x);
	}
	@Override
	public void println(long x) {
		if(v)
			super.println(x);
	}
	@Override
	public void println(float x) {
		if(v)
			super.println(x);
	}
	@Override
	public void println(double x) {
		if(v)
			super.println(x);
	}
	@Override
	public void println(char[] x) {
		if(v)
			super.println(x);
	}
}
