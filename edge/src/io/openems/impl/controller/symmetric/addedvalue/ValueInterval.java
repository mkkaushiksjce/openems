package io.openems.impl.controller.symmetric.addedvalue;

public class ValueInterval {
	private long intervalFrom;
	private long intervalTo;
	private final int value;

	public ValueInterval(long intervalFrom, Long intervalTo, int value) {
		this.intervalFrom = intervalFrom;
		this.intervalTo = intervalTo;
		this.value = value;
	}

	public void setIntervalFrom(long intervalFrom) {
		this.intervalFrom = intervalFrom;
	}

	public void setIntervalTo(long intervalTo) {
		this.intervalTo = intervalTo;
	}

	public long IntervalFrom() {
		return this.intervalFrom;
	}

	public long IntervalTo() {
		return this.intervalTo;
	}

	public int value() {
		return this.value;
	}

	@Override
	public String toString() {
		return "Interval[" + this.intervalFrom + "," + this.intervalTo + "," + this.value + "]";
	}
}
