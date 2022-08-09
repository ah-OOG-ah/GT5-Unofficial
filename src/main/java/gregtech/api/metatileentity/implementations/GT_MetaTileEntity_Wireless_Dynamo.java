package gregtech.api.metatileentity.implementations;

import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IGlobalWirelessEnergy;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import java.math.BigInteger;

import static gregtech.GT_Mod.gregtechproxy;
import static gregtech.api.enums.GT_Values.*;

public class GT_MetaTileEntity_Wireless_Dynamo extends GT_MetaTileEntity_Hatch_Dynamo implements IGlobalWirelessEnergy {

    private static final long ticks_between_energy_addition = 100L*20L;
    private static final long number_of_energy_additions = 2L;
    private final BigInteger eu_transferred_per_operation = BigInteger.valueOf(2L * V[mTier] * ticks_between_energy_addition);
    private String owner_uuid;
    private String owner_name;

    public GT_MetaTileEntity_Wireless_Dynamo(String aName, byte aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    public GT_MetaTileEntity_Wireless_Dynamo(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, new String[] {""});
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[]{aBaseTexture, Textures.BlockIcons.OVERLAYS_ENERGY_IN_MULTI_WIRELESS_ON[mTier]};
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[]{aBaseTexture, Textures.BlockIcons.OVERLAYS_ENERGY_IN_MULTI_WIRELESS_ON[mTier]};
    }

    @Override
    public boolean isSimpleMachine() {
        return true;
    }

    @Override
    public boolean isFacingValid(byte aFacing) {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public boolean isEnetOutput() {
        return false;
    }

    @Override
    public boolean isInputFacing(byte aSide) {
        return aSide == getBaseMetaTileEntity().getFrontFacing();
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
    }

    @Override
    public long getMinimumStoredEU() {
        return 2 * V[mTier];
    }

    @Override
    public long maxEUOutput() {
        return V[mTier];
    }

    @Override
    public long maxEUStore() {
        return V[mTier] * number_of_energy_additions * ticks_between_energy_addition;
    }

    @Override
    public String[] getDescription() {
        String uuid = gregtechproxy.getThePlayer().getUniqueID().toString();
        return new String[] {
//            "Transmits " + EnumChatFormatting.RED + GT_Utility.formatNumbers(eu_transferred_per_operation/V[mTier]) + EnumChatFormatting.GRAY + " A of " + TIER_COLORS[mTier] + VN[mTier] + EnumChatFormatting.GRAY + " through trans-dimensional space every " + EnumChatFormatting.RED + GT_Utility.formatNumbers(ticks_between_energy_addition) + EnumChatFormatting.GRAY + " ticks.",
            EnumChatFormatting.GRAY + "Does not connect to wires.",
            EnumChatFormatting.GRAY + "There is currently " + EnumChatFormatting.RED + GT_Utility.formatNumbers(GlobalEnergy.getOrDefault(uuid, BigInteger.ZERO)) + EnumChatFormatting.GRAY + " EU in your network.",
            AuthorColen
        };
    }

    @Override
    public long maxAmperesOut() {
        return 2;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_Wireless_Dynamo(mName, mTier, new String[] {""}, mTextures);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
        return false;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPreTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide()) {

            // On first tick find the player name and attempt to add them to the map.
            if (aTick == 1) {

                // UUID and username of the owner.
                owner_uuid = aBaseMetaTileEntity.getOwnerUuid().toString();
                owner_name = aBaseMetaTileEntity.getOwnerName();

                // Attempt to load in map from file.
                if (GlobalEnergy.size() == 0)
                    IGlobalWirelessEnergy.super.LoadGlobalEnergyInfo(aBaseMetaTileEntity.getWorld());

                IGlobalWirelessEnergy.super.StrongCheckOrAddUser(owner_uuid, owner_name);

            }

            // Every ticks_between_energy_addition ticks change the energy content of the machine.
            if (aTick % ticks_between_energy_addition == 0L) {
                IGlobalWirelessEnergy.super.addEUToGlobalEnergyMap(owner_uuid, getEUVar());
                setEUVar(0L);
            }
        }
    }
}
