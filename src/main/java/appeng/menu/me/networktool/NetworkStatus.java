/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.menu.me.networktool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEItemKey;
import appeng.client.gui.me.networktool.NetworkStatusScreen;

/**
 * Contains statistics about an ME network and the machines that form it.
 *
 * @see NetworkStatusScreen
 */
public class NetworkStatus {

    private double averagePowerInjection;
    private double averagePowerUsage;
    private double storedPower;
    private double maxStoredPower;
    private double channelPower;
    private int channelsUsed;

    private List<MachineGroup> groupedMachines = Collections.emptyList();

    public static NetworkStatus fromGrid(IGrid grid) {
        IEnergyService eg = grid.getEnergyService();

        NetworkStatus status = new NetworkStatus();

        status.averagePowerInjection = eg.getAvgPowerInjection();
        status.averagePowerUsage = eg.getAvgPowerUsage();
        status.storedPower = eg.getStoredPower();
        status.maxStoredPower = eg.getMaxStoredPower();
        status.channelPower = eg.getChannelPowerUsage();
        status.channelsUsed = grid.getPathingService().getUsedChannels();

        // This is essentially a groupBy machineRepresentation + count, sum(idlePowerUsage)
        Map<AEItemKey, MachineGroup> groupedMachines = new HashMap<>();
        for (var machineClass : grid.getMachineClasses()) {
            for (IGridNode machine : grid.getMachineNodes(machineClass)) {
                var ais = machine.getVisualRepresentation();
                if (ais != null) {
                    MachineGroup group = groupedMachines.get(ais);
                    if (group == null) {
                        groupedMachines.put(ais, group = new MachineGroup(ais));
                    }

                    group.setCount(group.getCount() + 1);
                    group.setIdlePowerUsage(group.getIdlePowerUsage() + machine.getIdlePowerUsage());
                }
            }
        }
        status.groupedMachines = ImmutableList.copyOf(groupedMachines.values());

        return status;
    }

    public double getAveragePowerInjection() {
        return averagePowerInjection;
    }

    public double getAveragePowerUsage() {
        return averagePowerUsage;
    }

    public double getStoredPower() {
        return storedPower;
    }

    public double getMaxStoredPower() {
        return maxStoredPower;
    }

    public double getChannelPower() {
        return channelPower;
    }

    public int getChannelsUsed() {
        return channelsUsed;
    }

    /**
     * @return Machines grouped by their UI representation.
     */
    public List<MachineGroup> getGroupedMachines() {
        return groupedMachines;
    }

    /**
     * Reads a network status previously written using {@link #write(FriendlyByteBuf)}.
     */
    public static NetworkStatus read(FriendlyByteBuf data) {
        NetworkStatus status = new NetworkStatus();
        status.averagePowerInjection = data.readDouble();
        status.averagePowerUsage = data.readDouble();
        status.storedPower = data.readDouble();
        status.maxStoredPower = data.readDouble();
        status.channelPower = data.readDouble();
        status.channelsUsed = data.readVarInt();

        int count = data.readVarInt();
        ImmutableList.Builder<MachineGroup> machines = ImmutableList.builder();
        for (int i = 0; i < count; i++) {
            machines.add(MachineGroup.read(data));
        }
        status.groupedMachines = machines.build();

        return status;
    }

    /**
     * Writes the contents of this object to a packet buffer. Use {@link #read(FriendlyByteBuf)} to restore.
     */
    public void write(FriendlyByteBuf data) {
        data.writeDouble(averagePowerInjection);
        data.writeDouble(averagePowerUsage);
        data.writeDouble(storedPower);
        data.writeDouble(maxStoredPower);
        data.writeDouble(channelPower);
        data.writeVarInt(channelsUsed);
        data.writeVarInt(groupedMachines.size());
        for (MachineGroup machine : groupedMachines) {
            machine.write(data);
        }
    }

}
