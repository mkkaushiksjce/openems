package io.openems.impl.controller.symmetric.addedvalue;


public class Load implements Consumer {

	private ValueInterval revenue;
	private long consumption;

	public Load(ValueInterval revenue) {
		this.revenue = revenue;
	}

	@Override
	public ValueInterval revenue() {

		return this.revenue;
	}

	@Override
	public void setConsumption(long Consumption) {
		this.consumption = Consumption;

	}

	@Override
	public long getConsumption() {

		return this.consumption;
	}

}
