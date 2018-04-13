package io.openems.impl.controller.symmetric.addedvalue;



public class Grid implements Producer, Consumer {

	private ValueInterval cost;
	private ValueInterval revenue;
	private long consumption;
	private long production;

	public Grid(ValueInterval cost, ValueInterval revenue) {
		this.cost = cost;
		this.revenue = revenue;
	}

	@Override
	public ValueInterval revenue() {

		return this.revenue;
	}

	@Override
	public ValueInterval cost() {

		return this.cost;
	}

	@Override
	public void setConsumption(long Consumption) {
		this.consumption = Consumption;

	}

	@Override
	public long getConsumption() {

		return this.consumption;
	}

	@Override
	public void setProduction(long production) {
		this.production = production;

	}

	@Override
	public long getProduction() {

		return this.production;
	}

}
