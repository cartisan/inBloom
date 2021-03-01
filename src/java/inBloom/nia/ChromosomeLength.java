package inBloom.nia;

public class ChromosomeLength {
	public Integer value;

	public ChromosomeLength(Integer value) {
		this.value = value;
	}

	public boolean equals(ChromosomeLength other) {
		if (other != null) {
			return this.value.equals(other.value);
		}
		return false;
	}

	@Override
	public ChromosomeLength clone() {
		return new ChromosomeLength(new Integer(this.value));
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
