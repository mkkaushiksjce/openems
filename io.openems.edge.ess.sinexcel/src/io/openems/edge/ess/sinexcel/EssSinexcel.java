package io.openems.edge.ess.sinexcel;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
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
	
	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.service_pid(), config.id(), config.enabled(), DEFAULT_UNIT_ID, this.cm, "Modbus",
				config.modbus_id()); //
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}
	
	public EssSinexcel() {
		Utils.initializeChannels(this).forEach(channel -> this.addChannel(channel));
	}
	
	public enum ChannelId implements io.openems.edge.common.channel.doc.ChannelId {
		SUNSPEC_DID_0103(new Doc()), //
		Analog_DC_Voltage(new Doc() //
				.unit(Unit.VOLT)), //
		Analog_DC_Power(new Doc() //
				.unit(Unit.WATT) //
				.text(POWER_DOC_TEXT)),
		Analog_DC_Current(new Doc() //
				.unit(Unit.AMPERE)), //
		ACTIVE_POWER(new Doc() //
				.type(OpenemsType.INTEGER) //
				.unit(Unit.WATT) //
				.text(POWER_DOC_TEXT)), //
		REACTIVE_POWER(new Doc() //
				.type(OpenemsType.INTEGER) //
				.unit(Unit.VOLT_AMPERE_REACTIVE) //
				.text(POWER_DOC_TEXT)),
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
						m(EssSinexcel.ChannelId.Analog_DC_Power, new SignedWordElement(0x008D))),		//Magnification = 100
				
				new FC3ReadRegistersTask(0x008E, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_DC_Voltage, new UnsignedWordElement(0x008E))), 	//Magnification = 10 
				
				new FC3ReadRegistersTask(0x008F, Priority.HIGH, //
						m(EssSinexcel.ChannelId.Analog_DC_Current, new UnsignedWordElement(0x008D))), 	//Magnification = 10
				
				new FC3ReadRegistersTask(0x0087, Priority.HIGH, //
						m(SymmetricEss.ChannelId.ACTIVE_POWER, new SignedWordElement(0x0087))),			//Target_Active_Power, Magnification = 10
				
				new FC3ReadRegistersTask(0x0088, Priority.HIGH, //
						m(SymmetricEss.ChannelId.REACTIVE_POWER, new SignedWordElement(0x0088))),		//Target_Reactive_Power, Magnification = 10
				
				new FC3ReadRegistersTask(0x00B8, Priority.HIGH, //
						m(SymmetricEss.ChannelId.SOC, new UnsignedWordElement(0x00B8)))				//SOC
				
				);
	};
}
