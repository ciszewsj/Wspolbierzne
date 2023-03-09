package ee.ciszewsj.c2java;

import lombok.Data;

@Data
public class Matrix {
	private final int cols;
	private final int rows;
	private final float[] _data;

	public Matrix(int r, int c) {
		this.cols = c;
		this.rows = r;
		_data = new float[c * r];
	}

	public float get(int r, int c) {
		return _data[r * cols + c];
	}

	public void set(int r, int c, float v) {
		_data[r * cols + c] = v;
	}

	public int rows() {
		return rows;
	}

	public int cols() {
		return cols;
	}
}
