package net.q00p.bots.util;

public class Tuple<T1, T2> {
	T1 first;
	T2 second;

	public Tuple(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Tuple))
			return false;
		Tuple t = (Tuple) o;
		return first.equals(t.first) && second.equals(t.second);
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + first.hashCode();
		result = 37 * result + second.hashCode();
		return result;
	}

	public String toString() {
		return "{" + first + ", " + second + "}";
	}
}