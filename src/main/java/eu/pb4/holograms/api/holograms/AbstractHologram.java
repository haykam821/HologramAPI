package eu.pb4.holograms.api.holograms;

import com.google.common.collect.Lists;
import eu.pb4.holograms.api.InteractionType;
import eu.pb4.holograms.api.elements.EmptyHologramElement;
import eu.pb4.holograms.api.elements.HologramElement;
import eu.pb4.holograms.api.elements.clickable.EntityHologramElement;
import eu.pb4.holograms.api.elements.item.AbstractItemHologramElement;
import eu.pb4.holograms.api.elements.item.SpinningItemHologramElement;
import eu.pb4.holograms.api.elements.item.StaticItemHologramElement;
import eu.pb4.holograms.api.elements.text.AbstractTextHologramElement;
import eu.pb4.holograms.api.elements.text.MovingTextHologramElement;
import eu.pb4.holograms.api.elements.text.StaticTextHologramElement;
import eu.pb4.holograms.impl.interfaces.HologramHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unused"})
public abstract class AbstractHologram {
    protected final ServerWorld world;
    protected List<HologramElement> elements = new ArrayList<>();
    protected Vec3d position;
    protected Set<ServerPlayerEntity> players = new HashSet<>();
    protected Object2IntMap<ServerPlayerEntity> playerLastInteraction = new Object2IntArrayMap<>();
    protected VerticalAlign alignment;

    protected IntList entityIds = new IntArrayList();
    protected boolean isActive = false;

    public AbstractHologram(ServerWorld world, Vec3d position, VerticalAlign alignment) {
        this.world = world;
        this.position = new Vec3d(position.x, position.y, position.z);
        this.alignment = alignment;
    }

    public VerticalAlign getAlignment() {
        return this.alignment;
    }

    public void setAlignment(VerticalAlign alignment) {
        this.alignment = alignment;
        if (this.isActive) {
            this.sendPositionUpdate();
        }
    }

    protected void updatePosition(Vec3d position) {
        this.position = new Vec3d(position.x, position.y, position.z);
        this.sendPositionUpdate();
    }

    protected void sendPositionUpdate() {
        if (isActive) {
            for (HologramElement element : this.elements) {
                if (element.getEntityIds().size() == 0) {
                    continue;
                }

                for (ServerPlayerEntity player : this.players) {
                    element.updatePosition(player, this);
                }
            }
        }
    }

    public int addText(Text text) {
        return this.addElement(new StaticTextHologramElement(text));
    }

    public int addText(int pos, Text text) {
        return this.addElement(pos, new StaticTextHologramElement(text));
    }

    public int setText(int pos, Text text) {
        var hologramElement = this.getElement(pos);

        if (hologramElement instanceof AbstractTextHologramElement element) {
            element.setText(text);
            return pos;
        } else {
            return this.setElement(pos, new StaticTextHologramElement(text));
        }
    }

    public int addText(Text text, boolean isStatic) {
        return this.addElement(AbstractTextHologramElement.create(text, isStatic));
    }

    public int addText(int pos, Text text, boolean isStatic) {
        return this.addElement(pos, AbstractTextHologramElement.create(text, isStatic));
    }

    public int setText(int pos, Text text, boolean isStatic) {
        var hologramElement = this.getElement(pos);

        if ((isStatic && hologramElement instanceof StaticTextHologramElement) || (!isStatic && hologramElement instanceof MovingTextHologramElement)) {
            ((AbstractTextHologramElement) hologramElement).setText(text);
            return pos;
        } else {
            return this.setElement(pos, AbstractTextHologramElement.create(text, isStatic));
        }
    }

    public int addItemStack(ItemStack stack, boolean isStatic) {
        return this.addElement(AbstractItemHologramElement.create(stack, isStatic));
    }

    public int addItemStack(int pos, ItemStack stack, boolean isStatic) {
        return this.addElement(pos, AbstractItemHologramElement.create(stack, isStatic));
    }

    public int setItemStack(int pos, ItemStack stack, boolean isStatic) {
        var hologramElement = this.getElement(pos);

        if ((isStatic && hologramElement instanceof StaticItemHologramElement) || (!isStatic && hologramElement instanceof SpinningItemHologramElement)) {
            ((AbstractItemHologramElement) hologramElement).setItemStack(stack);
            return pos;
        } else {
            return this.setElement(pos, AbstractItemHologramElement.create(stack, isStatic));
        }

    }

    public int addEntity(Entity entity) {
        return this.addElement(new EntityHologramElement(entity));
    }

    public int addEntity(int pos, Entity entity) {
        return this.addElement(pos, new EntityHologramElement(entity));
    }

    public int setEntity(int pos, Entity entity) {
        return this.setElement(pos, new EntityHologramElement(entity));
    }

    public int addElement(HologramElement element) {
        this.elements.add(element);
        this.entityIds.addAll(element.getEntityIds());
        if (this.isActive) {
            for (ServerPlayerEntity player : this.players) {
                element.createSpawnPackets(player, this);
            }
            this.sendPositionUpdate();
        }

        return this.elements.indexOf(element);
    }

    public int addElement(int pos, HologramElement element) {
        this.elements.add(pos, element);
        this.entityIds.addAll(element.getEntityIds());
        if (isActive) {
            for (ServerPlayerEntity player : this.players) {
                element.createSpawnPackets(player, this);
            }
            this.sendPositionUpdate();
        }

        return this.elements.indexOf(element);
    }

    public int setElement(int pos, HologramElement element) {
        if (pos >= 0) {
            this.entityIds.addAll(element.getEntityIds());

            if (pos >= this.elements.size()) {
                int needed = pos - this.elements.size();
                for (int x = 0; x < needed; x++) {
                    this.elements.add(new EmptyHologramElement());
                }
                this.elements.add(element);
                if (this.isActive) {
                    for (ServerPlayerEntity player : this.players) {
                        element.createSpawnPackets(player, this);
                    }
                    this.sendPositionUpdate();
                }
                return pos;
            } else {
                HologramElement oldElement = this.elements.get(pos);
                IntList ids = oldElement.getEntityIds();
                this.entityIds.removeAll(ids);
                this.elements.set(pos, element);

                if (this.isActive) {
                    for (ServerPlayerEntity player : this.players) {
                        oldElement.createRemovePackets(player, this);
                    }

                    for (ServerPlayerEntity player : this.players) {
                        element.createSpawnPackets(player, this);
                    }

                    this.sendPositionUpdate();
                }
            }

            return pos;
        }

        return -1;
    }

    @Nullable
    public HologramElement getElement(int pos) {
        if (pos >= 0 && pos < this.elements.size()) {
            return this.elements.get(pos);
        }

        return null;
    }

    public int getElementIndex(HologramElement element) {
        return this.elements.indexOf(element);
    }

    public void removeElement(HologramElement element) {
        this.elements.remove(element);
    }

    public void removeElement(int pos) {
        HologramElement element = this.elements.remove(pos);
        for (ServerPlayerEntity player : this.players) {
            element.createRemovePackets(player, this);
        }
        this.sendPositionUpdate();
    }

    @Nullable
    public Vec3d getElementPosition(HologramElement element) {
        if (this.alignment == VerticalAlign.TOP) {
            double height = 0;

            for (HologramElement other : Lists.reverse(elements)) {
                if (other == element) {
                    return new Vec3d(this.position.x, this.position.y + height, this.position.z);
                }
                height += other.getHeight();
            }
        }
        if (this.alignment == VerticalAlign.BOTTOM) {
            double height = 0;

            for (HologramElement other : this.elements) {
                if (other == element) {
                    return new Vec3d(this.position.x, this.position.y - height, this.position.z);
                }
                height += other.getHeight();
            }
        } else if (this.alignment == VerticalAlign.CENTER) {
            double fullHeight = 0;
            for (HologramElement other : this.elements) {
                fullHeight += other.getHeight();
            }

            fullHeight = fullHeight / 2;

            double height = 0;

            for (HologramElement other : Lists.reverse(elements)) {
                if (other == element) {
                    return new Vec3d(this.position.x, this.position.y + height - fullHeight, this.position.z);
                }
                height += other.getHeight();
            }
        }

        return null;
    }

    public List<HologramElement> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public void clearElements() {
        while (this.elements.size() != 0) {
            this.removeElement(0);
        }
    }

    public void tick() {
        if (isActive && this.players.size() > 0) {
            for (HologramElement element : this.elements) {
                element.onTick(this);
            }
        }
    }

    public void show() {
        if (!isActive) {
            this.isActive = true;
            for (ServerPlayerEntity player : this.players) {
                for (HologramElement element : this.elements) {
                    if (element.getEntityIds().size() == 0) {
                        continue;
                    }
                    element.createSpawnPackets(player, this);
                }
            }
        }
    }

    public void hide() {
        if (isActive) {
            this.isActive = false;
            for (HologramElement element : this.elements) {
                for (ServerPlayerEntity player : this.players) {
                    if (!player.isDisconnected()) {
                        element.createRemovePackets(player, this);
                    }
                }
            }
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void addPlayer(ServerPlayerEntity player) {
        if (!this.players.contains(player)) {
            this.players.add(player);
            ((HologramHolder) player).hologramApi$addHologram(this);

            if (isActive) {
                for (HologramElement element : this.elements) {
                    if (element.getEntityIds().size() == 0) {
                        continue;
                    }
                    element.createSpawnPackets(player, this);
                }
            }
        }
    }

    public void removePlayer(ServerPlayerEntity player) {
        if (this.players.contains(player)) {
            this.players.remove(player);
            this.playerLastInteraction.removeInt(player);
            ((HologramHolder) player).hologramApi$removeHologram(this);
            if (isActive) {
                if (!player.isDisconnected()) {
                    for (HologramElement element : this.elements) {
                        element.createRemovePackets(player, this);
                    }
                }
            }
        }
    }

    public Set<ServerPlayerEntity> getPlayerSet() {
        return Collections.unmodifiableSet(this.players);
    }

    public boolean canAddPlayer(ServerPlayerEntity player) {
        return true;
    }

    public IntList getEntityIds() {
        return IntLists.unmodifiable(this.entityIds);
    }

    public void click(ServerPlayerEntity player, InteractionType type, @Nullable Hand hand, @Nullable Vec3d hitPosition, int id) {
        int lastInteractionTick = this.playerLastInteraction.getInt(player);

        if (lastInteractionTick == player.age) {
            return;
        }
        this.playerLastInteraction.put(player, player.age);

        for (HologramElement element : this.elements) {
            if (element.getEntityIds().contains(id)) {
                element.onClick(this, player, type, hand, hitPosition, id);
                return;
            }
        }
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public void clearPlayers() {
        for (ServerPlayerEntity player : new HashSet<>(this.players)) {
            this.removePlayer(player);
        }
    }

    public enum VerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }
}
