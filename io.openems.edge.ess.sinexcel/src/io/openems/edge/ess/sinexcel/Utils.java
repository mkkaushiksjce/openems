package io.openems.edge.ess.sinexcel;

import java.util.Arrays;
import java.util.stream.Stream;

import io.openems.edge.common.channel.AbstractReadChannel;
import io.openems.edge.common.channel.FloatReadChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.StateCollectorChannel;
import io.openems.edge.common.channel.doc.ChannelId;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.ess.api.SymmetricEss;

public class Utils {
	public static Stream<? extends AbstractReadChannel<?>> initializeChannels(EssSinexcel ess) {
		// Define the channels. Using streams + switch enables Eclipse IDE to tell us if
		// we are missing an Enum value.
		return Stream.of( //
				Arrays.stream(OpenemsComponent.ChannelId.values()).map(channelId -> {
					switch (channelId) {
					case STATE:
						return new StateCollectorChannel(ess, channelId);
					}
					return null;
				}), Arrays.stream(SymmetricEss.ChannelId.values()).map(channelId -> {
					switch (channelId) {
					case SOC:
					case ACTIVE_POWER:
					case REACTIVE_POWER:
					case ACTIVE_CHARGE_ENERGY: // TODO ACTIVE_CHARGE_ENERGY
					case ACTIVE_DISCHARGE_ENERGY: // TODO ACTIVE_DISCHARGE_ENERGY
					case MAX_ACTIVE_POWER:
					case GRID_MODE:
						return new IntegerReadChannel(ess, channelId);
					}
					return null;
				}), Arrays.stream(EssSinexcel.ChannelId.values()).map(channelId -> {
					switch (channelId) {
					
					case SETDATA_GridOnCmd:
					case SETDATA_GridOffCmd:
					case SETDATA_ModOffCmd:
					case SETDATA_ModOnCmd:
					case SUNSPEC_DID_0103:
					case SOC:
					case Analog_DC_Power:
					case DC_Voltage:
					case ACTIVE_POWER:
					case REACTIVE_POWER:
					case Analog_Active_Power_3Phase:
					case Analog_Reactive_Power_3Phase:
					case AC_Power:
					case AC_Apparent_Power:
					case AC_Reactive_Power:	
					case Frequency:	
					case Temperature:
					case InvOutVolt_L1:
					case InvOutVolt_L2:
					case InvOutVolt_L3:
					case InvOutCurrent_L1:
					case InvOutCurrent_L2:
					case InvOutCurrent_L3:
					case DC_Current:
					case DC_Power:
					case Analog_DC_Current:
					case Vendor_State:
					case State:
					case EVENT_1:
					case EVENT_2:
					case Vendor_EVENT_1:
					case Vendor_EVENT_2:
					case Vendor_EVENT_3:
					case Vendor_EVENT_4:
						return new IntegerReadChannel(ess, channelId);
						
					case Start:
					case Stop:
						return new IntegerWriteChannel(ess, channelId);
				
					}
					return null;
				}) //
		).flatMap(channel -> channel);
	}
}
