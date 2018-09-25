package io.openems.edge.ess.core.power;

import java.util.List;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanReadChannel;
import io.openems.edge.common.channel.doc.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.power.api.Coefficient;
import io.openems.edge.ess.power.api.Constraint;
import io.openems.edge.ess.power.api.Phase;
import io.openems.edge.ess.power.api.Power;
import io.openems.edge.ess.power.api.PowerException;
import io.openems.edge.ess.power.api.Pwr;
import io.openems.edge.ess.power.api.Relationship;

@Designate(ocd = Config.class, factory = false)
@Component( //
		name = "ESS.Power", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL, //
		property = { //
				"id=_power", //
				"enabled=true", //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE //
		})
public class PowerComponent extends AbstractOpenemsComponent implements OpenemsComponent, EventHandler, Power {

	public enum ChannelId implements io.openems.edge.common.channel.doc.ChannelId {
		/**
		 * Whether the Power problem could be solved
		 * 
		 * <ul>
		 * <li>Interface: PowerComponent
		 * <li>Type: Boolean
		 * </ul>
		 */
		SOLVED(new Doc().type(OpenemsType.BOOLEAN));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}

	private final Logger log = LoggerFactory.getLogger(PowerComponent.class);

	protected final static boolean DEFAULT_SYMMETRIC_MODE = false;
	protected final static boolean DEFAULT_DEBUG_MODE = false;

	private final Data data;
	private final Solver solver;

	private boolean debugMode = PowerComponent.DEFAULT_DEBUG_MODE;

	public PowerComponent() {
		Utils.initializeChannels(this).forEach(channel -> this.addChannel(channel));
		this.data = new Data();
		this.solver = new Solver(data);

		this.solver.onSolved(wasSolved -> {
			this.getSolvedChannel().setNextValue(wasSolved);
		});
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, "_power", "_power", true);
		this.data.setSymmetricMode(config.symmetricMode());
		this.debugMode = config.debugMode();
		this.solver.setDebugMode(config.debugMode());
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Reference( //
			policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY, //
			cardinality = ReferenceCardinality.MULTIPLE, //
			target = "(enabled=true)")
	protected synchronized void addEss(ManagedSymmetricEss ess) {
		this.data.addEss(ess);
	}

	protected synchronized void removeEss(ManagedSymmetricEss ess) {
		this.data.removeEss(ess);
	}

	@Override
	public synchronized Constraint addConstraint(Constraint constraint) {
		this.data.addConstraint(constraint);
		return constraint;
	}

	@Override
	public synchronized Constraint addConstraintAndValidate(Constraint constraint) throws PowerException {
		this.data.addConstraint(constraint);
		try {
			this.solver.isSolvableOrError();
		} catch (PowerException e) {
			this.data.removeConstraint(constraint);
			if (this.debugMode) {
				List<Constraint> allConstraints = this.data.getConstraintsForAllInverters();
				PowerComponent.debugLogConstraints(this.log, "Unable to validate with following constraints:",
						allConstraints);
				this.log.info("Failed to add Constraint: " + constraint);
			}
			e.setReason(constraint);
			throw e;
		}
		return constraint;
	}

	/*
	 * Helpers to create Constraints
	 */
	@Override
	public Coefficient getCoefficient(ManagedSymmetricEss ess, Phase phase, Pwr pwr) {
		return this.data.getCoefficient(ess, phase, pwr);
	}

	@Override
	public Constraint createSimpleConstraint(String description, ManagedSymmetricEss ess, Phase phase, Pwr pwr,
			Relationship relationship, double value) {
		return this.data.createSimpleConstraint(description, ess, phase, pwr, relationship, value);
	}

	@Override
	public void removeConstraint(Constraint constraint) {
		this.data.removeConstraint(constraint);
	}

	@Override
	public int getMaxPower(ManagedSymmetricEss ess, Phase phase, Pwr pwr) {
		return this.solver.getActivePowerExtrema(ess, phase, pwr, GoalType.MAXIMIZE);
	}

	@Override
	public int getMinPower(ManagedSymmetricEss ess, Phase phase, Pwr pwr) {
		return this.solver.getActivePowerExtrema(ess, phase, pwr, GoalType.MINIMIZE);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE:
			this.solver.solve();
			break;
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE:
			this.data.initializeCycle();
			break;
		}
	}

	protected BooleanReadChannel getSolvedChannel() {
		return this.channel(ChannelId.SOLVED);
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Prints all Constraints to the system log
	 * 
	 * @param title
	 * @param constraints
	 */
	public static void debugLogConstraints(Logger log, String title, List<Constraint> constraints) {
		log.info(title);
		for (Constraint c : constraints) {
			log.info("- " + c);
		}
	}
}
