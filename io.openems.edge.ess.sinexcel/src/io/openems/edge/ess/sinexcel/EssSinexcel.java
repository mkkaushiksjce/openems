package io.openems.edge.ess.sinexcel;

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
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.LongWriteChannel;
import io.openems.edge.common.channel.doc.Doc;
import io.openems.edge.common.channel.doc.Level;
import io.openems.edge.common.channel.doc.OptionsEnum;
import io.openems.edge.common.channel.doc.Unit;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.SymmetricEss;


@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Ess.Sinexcel", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE) //
//
public class EssSinexcel extends AbstractOpenemsModbusComponent
		implements SymmetricEss, EventHandler, OpenemsComponent {

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

		SOC(new Doc().unit(Unit.PERCENT)),//
		ACTIVE_POWER(new Doc()//
				.type(OpenemsType.INTEGER)//
				.unit(Unit.WATT)//
				.text(POWER_DOC_TEXT)),//
		REACTIVE_POWER(new Doc()//
				.type(OpenemsType.INTEGER)//
				.unit(Unit.VOLT_AMPERE_REACTIVE)//
				.text(POWER_DOC_TEXT)),//
		
		Analog_Active_Power_3Phase(new Doc().unit(Unit.KILO_WATT)),//
		Analog_Reactive_Power_3Phase(new Doc().unit(Unit.KILO_VOLT_AMPERE_REACTIVE)),//
		
		Frequency(new Doc().unit(Unit.HERTZ)),//
		Temperature(new Doc().unit(Unit.DEGREE_CELSIUS)),//
		
		Analog_DC_Current(new Doc().unit(Unit.AMPERE)),//
		AC_Apparent_Power(new Doc().unit(Unit.VOLT_AMPERE)),//
		AC_Reactive_Power(new Doc().unit(Unit.VOLT_AMPERE_REACTIVE)),//
		AC_Power(new Doc().unit(Unit.WATT)),// 
		
		InvOutVolt_L1(new Doc() .unit(Unit.VOLT)),//
		InvOutVolt_L2(new Doc() .unit(Unit.VOLT)),//
		InvOutVolt_L3(new Doc().unit(Unit.VOLT)),//
		InvOutCurrent_L1(new Doc().unit(Unit.AMPERE)),//
		InvOutCurrent_L2(new Doc() .unit(Unit.AMPERE)),//
		InvOutCurrent_L3(new Doc().unit(Unit.AMPERE)),//

		DC_Power(new Doc().unit(Unit.KILO_WATT)),//
		DC_Current(new Doc().unit(Unit.AMPERE)),//
//		DC_Current_2(new Doc().unit(Unit.AMPERE)),//
		DC_Voltage(new Doc().unit(Unit.NEW_VOLT)),//
		Analog_DC_Power(new Doc().unit(Unit.WATT).text(POWER_DOC_TEXT)),//

		EVENT_1(new Doc().unit(Unit.NONE)),//
		EVENT_2(new Doc().unit(Unit.NONE)),//
		Vendor_EVENT_1(new Doc().unit(Unit.NONE)), //
		Vendor_EVENT_2(new Doc().unit(Unit.NONE)),//
		Vendor_EVENT_3(new Doc().unit(Unit.NONE)),// 
		Vendor_EVENT_4(new Doc().unit(Unit.NONE)),//

		Vendor_State(new Doc().unit(Unit.NONE)),// 
		State(new Doc().unit(Unit.NONE)),//

		Analog_DC_Discharge_Energy(new Doc().unit(Unit.KILOWATT_HOURS)),
		Analog_DC_Charge_Energy(new Doc().unit(Unit.KILOWATT_HOURS)),
		
		Slow_Charging_Voltage(new Doc().unit(Unit.VOLT)),
		
		Target_Active_Power(new Doc() .unit(Unit.KILO_WATT)),//
		Max_Charge_Current(new Doc() .unit(Unit.AMPERE)),
		Max_Discharge_Current(new Doc() .unit(Unit.AMPERE));
		
//-----------------------------------EVENT Bitfield 32-----------------------------------
//		STATE_0(new Doc().level(Level.FAULT).text("Ground fault")),//
//		STATE_1(new Doc().level(Level.WARNING).text("DC over Voltage")),//
//		STATE_2(new Doc().level(Level.WARNING).text("AC Disconnect open")),//
//		STATE_3(new Doc().level(Level.WARNING).text("DC disconnect open")),//
//		STATE_4(new Doc().level(Level.WARNING).text("Grid shutdown")),//
//		STATE_5(new Doc().level(Level.WARNING).text("Cabinet open")),//
//		STATE_6(new Doc().level(Level.WARNING).text("Manual shutdown")),//
//		STATE_7(new Doc().level(Level.WARNING).text("Over temperature")),//
//		STATE_8(new Doc().level(Level.WARNING).text("AC Frequency above limit")),//
//		STATE_9(new Doc().level(Level.WARNING).text("AC Frequnecy under limit")),//
//		STATE_10(new Doc().level(Level.WARNING).text("AC Voltage above limit")),//
//		STATE_11(new Doc().level(Level.WARNING).text("AC Voltage under limit")),//
//		STATE_12(new Doc().level(Level.WARNING).text("Blown String fuse on input")),//
//		STATE_13(new Doc().level(Level.WARNING).text("Under temperature")), //
//		STATE_14(new Doc().level(Level.WARNING).text("Generic Memory or Communication error (internal)")),//
//		STATE_15(new Doc().level(Level.FAULT).text("Hardware test failure"));//
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
		int ACTIVE = -20;//ACTIVE < 0 -> CHARGE;	ACTIVE > 0 ->DISCHARGE
		int REACTIVE = 0;
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
	
	public void SET_CHARGE_CURRENT() {
		int SET_CHARGE_CURRENT = 20;
		IntegerWriteChannel SET = this.channel(ChannelId.SET_CHARGE_CURRENT);
		try {
			SET.setNextWriteValue(SET_CHARGE_CURRENT);
			

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to write the charge current value" + e.getMessage());
		}

	}
	
	public void SET_DISCHARGE_CURRENT() {
		int SET_DISCHARGE_CURRENT = 0;
		IntegerWriteChannel SET = this.channel(ChannelId.SET_DISCHARGE_CURRENT);
		try {
			SET.setNextWriteValue(SET_DISCHARGE_CURRENT);
			

		} catch (OpenemsException e) {
			log.error("problem occurred while trying to write the charge current value" + e.getMessage());
		}

	}
	
	public void doHandling_DISCHARGE_CURRENT() {
		SET_DISCHARGE_CURRENT();
	}
	
	public void doHandling_CHARGE_CURRENT() {
		SET_CHARGE_CURRENT();
	}
	
//------------------------------------------------------------------------------------------------------------------	
	
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			doHandling_OFF();
			doHandling_DISCHARGE_CURRENT();
			doHandling_CHARGE_CURRENT();
			doHandling_CHARGE_DISCHARGE();
			break;
		}
	}
	
	
	
	
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
																													
//----------------------------------------------------------READ------------------------------------------------------
//				new FC3ReadRegistersTask(0x024A, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Frequency, new SignedWordElement(0x024A))),						// int16	//Line 132 // Magnification = 100
//				
//				new FC3ReadRegistersTask(0x0084, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Temperature, new SignedWordElement(0x0084))), 					// int16 // Line 62	Magnification = 0
			
				new FC3ReadRegistersTask(0x00B8, Priority.HIGH, //
						m(EssSinexcel.ChannelId.SOC, new UnsignedWordElement(0x00B8))), // uint16 // Line91 // Magnification = 10
				
				new FC3ReadRegistersTask(0x0260, Priority.HIGH,
						m(EssSinexcel.ChannelId.State, new UnsignedWordElement(0x0260))),
				
//				new FC3ReadRegistersTask(0x0261, Priority.HIGH, 
//				m(EssSinexcel.ChannelId.Vendor_State, new UnsignedWordElement(0x0261))),
				
//				new FC3ReadRegistersTask(0x007A, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.Analog_Active_Power_3Phase, new SignedWordElement(0x007A))), 	// KiloWatt //int16 // Line55
//		
//		new FC3ReadRegistersTask(0x007B, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.Analog_Reactive_Power_3Phase, new SignedWordElement(0x007B))), 	//KiloVAR // int16 // Line56
				
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
				

							
//-----------------------------------------EVENT TESTING-------------------------------------------------------------
//				new FC3ReadRegistersTask(0x0262, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.EVENT_1, new SignedDoublewordElement(0x0262))),

//				new FC3ReadRegistersTask(0x0264, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.EVENT_2, new SignedDoublewordElement(0x0264))),
//				
//				new FC3ReadRegistersTask(0x0266, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.Vendor_EVENT_1, new SignedDoublewordElement(0x0266))),
//				
//				new FC3ReadRegistersTask(0x0268, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.Vendor_EVENT_2, new SignedDoublewordElement(0x0268))),
//				
//				new FC3ReadRegistersTask(0x026A, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.Vendor_EVENT_3, new SignedDoublewordElement(0x026A))),
//				
//				new FC3ReadRegistersTask(0x026C, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.Vendor_EVENT_4, new SignedDoublewordElement(0x026C))),

//-----------------------------------------DC Parameter-----------------------------------------------------------------
				new FC3ReadRegistersTask(0x008D, Priority.HIGH,
						m(EssSinexcel.ChannelId.DC_Power, new SignedWordElement(0x008D))),				// Magnification = 100
				
				new FC3ReadRegistersTask(0x0255, Priority.HIGH,
						m(EssSinexcel.ChannelId.DC_Current, new UnsignedWordElement(0x0255))),
				
//				new FC3ReadRegistersTask(0x00AA, Priority.HIGH,
//						m(EssSinexcel.ChannelId.DC_Current_2, new UnsignedWordElement(0x00AA))),			// uint 16 // Line77 // Magnification = ?
				
				new FC3ReadRegistersTask(0x0257, Priority.HIGH, //
						m(EssSinexcel.ChannelId.DC_Voltage, new UnsignedWordElement(0x0257))), 			// NennSpannung // uint16 // Line144 // Magnification = 100
				
//				new FC3ReadRegistersTask(0x008D, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.Analog_DC_Power, new SignedWordElement(0x008D))), 				// int16 // Line69 // Magnification = 100
				
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
				
//				new FC3ReadRegistersTask(0x0248, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.AC_Power, new SignedWordElement(0x0248))), 						//	int16 // Line130 // Magnification = 0
				
//				new FC3ReadRegistersTask(0x024C, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.AC_Apparent_Power, new SignedWordElement(0x024C))), 			//	int16 // Line134 // Magnification = 0//

				//		new FC3ReadRegistersTask(0x024E, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.AC_Reactive_Power, new SignedWordElement(0x024E))), 			// int16 // Line136 // Magnification = 0

//				new FC3ReadRegistersTask(0x008F, Priority.HIGH, //
//				m(EssSinexcel.ChannelId.Analog_DC_Current, new UnsignedWordElement(0x008F))),					// uint64 // Line95

//-----------------------------------------EVENT Bitfield 32------------------------------------------------------------		
//				new FC3ReadRegistersTask(0x0262, Priority.LOW, //
//						bm(new UnsignedWordElement(0x0262)) //
//								.m(EssSinexcel.ChannelId.STATE_0, 0) //
//								.m(EssSinexcel.ChannelId.STATE_1, 1) //
//								.m(EssSinexcel.ChannelId.STATE_2, 2) //
//								.m(EssSinexcel.ChannelId.STATE_3, 3) //
//								.m(EssSinexcel.ChannelId.STATE_4, 4) //
//								.m(EssSinexcel.ChannelId.STATE_5, 5) //
//								.m(EssSinexcel.ChannelId.STATE_6, 6) //
//								.m(EssSinexcel.ChannelId.STATE_7, 7) //
//								.m(EssSinexcel.ChannelId.STATE_8, 8) //
//								.m(EssSinexcel.ChannelId.STATE_9, 9) //
//								.m(EssSinexcel.ChannelId.STATE_10, 10) //
//								.m(EssSinexcel.ChannelId.STATE_11, 11) //
//								.m(EssSinexcel.ChannelId.STATE_12, 12) //
//								.m(EssSinexcel.ChannelId.STATE_13, 13) //
//								.m(EssSinexcel.ChannelId.STATE_14, 14) //
//								.m(EssSinexcel.ChannelId.STATE_15, 15) //
//								.build()) //
				
//----------------------------------------------------GENERAL SETTINGS--------------------------------------------------------------
				new FC3ReadRegistersTask(0x0087, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Target_Active_Power, new UnsignedDoublewordElement(0x0087))), 		// uint 32 // Line72 // Magnification = 0																				
				
				new FC3ReadRegistersTask(0x0090, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_DC_Charge_Energy, new UnsignedDoublewordElement(0x0090))),
				
				new FC3ReadRegistersTask(0x0092, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_DC_Discharge_Energy, new UnsignedDoublewordElement(0x0092))),
				
				new FC3ReadRegistersTask(0x032A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Slow_Charging_Voltage, new UnsignedWordElement(0x032A))),				// TESTOBJEKT
				
				new FC3ReadRegistersTask(0x032B, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Max_Charge_Current, new UnsignedWordElement(0x032B))),					// uint 16 // Line217 // Magnifiaction = 10
				
				new FC3ReadRegistersTask(0x032C, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Max_Discharge_Current, new UnsignedWordElement(0x032C)))				// uint 16 // Line217 // Magnifiaction = 10
				
		);
	

	}

}
