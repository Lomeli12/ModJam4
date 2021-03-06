package net.lomeli.ring.core.handler;

import java.util.List;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

import net.lomeli.ring.Rings;
import net.lomeli.ring.api.interfaces.IPlayerSession;
import net.lomeli.ring.core.helper.SimpleUtil;
import net.lomeli.ring.item.ItemMaterial;
import net.lomeli.ring.item.ModItems;
import net.lomeli.ring.lib.ModLibs;

public class GameEventHandler {
    private int tick;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent tick) {
        if (tick.side == Side.SERVER) {
            EntityPlayer player = tick.player;
            if (tick.phase == TickEvent.Phase.END) {
                if (Rings.proxy.manaHandler.playerHasSession(player)) {
                    IPlayerSession playerSession = Rings.proxy.manaHandler.getPlayerSession(player);
                    if (++this.tick >= ModLibs.RECHARGE_WAIT_TIME) {
                        if (!player.capabilities.isCreativeMode) {
                            if (player.getFoodStats().getFoodLevel() > 4)
                                playerSession.adjustMana(((player.getFoodStats().getFoodLevel() / 4)), false);
                        } else
                            playerSession.setMana(playerSession.getMaxMana());
                        Rings.proxy.manaHandler.updatePlayerSession(playerSession, player.getEntityWorld().provider.dimensionId);
                        this.tick = 0;
                    }
                }
            }
        }
    }

    private boolean isHammer(Item item) {
        if (item != null) {
            List<ItemStack> stackList = OreDictionary.getOres("hammer");
            if (stackList != null && !stackList.isEmpty()) {
                for (ItemStack stack : stackList) {
                    if (stack != null && stack.getItem() != null && stack.getItem() == item)
                        return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        IInventory craftMatrix = event.craftMatrix;
        ItemStack output = event.crafting;
        EntityPlayer player = event.player;
        NBTTagCompound tag = null;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack itemstack = craftMatrix.getStackInSlot(i);
            if (itemstack != null && itemstack.getItem() != null) {
                if (isHammer(itemstack.getItem())) {
                    ItemStack damaged = new ItemStack(itemstack.getItem(), 2, itemstack.getItemDamage() + 1);
                    if (itemstack.hasTagCompound())
                        damaged.readFromNBT(itemstack.getTagCompound());
                    if (damaged.getItemDamage() >= damaged.getMaxDamage())
                        damaged.stackSize--;
                    if (damaged != null)
                        craftMatrix.setInventorySlotContents(i, damaged);
                    break;
                } else if (itemstack.getItem() == ModItems.spellParchment) {
                    if (itemstack.hasTagCompound())
                        tag = itemstack.stackTagCompound;
                }
            }
        }
        if (output != null && output.getItem() != null) {
            if (output.getItem() instanceof ItemMaterial) {
                if (output.getItemDamage() == 3) {
                    if (!player.getEntityWorld().isRemote && player.getEntityWorld().rand.nextInt(1000) < 75)
                        player.getEntityWorld().spawnEntityInWorld(new EntityLightningBolt(player.getEntityWorld(), player.posX + SimpleUtil.randDist(3), player.posY + SimpleUtil.randDist(3), player.posZ + SimpleUtil.randDist(3)));
                }
            } else if (output.getItem() == ModItems.spellParchment) {
                if (tag != null) {
                    String id = tag.getString(ModLibs.SPELL_ID);
                    if (!event.crafting.hasTagCompound())
                        event.crafting.stackTagCompound = new NBTTagCompound();
                    event.crafting.getTagCompound().setString(ModLibs.SPELL_ID, id);
                } else
                    event.crafting.stackSize = 0;
            }
        }
    }


    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player != null && FMLCommonHandler.instance().getEffectiveSide().isServer())
            Rings.proxy.manaHandler.loadPlayerData(event.player);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player != null && FMLCommonHandler.instance().getEffectiveSide().isServer())
            Rings.proxy.manaHandler.unloadPlayerSession(event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player != null && FMLCommonHandler.instance().getEffectiveSide().isServer())
            Rings.proxy.manaHandler.playerChangedDimension(event.player, event.fromDim, event.toDim);
    }
}
