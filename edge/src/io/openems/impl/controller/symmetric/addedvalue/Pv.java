package io.openems.impl.controller.symmetric.addedvalue;



public class Pv implements Producer {

	private ValueInterval cost;
	private long production;

	public Pv(ValueInterval cost) {
		this.cost = cost;
	}

	@Override
	public ValueInterval cost() {

		return this.cost;
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
