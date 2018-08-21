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
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.channel.doc.Doc;
import io.openems.edge.common.channel.doc.Unit;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.SymmetricEss;

@Designate(ocd = Config.class, factory = true)
@Component( //
		name = "Ess.Sinexcel", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
) //
public class EssSinexcel extends AbstractOpenemsModbusComponent implements SymmetricEss, OpenemsComponent {

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
		
		SOC(new Doc()
				.unit(Unit.PERCENT)),
		
		DC_Voltage(new Doc() //
				.unit(Unit.VOLT)), //
		
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
				.unit(Unit.WATT)),
		Analog_Reactive_Power_3Phase(new Doc()
				.unit(Unit.WATT)),
		
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
		
	protected ModbusProtocol defineModbusProtocol(int unitId) {
		return new ModbusProtocol(unitId, //
				new FC3ReadRegistersTask(0x023A, Priority.LOW, //
						m(EssSinexcel.ChannelId.SUNSPEC_DID_0103, new UnsignedWordElement(0x023A))), //
				
				new FC3ReadRegistersTask(0x008D, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_DC_Power, new SignedWordElement(0x008D))), // int16 // Line69 // Magnification = 100
				
				new FC3ReadRegistersTask(0x024A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Frequency, new SignedWordElement(0x024))),	// int16	//Line 132 // Magnification = 100
				
				new FC3ReadRegistersTask(0x0084, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Temperature, new SignedWordElement(0x0084))), // int 16 // Line 62	Magnification = 0
				
				new FC3ReadRegistersTask(0x024C, Priority.HIGH, //
						m(EssSinexcel.ChannelId.AC_Apparent_Power, new SignedWordElement(0x024C))), //	int16 // Line134 // Magnification = 0
				
				new FC3ReadRegistersTask(0x024E, Priority.HIGH, //
						m(EssSinexcel.ChannelId.AC_Reactive_Power, new SignedWordElement(0x024E))), //	int16 // Line136 // Magnification = 0
				
				new FC3ReadRegistersTask(0x0257, Priority.HIGH, //
						m(EssSinexcel.ChannelId.DC_Voltage, new UnsignedWordElement(0x0257))),	//NennSpannung //	uint16 // Line144 // Magnification = 100
				
				new FC3ReadRegistersTask(0x007A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_Active_Power_3Phase, new SignedWordElement(0x007A))), // Kilo Watt //int16 // Line55
				
				new FC3ReadRegistersTask(0x007B, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_Reactive_Power_3Phase, new SignedWordElement(0x007B))), //int16 // Line56
				
				new FC3ReadRegistersTask(0x0248, Priority.HIGH, //
						m(EssSinexcel.ChannelId.AC_Power, new SignedWordElement(0x0248))), //	int16 // Line130 // Magnification = 0
				
				new FC3ReadRegistersTask(0x0065, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutVolt_L1, new UnsignedWordElement(0x0065))), //	uint16 // Line36 // Magnification = 10
				
				new FC3ReadRegistersTask(0x0066, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutVolt_L2, new UnsignedWordElement(0x0066))), //	uint16 // Line37 // Magnification = 10
				
				new FC3ReadRegistersTask(0x0067, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutVolt_L3, new UnsignedWordElement(0x0067))), //	uint16 // Line38 // Magnification = 10
				
				new FC3ReadRegistersTask(0x0068, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L1, new UnsignedWordElement(0x0068))), //	uint16 // Line39 // Magnification = 10
	
				new FC3ReadRegistersTask(0x0069, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L2, new UnsignedWordElement(0x0069))), //	uint16 // Line40 // Magnification = 10
				
				new FC3ReadRegistersTask(0x006A, Priority.HIGH, //
						m(EssSinexcel.ChannelId.InvOutCurrent_L3, new UnsignedWordElement(0x006A))), //	uint16 // Line41 // Magnification = 10
				
				new FC3ReadRegistersTask(0x00B8, Priority.HIGH, //
						m(EssSinexcel.ChannelId.SOC, new UnsignedWordElement(0x00B8))) //	uint16 // Line91 // Magnification = 10
				
				);
				//Testing different parameters 8.20.18
				
				
				
	}


}
	
