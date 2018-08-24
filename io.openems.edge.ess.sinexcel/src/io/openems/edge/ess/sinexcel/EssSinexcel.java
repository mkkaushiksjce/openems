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
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.doc.Doc;
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
public class EssSinexcel extends AbstractOpenemsModbusComponent implements SymmetricEss, EventHandler, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(EssSinexcel.class);

	public static final int DEFAULT_UNIT_ID = 1;
	
	@Reference
	protected ConfigurationAdmin cm;
	
	@Activate void activate(ComponentContext context, Config config) {
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
		super.setModbus(modbus);						//Bridge Modbus
	}
	public enum ChannelId implements io.openems.edge.common.channel.doc.ChannelId {
		SUNSPEC_DID_0103(new Doc()), //
		Start(new Doc().options(RequestedState.values())),
		Stop(new Doc().options(RequestedState.values())),
		
		SETDATA_ModOnCmd(new Doc()
				.unit(Unit.ON_OFF)),
		SETDATA_ModOffCmd(new Doc()
				.unit(Unit.ON_OFF)),
		SETDATA_GridOnCmd(new Doc()
				.unit(Unit.ON_OFF)),
		SETDATA_GridOffCmd(new Doc()
				.unit(Unit.ON_OFF)),
		
		SOC(new Doc()
				.unit(Unit.PERCENT)),
		DC_Voltage(new Doc() //
				.unit(Unit.NEW_VOLT)), //
		Analog_DC_Power(new Doc() //
				.unit(Unit.WATT) //
				.text(POWER_DOC_TEXT)),
		ACTIVE_POWER(new Doc() //
				.type(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.text(POWER_DOC_TEXT)), //
		REACTIVE_POWER(new Doc() //
				.type(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.text(POWER_DOC_TEXT)),
		Analog_Active_Power_3Phase(new Doc()
				.unit(Unit.KILO_WATT)),
		Analog_Reactive_Power_3Phase(new Doc()
				.unit(Unit.KILO_VOLT_AMPERE_REACTIVE)),
		AC_Power(new Doc()
				.unit(Unit.WATT)),
		Frequency(new Doc()
				.unit(Unit.HERTZ)),
		Temperature(new Doc()
				.unit(Unit.DEGREE_CELSIUS)),
		AC_Apparent_Power(new Doc()
				.unit(Unit.VOLT_AMPERE)),
		AC_Reactive_Power(new Doc()
				.unit(Unit.VOLT_AMPERE_REACTIVE)),
		
		InvOutVolt_L1(new Doc() //
				.unit(Unit.VOLT)),
		InvOutVolt_L2(new Doc() //
				.unit(Unit.VOLT)),
		InvOutVolt_L3(new Doc() //
				.unit(Unit.VOLT)),
		InvOutCurrent_L1(new Doc() //
				.unit(Unit.AMPERE)),
		InvOutCurrent_L2(new Doc() //
				.unit(Unit.AMPERE)),
		InvOutCurrent_L3(new Doc() //
				.unit(Unit.AMPERE)),
		
		DC_Power(new Doc()
				.unit(Unit.KILO_WATT)),
		DC_Current(new Doc()
				.unit(Unit.AMPERE)),
		Analog_DC_Current(new Doc()
				.unit(Unit.AMPERE)),
		
		EVENT_1(new Doc()
				.unit(Unit.NONE)),
		EVENT_2(new Doc()
				.unit(Unit.NONE)),
		Vendor_EVENT_1(new Doc()
				.unit(Unit.NONE)),
		Vendor_EVENT_2(new Doc()
				.unit(Unit.NONE)),
		Vendor_EVENT_3(new Doc()
				.unit(Unit.NONE)),
		Vendor_EVENT_4(new Doc()
				.unit(Unit.NONE)),
		
		Vendor_State(new Doc()
				.unit(Unit.NONE)),
		State(new Doc()
				.unit(Unit.NONE))
		;
		
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		
	
	}
}	
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
	
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
				doHandling_ON();
			break;
		}
	}
	
	
	protected ModbusProtocol defineModbusProtocol(int unitId) {
		return new ModbusProtocol(unitId, //
//------------------------------------------------------------WRITE-----------------------------------------------------------
				new FC6WriteRegisterTask(0x028A,									
						m(EssSinexcel.ChannelId.Start,
								new UnsignedWordElement(0x028A))),		// Start SETDATA_ModOnCmd
				new FC6WriteRegisterTask(0x028D,									
						m(EssSinexcel.ChannelId.Start,
								new UnsignedWordElement(0x028D))),		// Start SETDATA_GridOnCmd
				
				
				new FC6WriteRegisterTask(0x028B,									
						m(EssSinexcel.ChannelId.Stop,
								new UnsignedWordElement(0x028B))),		// Stop SETDATA_ModOffCmd
				new FC6WriteRegisterTask(0x028D,									
						m(EssSinexcel.ChannelId.Stop,
								new UnsignedWordElement(0x028D))),		// Stop SETDATA_GridOffCmd
			
//----------------------------------------------------------READ--------------------------------------------------------------------				
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
				
//				new FC3ReadRegistersTask(0x008D, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Analog_DC_Power, new SignedWordElement(0x008D))), 				// int16 // Line69 // Magnification = 100
//				
//				new FC3ReadRegistersTask(0x024A, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Frequency, new SignedWordElement(0x024A))),						// int16	//Line 132 // Magnification = 100
//				
//				new FC3ReadRegistersTask(0x0084, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Temperature, new SignedWordElement(0x0084))), 					// int16 // Line 62	Magnification = 0
//				
//				new FC3ReadRegistersTask(0x024C, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.AC_Apparent_Power, new SignedWordElement(0x024C))), 			//	int16 // Line134 // Magnification = 0
//				
//				new FC3ReadRegistersTask(0x024E, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.AC_Reactive_Power, new SignedWordElement(0x024E))), 			// int16 // Line136 // Magnification = 0
//				
				new FC3ReadRegistersTask(0x0257, Priority.HIGH, //
						m(EssSinexcel.ChannelId.DC_Voltage, new UnsignedWordElement(0x0257))),					// NennSpannung //	uint16 // Line144 // Magnification = 100
//				
//				new FC3ReadRegistersTask(0x007A, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Analog_Active_Power_3Phase, new SignedWordElement(0x007A))), 	// KiloWatt //int16 // Line55
//				
//				new FC3ReadRegistersTask(0x007B, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Analog_Reactive_Power_3Phase, new SignedWordElement(0x007B))), 	//KiloVAR // int16 // Line56
//				
//				new FC3ReadRegistersTask(0x0248, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.AC_Power, new SignedWordElement(0x0248))), 						//	int16 // Line130 // Magnification = 0
//				
//				new FC3ReadRegistersTask(0x0065, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L1, new UnsignedWordElement(0x0065))), 				//	uint16 // Line36 // Magnification = 10
//				
//				new FC3ReadRegistersTask(0x0066, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L2, new UnsignedWordElement(0x0066))),				 //	uint16 // Line37 // Magnification = 10
//							
//				new FC3ReadRegistersTask(0x0067, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.InvOutVolt_L3, new UnsignedWordElement(0x0067))), 				 //	uint16 // Line38 // Magnification = 10
//				
				new FC3ReadRegistersTask(0x0068, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L1, new UnsignedWordElement(0x0068))), 			 //	uint16 // Line39 // Magnification = 10
	
				new FC3ReadRegistersTask(0x0069, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L2, new UnsignedWordElement(0x0069))), 			 //	uint16 // Line40 // Magnification = 10
			
				new FC3ReadRegistersTask(0x006A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L3, new UnsignedWordElement(0x006A))),			 //	uint16 // Line41 // Magnification = 10
//				
				new FC3ReadRegistersTask(0x00B8, Priority.HIGH, //
						m(EssSinexcel.ChannelId.SOC, new UnsignedWordElement(0x00B8))), 							 //	uint16 // Line91 // Magnification = 10
//				
//				new FC3ReadRegistersTask(0x008F, Priority.HIGH, //
//						m(EssSinexcel.ChannelId.Analog_DC_Current, new UnsignedWordElement(0x008F))),					// uint64 // Line95
//
				new FC3ReadRegistersTask(0x0255, Priority.HIGH, 
						m(EssSinexcel.ChannelId.DC_Current, new UnsignedWordElement(0x0255))),
//				
//				new FC3ReadRegistersTask(0x0261, Priority.HIGH, 
//						m(EssSinexcel.ChannelId.Vendor_State, new UnsignedWordElement(0x0261))),
				
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
				
				new FC3ReadRegistersTask(0x008D, Priority.HIGH, 
						m(EssSinexcel.ChannelId.DC_Power, new SignedWordElement(0x008D))),
				
				new FC3ReadRegistersTask(0x0260, Priority.HIGH, 
						m(EssSinexcel.ChannelId.State, new UnsignedWordElement(0x0260)))
				
				
				);
				//Testing different parameters 8.22.18
				
				
				
	}


}
	
