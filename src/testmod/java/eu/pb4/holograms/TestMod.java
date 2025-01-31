package eu.pb4.holograms;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.holograms.api.InteractionType;
import eu.pb4.holograms.api.elements.clickable.CubeHitboxHologramElement;
import eu.pb4.holograms.api.elements.clickable.EntityHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.EntityHologram;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer {
    static int pos = -1;
    static int pos2 = 1;

    private static int test(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            WorldHologram hologram = new WorldHologram(player.getWorld(), player.getPos());

            hologram.addText(Text.literal("hello"));
            hologram.addElement(new EntityHologramElement(getEntityType(false).create(player.world)));
            hologram.addText(Text.literal("test"));
            hologram.addItemStack(Items.POTATO.getDefaultStack(), false);
            hologram.addItemStack(Items.DIAMOND.getDefaultStack(), true);
            hologram.addText(Text.literal("« »"));
            hologram.addElement(new CubeHitboxHologramElement(2, new Vec3d(0, -0.2, 0)) {
                @Override
                public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
                    super.onClick(hologram, player, type, hand, vec, entityId);
                    hologram.setElement(1, new EntityHologramElement(getEntityType(type == InteractionType.ATTACK).create(player.world)));
                }
            });
            hologram.addText(Text.literal("434234254234562653247y4575678rt").formatted(Formatting.AQUA));

            hologram.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static EntityType getEntityType(boolean previous) {
        if (previous) {
            pos--;
        } else {
            pos++;
        }

        EntityType type = Registries.ENTITY_TYPE.get(pos);

        if (type == null) {
            pos = 0;
            type = Registries.ENTITY_TYPE.get(pos);
        }

        System.out.println(type);

        return type;
    }

    private static int test2(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            WorldHologram hologram = new WorldHologram(player.getWorld(), player.getPos());

            hologram.addText(Text.literal("hello"));
            hologram.addElement(new CubeHitboxHologramElement(1, new Vec3d(0, 0, 0)) {
                @Override
                public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
                    super.onClick(hologram, player, type, hand, vec, entityId);
                    hologram.setAlignment(AbstractHologram.VerticalAlign.TOP);
                }
            });
            hologram.addText(Text.literal("test"));
            hologram.addItemStack(Items.POTATO.getDefaultStack(), false);
            hologram.addElement(new CubeHitboxHologramElement(1, new Vec3d(0, 0, 0)) {
                @Override
                public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
                    super.onClick(hologram, player, type, hand, vec, entityId);
                    hologram.setAlignment(AbstractHologram.VerticalAlign.CENTER);
                }
            });
            hologram.addItemStack(Items.DIAMOND.getDefaultStack(), true);
            hologram.addText(Text.literal("« »"));
            hologram.addElement(new CubeHitboxHologramElement(1, new Vec3d(0, -0.2, 0)) {
                @Override
                public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
                    super.onClick(hologram, player, type, hand, vec, entityId);
                    hologram.setAlignment(AbstractHologram.VerticalAlign.BOTTOM);
                }
            });

            hologram.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test3(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            PigEntity pig = EntityType.PIG.create(player.world);
            pig.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0, 0);

            System.out.println(pig);
            EntityHologram hologram = new EntityHologram(pig, new Vec3d(2, 2, 2));

            hologram.addElement(new CubeHitboxHologramElement(1, new Vec3d(0, -0.2, 0)) {
                @Override
                public void onClick(AbstractHologram hologram, ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d vec, int entityId) {
                    super.onClick(hologram, player, type, hand, vec, entityId);
                    hologram.setText(pos2++, Text.literal("Nice-" + pig.age));
                }
            });
            hologram.show();

            hologram.addText(Text.literal("Hello There"));
            hologram.addItemStack(Items.DIAMOND.getDefaultStack(), true);


            player.world.spawnEntity(pig);

            System.out.println(hologram.getEntityIds());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test4(CommandContext<ServerCommandSource> objectCommandContext) {
        try {
            ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();

            PigEntity pig = EntityType.PIG.create(player.world);
            pig.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0, 0);
            player.world.spawnEntity(pig);
            System.out.println(pig);

            EntityHologram hologram = new EntityHologram(pig, new Vec3d(2, 2, 2));

            hologram.addText(Text.literal("Hello There"));
            hologram.addText(Text.literal("(Static)"));
            hologram.addText(Text.literal("Hello!"), false);
            hologram.addText(Text.literal("(Non Static)"), false);

            hologram.addItemStack(Items.DIAMOND.getDefaultStack(), true);
            hologram.addItemStack(Items.IRON_AXE.getDefaultStack(), false);

            hologram.show();
            System.out.println(hologram.getEntityIds());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("test").executes(TestMod::test)
            );
            dispatcher.register(
                    literal("test2").executes(TestMod::test2)
            );
            dispatcher.register(
                    literal("test3").executes(TestMod::test3)
            );

            dispatcher.register(
                    literal("test4").executes(TestMod::test4)
            );
        });
    }

}
