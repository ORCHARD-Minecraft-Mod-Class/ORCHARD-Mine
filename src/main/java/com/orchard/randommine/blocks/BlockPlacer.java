package com.orchard.randommine.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.RegistryDefaulted;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlacer extends BlockContainer
{
    /**
     * this places Block.
     *sorry it is so messy it is the dispenser changed up.
     */
	Entity en;
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");
    /** Registry for all dispense behaviors. */
    public static final RegistryDefaulted dispenseBehaviorRegistry = new RegistryDefaulted(new BehaviorDefaultDispenseItem());
    protected Random rand = new Random();
    public BlockPlacer()
    {
        super(Material.rock);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TRIGGERED, Boolean.valueOf(false)));
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn)
    {
        return 4;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        this.setDefaultDirection(worldIn, pos, state);
    }

    private void setDefaultDirection(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
            boolean flag = worldIn.getBlockState(pos.north()).getBlock().isFullBlock();
            boolean flag1 = worldIn.getBlockState(pos.south()).getBlock().isFullBlock();

            if (enumfacing == EnumFacing.NORTH && flag && !flag1)
            {
                enumfacing = EnumFacing.SOUTH;
            }
            else if (enumfacing == EnumFacing.SOUTH && flag1 && !flag)
            {
                enumfacing = EnumFacing.NORTH;
            }
            else
            {
                boolean flag2 = worldIn.getBlockState(pos.west()).getBlock().isFullBlock();
                boolean flag3 = worldIn.getBlockState(pos.east()).getBlock().isFullBlock();

                if (enumfacing == EnumFacing.WEST && flag2 && !flag3)
                {
                    enumfacing = EnumFacing.EAST;
                }
                else if (enumfacing == EnumFacing.EAST && flag3 && !flag2)
                {
                    enumfacing = EnumFacing.WEST;
                }
            }

            worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing).withProperty(TRIGGERED, Boolean.valueOf(false)), 2);
        }
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	
        if (worldIn.isRemote)
        {
            return true;
        }
        else
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
            TileEntityDispenser tileentitydispenser = (TileEntityDispenser)blocksourceimpl.getBlockTileEntity();
            tileentitydispenser.setCustomName("Block Placer");
            if (tileentity instanceof TileEntityDispenser)
            {
                playerIn.displayGUIChest((TileEntityDispenser)tileentity);
            }

            return true;
        }
    }

    protected void dispense(World worldIn, BlockPos pos)
    {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser)blocksourceimpl.getBlockTileEntity();

        if (tileentitydispenser != null)
        {
            int i = tileentitydispenser.getDispenseSlot();
            	
            if (i < 0)
            {
                worldIn.playAuxSFX(1001, pos, 0);
            }
            else
            {
                ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
                IBehaviorDispenseItem ibehaviordispenseitem = this.getBehavior(itemstack);

                if (ibehaviordispenseitem != IBehaviorDispenseItem.itemDispenseBehaviorProvider)
                {
                	try{
                	    IPosition iposition = BlockDispenser.getDispensePosition(blocksourceimpl);
                	    BlockPos pos1 = new BlockPos(iposition.getX(), iposition.getY(), iposition.getZ());
                		Block block = Block.getBlockFromItem(itemstack.getItem());
                		if (worldIn.canBlockBePlaced(block,pos1,true,BlockPistonBase.getFacing(1),en,itemstack)){
                	ItemStack itemstack1 = this.dispenseStack(blocksourceimpl, itemstack, worldIn);
                	worldIn.playSoundEffect(iposition.getX(), iposition.getY(), iposition.getZ(), block.stepSound.getPlaceSound(), 1.0f, 1.0f);
                    if (block.getStateFromMeta(itemstack1.getMetadata())==worldIn.getBlockState(pos1))tileentitydispenser.setInventorySlotContents(i, itemstack1.stackSize == 0 ? null : itemstack1);
                	    }}
                	    catch (Exception e) {}
                }
            }
        }
    }

    protected IBehaviorDispenseItem getBehavior(ItemStack stack)
    {
        return (IBehaviorDispenseItem)dispenseBehaviorRegistry.getObject(stack == null ? null : stack.getItem());
    }

    /**
     * Called when a neighboring block changes.
     */
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
        boolean flag1 = ((Boolean)state.getValue(TRIGGERED)).booleanValue();

        if (flag && !flag1)
        {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
        }
        else if (!flag && flag1)
        {
            worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            this.dispense(worldIn, pos);
        }
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityDispenser();
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)).withProperty(TRIGGERED, Boolean.valueOf(false));
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, BlockPistonBase.getFacingFromEntity(worldIn, pos, placer)), 2);

        if (stack.hasDisplayName())
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityDispenser)
            {
                ((TileEntityDispenser)tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityDispenser)
        {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityDispenser)tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Get the position where the dispenser at the given Coordinates should dispense to.
     */
    public static IPosition getDispensePosition(IBlockSource coords)
    {
        EnumFacing enumfacing = getFacing(coords.getBlockMetadata());
        double d0 = coords.getX() + 0.7D * (double)enumfacing.getFrontOffsetX();
        double d1 = coords.getY() + 0.7D * (double)enumfacing.getFrontOffsetY();
        double d2 = coords.getZ() + 0.7D * (double)enumfacing.getFrontOffsetZ();
        return new PositionImpl(d0, d1, d2);
    }

    /**
     * Get the facing of a dispenser with the given metadata
     */
    public static EnumFacing getFacing(int meta)
    {
        return EnumFacing.getFront(meta & 7);
    }

    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    public int getComparatorInputOverride(World worldIn, BlockPos pos)
    {
        return Container.calcRedstone(worldIn.getTileEntity(pos));
    }

    /**
     * The type of render function that is called for this block
     */
    public int getRenderType()
    {
        return 3;
    }

    /**
     * Possibly modify the given BlockState before rendering it on an Entity (Minecarts, Endermen, ...)
     */
    @SideOnly(Side.CLIENT)
    public IBlockState getStateForEntityRender(IBlockState state)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, getFacing(meta)).withProperty(TRIGGERED, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        byte b0 = 0;
        int i = b0 | ((EnumFacing)state.getValue(FACING)).getIndex();

        if (((Boolean)state.getValue(TRIGGERED)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING, TRIGGERED});
    }

protected ItemStack dispenseStack(IBlockSource source, ItemStack stack,World worldIn)
{ 
	try{
    IPosition iposition = BlockDispenser.getDispensePosition(source);
    ItemStack itemstack1 = stack.splitStack(1);
    BlockPos pos = new BlockPos(iposition.getX(), iposition.getY(), iposition.getZ());
	Block block;
	block = Block.getBlockFromItem(itemstack1.getItem());
    if( worldIn.canBlockBePlaced(block,pos,true,BlockPistonBase.getFacing(1),en,itemstack1)){
    worldIn.setBlockState(pos, block.getStateFromMeta(itemstack1.getMetadata()));
    }}
	catch (Exception e) {return stack;}

    return stack;
}
}