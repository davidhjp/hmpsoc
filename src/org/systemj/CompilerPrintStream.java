package org.systemj;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class CompilerPrintStream extends PrintStream {


	public CompilerPrintStream(OutputStream out) {
		super(out);
	}

	private static boolean v = false;

	public static void setVerbose() { v = true; }
	public static void unsetVerbose() { v = false; }


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
		// TODO Auto-generated method stub
		if(v)
			super.print(b);
	}
	@Override
	public void print(char c) {
		// TODO Auto-generated method stub
		if(v)
			super.print(c);
	}
	@Override
	public void print(int i) {
		// TODO Auto-generated method stub
		if(v)
			super.print(i);
	}
	@Override
	public void print(long l) {
		// TODO Auto-generated method stub
		if(v)
			super.print(l);
	}
	@Override
	public void print(float f) {
		// TODO Auto-generated method stub
		if(v)
			super.print(f);
	}
	@Override
	public void print(double d) {
		// TODO Auto-generated method stub
		if(v)
			super.print(d);
	}
	@Override
	public void print(char[] s) {
		// TODO Auto-generated method stub
		if(v)
			super.print(s);
	}
	@Override
	public void print(String s) {
		// TODO Auto-generated method stub
		if(v)
			super.print(s);
	}
	@Override
	public void print(Object obj) {
		// TODO Auto-generated method stub
		if(v)
			super.print(obj);
	}
	@Override
	public void println() {
		// TODO Auto-generated method stub
		if(v)
			super.println();
	}
	@Override
	public void println(boolean x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
	@Override
	public void println(char x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
	@Override
	public void println(long x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
	@Override
	public void println(float x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
	@Override
	public void println(double x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
	@Override
	public void println(char[] x) {
		// TODO Auto-generated method stub
		if(v)
			super.println(x);
	}
}
