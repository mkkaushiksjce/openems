/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.impl.controller.symmetric.addedvalue;

import java.util.ArrayList;
import java.util.List;

import io.openems.api.channel.ConfigChannel;
import io.openems.api.controller.Controller;
import io.openems.api.doc.ChannelInfo;
import io.openems.api.doc.ThingInfo;
import io.openems.api.exception.InvalidValueException;

@ThingInfo(title = "(Symmetric)", description = "")
public class AddedValueController extends Controller {

	/*
	 * Constructors
	 */
	public AddedValueController() {
		super();
	}

	public AddedValueController(String thingId) {
		super(thingId);
	}

	/*
	 * Config
	 */
	@ChannelInfo(title = "Ess", description = "Sets the Ess device.", type = Ess.class)
	public ConfigChannel<Ess> ess = new ConfigChannel<Ess>("ess", this);

	@ChannelInfo(title = "Grid-Meter", description = "Sets the grid meter.", type = Meter.class)
	public ConfigChannel<Meter> gridMeter = new ConfigChannel<Meter>("gridMeter", this);

	@ChannelInfo(title = "PV-Meter", description = "Sets the PV meter.", type = Meter.class)
	public ConfigChannel<Meter> pvmeter = new ConfigChannel<Meter>("pvmeter", this);

	/*
	 * Methods
	 */
	@Override
	public void run() {
		try {
			Ess ess = this.ess.value();

			long pvproduction = pvmeter.value().activePower.value();
			long essproduction = ess.allowedDischarge.value();
			long essconsumption = ess.allowedCharge.value();
			long gridproduction = 0;
			long gridconsumption = 0;
			if (gridMeter.value().activePower.value() > 0) {
				gridproduction = gridMeter.value().activePower.value();
			} else {
				gridconsumption = gridMeter.value().activePower.value();
			}
			long loadvalue = gridproduction + pvproduction + essproduction - essconsumption - gridconsumption;

			Pv pv = new Pv(new ValueInterval(0, pvproduction, 10));
			Load load = new Load(new ValueInterval(loadvalue, loadvalue, Integer.MAX_VALUE));
			Battery battery = new Battery(new ValueInterval(0, essproduction, 20),
					new ValueInterval(0, essconsumption, 15));
			Grid grid = new Grid(new ValueInterval(0, Long.MAX_VALUE, 28), new ValueInterval(0, Long.MAX_VALUE, 13));

			List<Consumer> consumers = new ArrayList<>();
			consumers.add(battery);
			consumers.add(grid);
			consumers.add(load);

			List<Producer> producers = new ArrayList<>();
			producers.add(battery);
			producers.add(pv);
			producers.add(grid);

			Optimizer.optimize(producers, consumers);

			System.out.println("produce:");
			for (Producer producer : producers) {
				System.out.println(producer.getClass().getSimpleName() + " produces " + producer.getProduction());
			}

			System.out.println("consume:");
			for (Consumer consumer : consumers) {
				System.out.println(consumer.getClass().getSimpleName() + " consumes " + consumer.getConsumption());
			}

		} catch (InvalidValueException e) {
			log.error(e.getMessage());
		}
	}

}
