package io.openems.edge.ess.sinexcel;

import java.util.Arrays;
import java.util.stream.Stream;

import io.openems.edge.common.channel.AbstractReadChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.StateCollectorChannel;
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
					case SUNSPEC_DID_0103:
					case Analog_DC_Power:
					case Analog_DC_Voltage:
					case Analog_DC_Current:
					case ACTIVE_POWER:
					case REACTIVE_POWER:
						return new IntegerReadChannel(ess, channelId);
					}
					return null;
				}) //
		).flatMap(channel -> channel);
	}
}
