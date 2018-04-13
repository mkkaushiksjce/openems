package io.openems.impl.controller.symmetric.addedvalue;

public interface Consumer {

	public ValueInterval revenue();

	public void setConsumption(long Consumption);

	public long getConsumption();

}
