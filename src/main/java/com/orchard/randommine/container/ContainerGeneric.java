package com.orchard.randommine.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGeneric extends Container
{
	public ContainerGeneric(InventoryPlayer player)
    {
		//this.addSlotToContainer(new Slot(tileEntity, 0, 80, 40));
        addPlayerSlots(player);
    }
	
	public void addPlayerSlots(InventoryPlayer player)
	{
		for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 109));
        }
        IInventory tableInventory = new InventoryBasic("Trash", false, 1) {
        	public void setInventorySlotContents(int index, ItemStack stack)
            {
        		
            }
        };
        this.addSlotToContainer(new Slot(tableInventory, 0, 80, 20));
	}
	
	public ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex)
	{
		Slot sourceSlot = (Slot)inventorySlots.get(sourceSlotIndex);
		sourceSlot.putStack(null);
		return null;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
}
