package io.openems.impl.controller.symmetric.addedvalue;

public interface Producer {

	public ValueInterval cost();

	public void setProduction(long production);

	public long getProduction();

}
