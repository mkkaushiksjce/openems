package io.openems.edge.ess.sinexcel;

import java.util.Arrays;
import java.util.stream.Stream;

import io.openems.edge.common.channel.AbstractReadChannel;
import io.openems.edge.common.channel.FloatReadChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.StateCollectorChannel;
import io.openems.edge.common.channel.doc.ChannelId;
import io.openems.edge.common.channel.doc.Doc;
import io.openems.edge.common.channel.doc.Unit;
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
					
//----------------------------------------------------------------------------------------------------------------------
		
					case SETDATA_GridOnCmd:
					case SETDATA_GridOffCmd:
					case SETDATA_ModOffCmd:
					case SETDATA_ModOnCmd:
					case SUNSPEC_DID_0103:
					case DC_Voltage:
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
					
					
					case Sinexcel_State:
					case EVENT_1:
					
				
					case Test_Register:
					case Max_Discharge_Current:
					case Max_Charge_Current:
					case Target_Active_Power:
					case Target_Reactive_Power:

		
						return new IntegerReadChannel(ess, channelId);
					case Start:
					case Stop:
					case SET_CHARGE_DISCHARGE_ACTIVE:
					case SET_CHARGE_DISCHARGE_REACTIVE:
					case SET_CHARGE_CURRENT:
					case SET_DISCHARGE_CURRENT:
					case SET_SLOW_CHARGE_VOLTAGE:
					case SET_FLOAT_CHARGE_VOLTAGE:
					case SET_UPPER_VOLTAGE:
					case SET_LOWER_VOLTAGE:
						return new IntegerWriteChannel(ess, channelId);
//-----------------------------------STATES--------------------------------------------------
					case Sinexcel_STATE_1:
					case Sinexcel_STATE_2:
					case Sinexcel_STATE_3:
					case Sinexcel_STATE_4:
					case Sinexcel_STATE_5:
					case Sinexcel_STATE_6:
					case Sinexcel_STATE_7:
					case Sinexcel_STATE_8:
					case Sinexcel_STATE_9:	
						return new StateChannel(ess, channelId);
//-----------------------------------EVENT BITFIELD32-----------------------------------------						
					case STATE_0:
					case STATE_1:
					case STATE_2:
					case STATE_3:
					case STATE_4:
					case STATE_5:
					case STATE_6:
					case STATE_7:
					case STATE_8:
					case STATE_9:
					case STATE_10:
					case STATE_11:
					case STATE_12:
					case STATE_13:
					case STATE_14:
					case STATE_15:
						return new StateChannel(ess, channelId);
					}
					return null;
				}) //
		).flatMap(channel -> channel);
	}
}
