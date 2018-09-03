package io.openems.edge.ess.sinexcel;

import java.util.Optional;

import org.osgi.service.cm.ConfigurationAdmin;
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

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.doc.Doc;
import io.openems.edge.common.channel.doc.Level;
import io.openems.edge.common.channel.doc.OptionsEnum;
import io.openems.edge.common.channel.doc.Unit;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.CircleConstraint;
import io.openems.edge.ess.power.api.Power;


@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Ess.Sinexcel", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE) //
//
public class EssSinexcel extends AbstractOpenemsModbusComponent
		implements SymmetricEss, ManagedSymmetricEss, EventHandler, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(EssSinexcel.class);

	public static final int DEFAULT_UNIT_ID = 1;

	@Reference
	protected ConfigurationAdmin cm;

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.service_pid(), config.id(), config.enabled(), DEFAULT_UNIT_ID, this.cm, "Modbus",
				config.modbus_id());

	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	public EssSinexcel() {
		Utils.initializeChannels(this).forEach(channel -> this.addChannel(channel));
	}

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus); // Bridge Modbus
	}

	public enum ChannelId implements io.openems.edge.common.channel.doc.ChannelId {
		SUNSPEC_DID_0103(new Doc()),//
		
		Start(new Doc().options(RequestedState.values())),// 
		Stop(new Doc().options(RequestedState.values())),//

		SETDATA_ModOnCmd(new Doc().unit(Unit.ON_OFF)), //
		SETDATA_ModOffCmd(new Doc().unit(Unit.ON_OFF)),//
		
		SETDATA_GridOnCmd(new Doc().unit(Unit.ON_OFF)),// 
		SETDATA_GridOffCmd(new Doc().unit(Unit.ON_OFF)),//
		
		SET_CHARGE_DISCHARGE_ACTIVE(new Doc().unit(Unit.KILOWATT_HOURS)),//
		SET_CHARGE_DISCHARGE_REACTIVE(new Doc().unit(Unit.KILO_VOLT_AMPERE_REACTIVE)),//
		
		SET_CHARGE_CURRENT(new Doc().unit(Unit.AMPERE)),
		SET_DISCHARGE_CURRENT(new Doc().unit(Unit.AMPERE)),
		
		SET_SLOW_CHARGE_VOLTAGE(new Doc().unit(Unit.VOLT)),
		SET_FLOAT_CHARGE_VOLTAGE(new Doc().unit(Unit.VOLT)),
		
		SET_UPPER_VOLTAGE(new Doc().unit(Unit.VOLT)),
		SET_LOWER_VOLTAGE(new Doc().unit(Unit.VOLT)),

		SET_CLEAR_FAILURE(new Doc().unit(Unit.NONE)),

		
	
		
		Frequency(new Doc().unit(Unit.HERTZ).level(Level.INFO).text("/100")),//
		Temperature(new Doc().unit(Unit.DEGREE_CELSIUS)),//
		
		
		AC_Apparent_Power(new Doc().unit(Unit.VOLT_AMPERE)),//
		AC_Reactive_Power(new Doc().unit(Unit.VOLT_AMPERE_REACTIVE)),//
		AC_Power(new Doc().unit(Unit.WATT)),// 
		
		InvOutVolt_L1(new Doc() .unit(Unit.VOLT)),//
		InvOutVolt_L2(new Doc() .unit(Unit.VOLT)),//
		InvOutVolt_L3(new Doc().unit(Unit.VOLT)),//
		InvOutCurrent_L1(new Doc().unit(Unit.AMPERE).level(Level.INFO).text("/10")),//
		InvOutCurrent_L2(new Doc() .unit(Unit.AMPERE).level(Level.INFO).text("/10")),//
		InvOutCurrent_L3(new Doc().unit(Unit.AMPERE).level(Level.INFO).text("/10")),//

		DC_Power(new Doc().unit(Unit.KILO_WATT).level(Level.INFO).text("/100")),//
		DC_Current(new Doc().unit(Unit.AMPERE).level(Level.INFO).text("/10")),//
		DC_Voltage(new Doc().unit(Unit.VOLT).level(Level.INFO).text("/10")),//


		EVENT_1(new Doc().unit(Unit.NONE)),//
		Sinexcel_State(new Doc().unit(Unit.NONE)),//
		
		Test_Register(new Doc().unit(Unit.NONE)),
		
		Target_Active_Power(new Doc() .unit(Unit.KILO_WATT).text("/10")),//
		Target_Reactive_Power(new Doc().unit(Unit.KILO_WATT).text("/10")),//
		Max_Charge_Current(new Doc() .unit(Unit.AMPERE).text("/10")),
		Max_Discharge_Current(new Doc() .unit(Unit.AMPERE).text("/10")),
//---------------------------------------STATES------------------------------------------------		
		Sinexcel_STATE_1(new Doc().level(Level.INFO).text("OFF")),//
		Sinexcel_STATE_2(new Doc().level(Level.INFO).text("Sleeping")),//
		Sinexcel_STATE_3(new Doc().level(Level.INFO).text("Starting")),//
		Sinexcel_STATE_4(new Doc().level(Level.INFO).text("MPPT")),//
		Sinexcel_STATE_5(new Doc().level(Level.INFO).text("Throttled")),//
		Sinexcel_STATE_6(new Doc().level(Level.INFO).text("Shutting down")),//
		Sinexcel_STATE_7(new Doc().level(Level.INFO).text("Fault")),//
		Sinexcel_STATE_8(new Doc().level(Level.INFO).text("Standby")),//
		Sinexcel_STATE_9(new Doc().level(Level.INFO).text("Started")),//
		
//-----------------------------------EVENT Bitfield 32-----------------------------------
		STATE_0(new Doc().level(Level.FAULT).text("Ground fault")),//
		STATE_1(new Doc().level(Level.WARNING).text("DC over Voltage")),//
		STATE_2(new Doc().level(Level.WARNING).text("AC disconnect open")),//
		STATE_3(new Doc().level(Level.WARNING).text("DC disconnect open")),//
		STATE_4(new Doc().level(Level.WARNING).text("Grid shutdown")),//
		STATE_5(new Doc().level(Level.WARNING).text("Cabinet open")),//
		STATE_6(new Doc().level(Level.WARNING).text("Manual shutdown")),//
		STATE_7(new Doc().level(Level.WARNING).text("Over temperature")),//
		STATE_8(new Doc().level(Level.WARNING).text("AC Frequency above limit")),//
		STATE_9(new Doc().level(Level.WARNING).text("AC Frequnecy under limit")),//
		STATE_10(new Doc().level(Level.WARNING).text("AC Voltage above limit")),//
		STATE_11(new Doc().level(Level.WARNING).text("AC Voltage under limit")),//
		STATE_12(new Doc().level(Level.WARNING).text("Blown String fuse on input")),//
		STATE_13(new Doc().level(Level.WARNING).text("Under temperature")), //
		STATE_14(new Doc().level(Level.WARNING).text("Generic Memory or Communication error (internal)")),//
		STATE_15(new Doc().level(Level.FAULT).text("Hardware test failure"));//
//---------------------------------------------------------------------------------------------------------------
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;

		}
	}
//------------------------------------------START AND STOP-------------------------------------------------
	public void startSystem() {
		IntegerWriteChannel SETDATA_GridOnCmd = this.channel(ChannelId.Start);
		IntegerWriteChannel SETDATA_ModOnCmd = this.channel(ChannelId.Start);
		try {
			SETDATA_ModOnCmd.setNextWriteValue(RequestedState.ON.value);
			SETDATA_GridOnCmd.setNextWriteValue(RequestedState.ON.value);

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to start inverter" + e.getMessage());
		}

	}

	public void stopSystem() {
		IntegerWriteChannel SETDATA_ModOffCmd = this.channel(ChannelId.Stop);
		IntegerWriteChannel SETDATA_GridOffCmd = this.channel(ChannelId.Stop);

		try {
			SETDATA_GridOffCmd.setNextWriteValue(RequestedState.ON.value);
			SETDATA_ModOffCmd.setNextWriteValue(RequestedState.ON.value);
		} catch (OpenemsException e) {
			log.error("problem occurred while trying to stop system" + e.getMessage());
		}
	}

	public enum RequestedState implements OptionsEnum {
		// directly addressable states
		OFF(0, "Off"), ON(1, "ON");

		int value;
		String option;

		private RequestedState(int value, String option) {
			this.value = value;
			this.option = option;
		}

		@Override
		public int getValue() {
			return value;
		}

		@Override
		public String getOption() {
			return option;
		}
	}

	public void doHandling_ON() {
		startSystem();
	}

	public void doHandling_OFF() {
		stopSystem();
	}

	
//---------------------------------------------------CHARGE AND DISCHARGE-------------------------------------------
	public void SET_CHARGE_DISCHARGE() {
		
		IntegerWriteChannel SET_Active = this.channel(ChannelId.SET_CHARGE_DISCHARGE_ACTIVE);
		IntegerWriteChannel SET_Reactive = this.channel(ChannelId.SET_CHARGE_DISCHARGE_REACTIVE);
		try {
			SET_Active.setNextWriteValue(ACTIVE);
			SET_Reactive.setNextWriteValue(REACTIVE);
			

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to write the charge value" + e.getMessage());
		}

	}
	
	public void doHandling_CHARGE_DISCHARGE() {
		SET_CHARGE_DISCHARGE();
	}
//-----------------------------------------------------MAX CHARGE AND DISCHARGE CURRENT-----------------------------------------------
	
	public void SET_CHARGE_DISCHARGE_CURRENT() {
		
		IntegerWriteChannel SET_DISCHARGE_CURRENT = this.channel(ChannelId.SET_DISCHARGE_CURRENT);
		IntegerWriteChannel SET_CHARGE_CURRENT = this.channel(ChannelId.SET_CHARGE_CURRENT);
		try {
			SET_DISCHARGE_CURRENT.setNextWriteValue(DISCHARGE_CURRENT);
			SET_CHARGE_CURRENT.setNextWriteValue(CHARGE_CURRENT);
			

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to write the charge and discharge current value" + e.getMessage());
		}

	}
	
	public void doHandling_CHARGE_DISCHARGE_CURRENT() {
		SET_CHARGE_DISCHARGE_CURRENT();
	}
	
	
//---------------------------------------------SET UPPER/LOWER BATTERY VOLTAGE --------------------------------------------	
	public void SET_UPPER_LOWER_BATTERY_VOLTAGE() {
		
		IntegerWriteChannel SET_LOWER_VOLTAGE = this.channel(ChannelId.SET_LOWER_VOLTAGE);
		IntegerWriteChannel SET_UPPER_VOLTAGE = this.channel(ChannelId.SET_UPPER_VOLTAGE);
		IntegerWriteChannel SET_SLOW_CHARGE_VOLTAGE = this.channel(ChannelId.SET_SLOW_CHARGE_VOLTAGE);
		IntegerWriteChannel SET_FLOAT_CHARGE_VOLTAGE = this.channel(ChannelId.SET_FLOAT_CHARGE_VOLTAGE);
		try {
			SET_UPPER_VOLTAGE.setNextWriteValue(UPPER_BAT_VOLTAGE);
			SET_LOWER_VOLTAGE.setNextWriteValue(LOWER_BAT_VOLTAGE);
			SET_SLOW_CHARGE_VOLTAGE.setNextWriteValue(SLOW_CHARGE_VOLTAGE);
			SET_FLOAT_CHARGE_VOLTAGE.setNextWriteValue(FLOAT_CHARGE_VOLTAGE);
			

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to write the voltage limits" + e.getMessage());
		}

	}
	
	public void doHandling_UPPER_LOWER_VOLTAGE() {
		SET_UPPER_LOWER_BATTERY_VOLTAGE();
	}
//--------------------------------------------SET CLEAR FAILURE-------------------------------------------------------------------------------
	public void SET_CLEAR_FAILURE_CMD() {
	IntegerWriteChannel SET_CLEAR_FAILURE = this.channel(ChannelId.SET_CLEAR_FAILURE);
	try {
		SET_CLEAR_FAILURE.setNextWriteValue(CLEAR_FAILURE);
	} catch (OpenemsException e) {
		log.error("problem occurred while trying to write the clear failure command" + e.getMessage());
	}
}

	public void doHandling_CLEAR_FAILURE() {
		SET_CLEAR_FAILURE_CMD();
	}
	
//------------------------------------------------------------------------------------------------------------------	
	
	protected ModbusProtocol defineModbusProtocol(int unitId) {
		return new ModbusProtocol(unitId, //
//------------------------------------------------------------WRITE-----------------------------------------------------------
				new FC6WriteRegisterTask(0x028A, 
						m(EssSinexcel.ChannelId.Start, new UnsignedWordElement(0x028A))), // Start// SETDATA_ModOnCmd				
																													

				new FC6WriteRegisterTask(0x028B, 
						m(EssSinexcel.ChannelId.Stop, new UnsignedWordElement(0x028B))), // Stop// SETDATA_ModOffCmd
																													
				new FC6WriteRegisterTask(0x028D, 
						m(EssSinexcel.ChannelId.Start, new UnsignedWordElement(0x028D))), // Start// SETDATA_GridOnCmd
				
				new FC6WriteRegisterTask(0x028E, 
						m(EssSinexcel.ChannelId.Stop, new UnsignedWordElement(0x028E))), // Stop// SETDATA_GridOffCmd
				
				new FC6WriteRegisterTask(0x0087, 
						m(EssSinexcel.ChannelId.SET_CHARGE_DISCHARGE_ACTIVE, new SignedWordElement(0x0087))), // Target ACTIVE Power //Line65
				new FC6WriteRegisterTask(0x0088, 
						m(EssSinexcel.ChannelId.SET_CHARGE_DISCHARGE_REACTIVE, new SignedWordElement(0x0088))), // Target ACTIVE Power //Line65
				
				new FC6WriteRegisterTask(0x032B, 
						m(EssSinexcel.ChannelId.SET_CHARGE_CURRENT, new UnsignedWordElement(0x032B))), // MAX_CHARGING_CURRENT //Line217
				new FC6WriteRegisterTask(0x032C, 
						m(EssSinexcel.ChannelId.SET_DISCHARGE_CURRENT, new UnsignedWordElement(0x032C))), // MAX_DISCHARGING_CURRENT //Line218
				
				new FC6WriteRegisterTask(0x0329, 
						m(EssSinexcel.ChannelId.SET_SLOW_CHARGE_VOLTAGE, new UnsignedWordElement(0x0329))), // Slow charge Voltage // Line215 
				new FC6WriteRegisterTask(0x0328, 
						m(EssSinexcel.ChannelId.SET_FLOAT_CHARGE_VOLTAGE, new UnsignedWordElement(0x0328))),
				
				new FC6WriteRegisterTask(0x032E, 
						m(EssSinexcel.ChannelId.SET_UPPER_VOLTAGE, new UnsignedWordElement(0x032E))), // Upper voltage limit of battery protection //Line220
				new FC6WriteRegisterTask(0x032D, 
						m(EssSinexcel.ChannelId.SET_LOWER_VOLTAGE, new UnsignedWordElement(0x032D))), // LOWER voltage limit of battery protection //Line219
				
				new FC6WriteRegisterTask(0x028C, 
						m(EssSinexcel.ChannelId.SET_CLEAR_FAILURE, new UnsignedWordElement(0x028C))), // Clear Failure Command //Line219 // uint16
																													
//----------------------------------------------------------READ------------------------------------------------------
//				new FC3ReadRegistersTask(0x024A, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Frequency, new SignedWordElement(0x024A))),						// int16	//Line 132 // Magnification = 100
//				
//				new FC3ReadRegistersTask(0x0084, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Temperature, new SignedWordElement(0x0084))), 					// int16 // Line 62	Magnification = 0
			

				
				new FC3ReadRegistersTask(0x0260, Priority.HIGH,
						m(EssSinexcel.ChannelId.Sinexcel_State, new UnsignedWordElement(0x0260))),
				

				
//----------------------------------------------------------START and STOP--------------------------------------------------------------------				
				new FC3ReadRegistersTask(0x023A, Priority.LOW, //
						m(EssSinexcel.ChannelId.SUNSPEC_DID_0103, new UnsignedWordElement(0x023A))), //

				new FC3ReadRegistersTask(0x028A, Priority.HIGH,
						m(EssSinexcel.ChannelId.SETDATA_ModOnCmd, new UnsignedWordElement(0x028A))),

				new FC3ReadRegistersTask(0x028B, Priority.HIGH,
						m(EssSinexcel.ChannelId.SETDATA_ModOffCmd, new UnsignedWordElement(0x028B))),

				new FC3ReadRegistersTask(0x028D, Priority.HIGH,
						m(EssSinexcel.ChannelId.SETDATA_GridOnCmd, new UnsignedWordElement(0x028D))),

				new FC3ReadRegistersTask(0x028E, Priority.HIGH,
						m(EssSinexcel.ChannelId.SETDATA_GridOffCmd, new UnsignedWordElement(0x028E))),
				

//-----------------------------------------DC Parameter-----------------------------------------------------------------
				new FC3ReadRegistersTask(0x008D, Priority.HIGH,
						m(EssSinexcel.ChannelId.DC_Power, new SignedWordElement(0x008D))),				// int16 // Line69 // Magnification = 100
				
				new FC3ReadRegistersTask(0x0255, Priority.HIGH,
						m(EssSinexcel.ChannelId.DC_Current, new SignedWordElement(0x0255))),			// int16 // Line142 // Magnification = 10
				
				new FC3ReadRegistersTask(0x0257, Priority.HIGH, //
						m(EssSinexcel.ChannelId.DC_Voltage, new UnsignedWordElement(0x0257))), 			// NennSpannung // uint16 // Line144 // Magnification = 10
				
//-----------------------------------------AC Parameter-----------------------------------------------------------------
//				new FC3ReadRegistersTask(0x0065, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L1, new UnsignedWordElement(0x0065))), 				//	uint16 // Line36 // Magnification = 10//				
//				new FC3ReadRegistersTask(0x0066, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L2, new UnsignedWordElement(0x0066))),				 //	uint16 // Line37 // Magnification = 10//
//				new FC3ReadRegistersTask(0x0067, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L3, new UnsignedWordElement(0x0067))), 				 //	uint16 // Line38 // Magnification = 10
//				
				new FC3ReadRegistersTask(0x0068, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L1, new UnsignedWordElement(0x0068))), 			// uint16 // Line39// Magnification = 10																			
				new FC3ReadRegistersTask(0x0069, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L2, new UnsignedWordElement(0x0069))), 			// uint16 // Line40// Magnification= 10												
				new FC3ReadRegistersTask(0x006A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L3, new UnsignedWordElement(0x006A))),			// uint16 // Line41// Magnification= 10
				
				new FC3ReadRegistersTask(0x0248, Priority.HIGH, //
				m(EssSinexcel.ChannelId.AC_Power, new SignedWordElement(0x0248))), 						//	int16 // Line130 // Magnification = 0
				
//				new FC3ReadRegistersTask(0x024C, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.AC_Apparent_Power, new SignedWordElement(0x024C))), 			//	int16 // Line134 // Magnification = 0//
//
//						new FC3ReadRegistersTask(0x024E, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.AC_Reactive_Power, new SignedWordElement(0x024E))), 			// int16 // Line136 // Magnification = 0


//-----------------------------------------EVENT Bitfield 32------------------------------------------------------------		
				new FC3ReadRegistersTask(0x0262, Priority.LOW, //
						bm(new UnsignedWordElement(0x0262)) //
								.m(EssSinexcel.ChannelId.STATE_0, 0) //
								.m(EssSinexcel.ChannelId.STATE_1, 1) //
								.m(EssSinexcel.ChannelId.STATE_2, 2) //
								.m(EssSinexcel.ChannelId.STATE_3, 3) //
								.m(EssSinexcel.ChannelId.STATE_4, 4) //
								.m(EssSinexcel.ChannelId.STATE_5, 5) //
								.m(EssSinexcel.ChannelId.STATE_6, 6) //
								.m(EssSinexcel.ChannelId.STATE_7, 7) //
								.m(EssSinexcel.ChannelId.STATE_8, 8) //
								.m(EssSinexcel.ChannelId.STATE_9, 9) //
								.m(EssSinexcel.ChannelId.STATE_10, 10) //
								.m(EssSinexcel.ChannelId.STATE_11, 11) //
								.m(EssSinexcel.ChannelId.STATE_12, 12) //
								.m(EssSinexcel.ChannelId.STATE_13, 13) //
								.m(EssSinexcel.ChannelId.STATE_14, 14) //
								.m(EssSinexcel.ChannelId.STATE_15, 15) //
								.build()), //
//---------------------------------------------STATES---------------------------------------------------------
				new FC3ReadRegistersTask(0x0260, Priority.LOW, //
						bm(new UnsignedWordElement(0x0260)) //
								 
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_1, 1) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_2, 2) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_3, 3) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_4, 4) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_5, 5) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_6, 6) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_7, 7) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_8, 8) //
								.m(EssSinexcel.ChannelId.Sinexcel_STATE_9, 9) //
								.build()), //
//----------------------------------------------------GENERAL SETTINGS--------------------------------------------------------------
				new FC3ReadRegistersTask(0x0087, Priority.LOW, //
						m(EssSinexcel.ChannelId.Target_Active_Power, new SignedWordElement(0x0087))), 		// int 16 // Line65 // Magnification = 0																				
				new FC3ReadRegistersTask(0x0088, Priority.LOW, //
						m(EssSinexcel.ChannelId.Target_Reactive_Power, new SignedWordElement(0x0088))),
				
				new FC3ReadRegistersTask(0x032C, Priority.LOW, //
						m(EssSinexcel.ChannelId.Max_Discharge_Current, new UnsignedWordElement(0x032C))),	// uint 16 // Line217 // Magnifiaction = 10
				
				new FC3ReadRegistersTask(0x032B, Priority.LOW, //
						m(EssSinexcel.ChannelId.Max_Charge_Current, new UnsignedWordElement(0x032B))),					// uint 16 // Line217 // Magnifiaction = 10
				
				new FC3ReadRegistersTask(0x028C, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Test_Register, new UnsignedWordElement(0x028C)))				// TESTOBJEKT
		);
	

	}
//------------------------------------------------------------------------------------------------------------------------
	private void initializePower() {					//Begrenzungen eingeben
		this.maxApparentPowerConstraint = new CircleConstraint(this, MAX_ACTIVE_POWER);
	}
	
	
	@Override
	public void applyPower(int activePower, int reactivePower) {
		
		IntegerWriteChannel SET_ACTIVE_POWER = this.channel(ChannelId.SET_CHARGE_DISCHARGE_ACTIVE);
		IntegerWriteChannel SET_REACTIVE_POWER = this.channel(ChannelId.SET_CHARGE_DISCHARGE_REACTIVE);
		
		int reactiveValue = (int)((reactivePower/100));
		if((reactiveValue > MAX_REACTIVE_POWER) || (reactiveValue < (MAX_REACTIVE_POWER*(-1)))) {
			reactiveValue = 0;
			log.error("Reactive power limit exceeded");
		}
		
		int activeValue = (int) ((activePower/100));
		if((activeValue > MAX_ACTIVE_POWER) || (activeValue < (MAX_ACTIVE_POWER*(-1)))) {
			activeValue = 0;
			log.error("Active power limit exceeded");
		}
		
		try {
			SET_REACTIVE_POWER.setNextWriteValue(reactiveValue);
			SET_ACTIVE_POWER.setNextWriteValue(activeValue);
		}
			    catch (OpenemsException e) {
				log.error("EssKacoBlueplanetGridsave50.applyPower(): Problem occurred while trying so set active power" + e.getMessage());
			    }
	}

	
	
/*
 * Example: Value 3000 means 300; Value 3001 means 300,1
 */
	private CircleConstraint maxApparentPowerConstraint = null;
	
	private int MAX_REACTIVE_POWER = 300;	// 30 kW
	private int MAX_ACTIVE_POWER = 300;	// 30 kW
	
	
	int CLEAR_FAILURE = 1;				// 1 = true // 0 = false
	
	int SLOW_CHARGE_VOLTAGE = 3500;		// Slow and Float Charge Voltage must be the same for the Lithium Ion battery. 
	int FLOAT_CHARGE_VOLTAGE = 3500;		
	
	int LOWER_BAT_VOLTAGE = 3000;
	int UPPER_BAT_VOLTAGE = 3900;
	
	int CHARGE_CURRENT = 900;			// [CHARGE_CURRENT] = A // Range = 0 A ... 90 A
	int DISCHARGE_CURRENT = 900;			// [DISCHARGE_CURRENT] = A	// Range = 0 A ... 90 A
	int ACTIVE = 0;					// [ACTIVE] = kW	// Range = -30 kW ... 30 kW	// ACTIVE < 0 -> CHARGE //	ACTIVE > 0 ->DISCHARGE 
	int REACTIVE = 0;					// [REACTIVE] = kVAr	// Range = -30 kW ... 30 //REACTIVE < 0 -> inductive // REACTIVE > 0 -> capacitive 
	
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
//			doHandling_OFF();
			doHandling_CLEAR_FAILURE();
			doHandling_UPPER_LOWER_VOLTAGE();
			doHandling_CHARGE_DISCHARGE_CURRENT();
			doHandling_CHARGE_DISCHARGE();
			break;
		}
	}

	@Override
	public Power getPower() {					//Siehe KACO
		// TODO Auto-generated method stub
		return null;
	}

	@Override								// Leistungsstufen des Wechselrichters
	public int getPowerPrecision() {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
