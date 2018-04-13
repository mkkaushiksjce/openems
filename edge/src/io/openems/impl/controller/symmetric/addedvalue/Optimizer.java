package io.openems.impl.controller.symmetric.addedvalue;

import java.util.List;

public class Optimizer {
	public static void optimize(List<Producer> producers, List<Consumer> consumers) {
		consumers.sort((Consumer s1, Consumer s2) -> s2.revenue().value() - s1.revenue().value());
		producers.sort((Producer s1, Producer s2) -> s1.cost().value() - s2.cost().value());
		long Prev = 0;

		for (Consumer consumer : consumers) {
			for (Producer producer : producers) {
				if (consumer.revenue().value() >= producer.cost().value()) {
					System.out.println("Comparing Intervals[" + consumer.revenue().IntervalFrom() + ","
							+ consumer.revenue().IntervalTo() + "]" + "[" + producer.cost().IntervalFrom() + ","
							+ producer.cost().IntervalTo() + "]");
					long Min = Math.min(consumer.revenue().IntervalTo(), producer.cost().IntervalTo());
					long ConsumerintervalFrom = consumer.revenue().IntervalFrom();
					long ConsumerintervalTo = consumer.revenue().IntervalTo();
					long ProducerintervalTo = producer.cost().IntervalTo();

					if (ConsumerintervalFrom > 0) {
						ConsumerintervalFrom -= Min;
					}

					if (ConsumerintervalTo <= 0) {
						System.out.println("No requirement from consumer");
						continue;
					}

					if (ProducerintervalTo <= 0) {
						System.out.println("No Production Available");
						continue;
					}

					ConsumerintervalTo -= Min;
					ProducerintervalTo -= Min;
					consumer.revenue().setIntervalFrom(ConsumerintervalFrom);
					consumer.revenue().setIntervalTo(ConsumerintervalTo);
					producer.cost().setIntervalTo(ProducerintervalTo);
					long total = Prev + Min;
					Prev = total;
					consumer.setConsumption(total);
					producer.setProduction(Min);
					System.out.println("Taking Maximum of: " + Min + " From: " + producer.getClass().getSimpleName()
							+ " For:" + producer.cost().value() + " Euro-cent");
					System.out.println("New Intervals[" + consumer.revenue().IntervalFrom() + ","
							+ consumer.revenue().IntervalTo() + "]" + "[" + producer.cost().IntervalFrom() + ","
							+ producer.cost().IntervalTo() + "]");

				}
			}
		}
	}
}
