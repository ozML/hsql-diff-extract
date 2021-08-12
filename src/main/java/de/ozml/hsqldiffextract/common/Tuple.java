package de.ozml.hsqldiffextract.common;

public class Tuple<T, S> {

	public T v1;
	public S v2;

	public Tuple(T v1, S v2) {
		this.setV1(v1);
		this.setV2(v2);
	}

	public T getV1() {
		return v1;
	}

	public void setV1(T v1) {
		this.v1 = v1;
	}

	public S getV2() {
		return v2;
	}

	public void setV2(S v2) {
		this.v2 = v2;
	}

}