package com.hopr.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;

@Mixin(HostileEntity.class)
public class MobSpawnMixin {
	private static final Identifier wSkeleId = new Identifier("minecraft:wither_skeleton");
	private static final Identifier skeleId = new Identifier("minecraft:skeleton");
	private static final RegistryKey<DimensionType> OVERWORLD = of("overworld");

	private static final RegistryKey<DimensionType> THE_NETHER = of("the_nether");

	@Inject(method = "canSpawnIgnoreLightLevel", at = @At("RETURN"), cancellable = true)
	private static void injectSpawn1(EntityType<? extends HostileEntity> entityType, WorldAccess worldAccess,
			SpawnReason spawnReason, BlockPos blockPos, Random random, CallbackInfoReturnable<Boolean> cir) {
//	 
		EntityType<?> wSkeleEntity = Registries.ENTITY_TYPE.get(wSkeleId);
		EntityType<?> skeleEntity = Registries.ENTITY_TYPE.get(skeleId);
		DimensionType dt = worldAccess.getDimension();
		Impl<DimensionType> ty = BuiltinRegistries.createWrapperLookup().getOptionalWrapper(RegistryKeys.DIMENSION_TYPE)
				.get();
		DimensionType nether = ty.getOptional(THE_NETHER).get().comp_349();

		if ((entityType.equals(skeleEntity) || entityType.equals(wSkeleEntity)) && dt.equals(nether))
			cir.setReturnValue(true);
	}

	@Inject(method = "canSpawnInDark", at = @At("RETURN"), cancellable = true)
	private static void injectSpawn2(EntityType<? extends HostileEntity> entityType,
			ServerWorldAccess serverWorldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random,
			CallbackInfoReturnable<Boolean> cir) {
		EntityType<?> wSkeleEntity = Registries.ENTITY_TYPE.get(wSkeleId);
		EntityType<?> skeleEntity = Registries.ENTITY_TYPE.get(skeleId);
		DimensionType dt = serverWorldAccess.getDimension();
		Impl<DimensionType> ty = BuiltinRegistries.createWrapperLookup().getOptionalWrapper(RegistryKeys.DIMENSION_TYPE)
				.get();
		DimensionType nether = ty.getOptional(THE_NETHER).get().comp_349();

		if ((entityType.equals(skeleEntity) || entityType.equals(wSkeleEntity)) && dt.equals(nether))
			cir.setReturnValue(true);

	}

	@Inject(method = "isSpawnDark", at = @At("RETURN"), cancellable = true)
	private static void injectSpawn3(ServerWorldAccess serverWorldAccess, BlockPos blockPos, Random random,
			CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	private static RegistryKey<DimensionType> of(String string) {
		return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier(string));
	}
}
