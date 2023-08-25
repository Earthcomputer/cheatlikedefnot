package net.earthcomputer.cheatlikedefnot.compat.clientcommands;

import com.mojang.logging.LogUtils;
import net.earthcomputer.cheatlikedefnot.CLDDataQueryHandler;
import net.earthcomputer.clientcommands.task.SimpleTask;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class CLDFindItemTask extends SimpleTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final long MAX_SCAN_TIME = 30_000_000L; // 30ms
    private static final int NO_RESPONSE_TIMEOUT = 100; // ticks

    private final String searchingForName;
    private final Predicate<ItemStack> searchingFor;
    private final boolean searchShulkerBoxes;
    private final boolean keepSearching;

    private int totalFound = 0;
    private final Set<BlockPos> searchedBlocks = new HashSet<>();
    private boolean isScanning = true;
    private Iterator<BlockPos.Mutable> scanningIterator;
    private final Set<BlockPos> waitingOnBlocks = new HashSet<>();
    private int currentlySearchingTimeout;
    @Nullable
    private BlockPos enderChestPosition = null;
    @Nullable
    private Integer numItemsInEnderChest = null;
    private boolean hasPrintedEnderChest = false;

    public CLDFindItemTask(String searchingForName, Predicate<ItemStack> searchingFor, boolean searchShulkerBoxes, boolean keepSearching) {
        this.searchingForName = searchingForName;
        this.searchingFor = searchingFor;
        this.searchShulkerBoxes = searchShulkerBoxes;
        this.keepSearching = keepSearching;
    }

    // @Override
    public boolean condition() {
        return true;
    }

    // @Override
    protected void onTick() {
        Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
        if (cameraEntity == null) {
            try {
                getClass().getMethod("_break").invoke(this);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to break", e);
            }
            return;
        }
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;

        if (isScanning) {
            long startTime = System.nanoTime();
            if (scanningIterator == null) {
                Vec3d cameraPos = cameraEntity.getCameraPosVec(0);
                scanningIterator = BlockPos.iterateInSquare(new BlockPos(MathHelper.floor(cameraPos.x) >> 4, 0, MathHelper.floor(cameraPos.z) >> 4), MinecraftClient.getInstance().options.getViewDistance().getValue(), Direction.EAST, Direction.SOUTH).iterator();
            }
            while (scanningIterator.hasNext()) {
                BlockPos chunkPosAsBlockPos = scanningIterator.next();
                if (world.getChunk(chunkPosAsBlockPos.getX(), chunkPosAsBlockPos.getZ(), ChunkStatus.FULL, false) != null) {
                    scanChunk(new ChunkPos(chunkPosAsBlockPos.getX(), chunkPosAsBlockPos.getZ()), cameraEntity);
                }

                if (System.nanoTime() - startTime > MAX_SCAN_TIME) {
                    // wait a tick
                    return;
                }
            }
            isScanning = false;
        }

        if (waitingOnBlocks.isEmpty() && (enderChestPosition == null || numItemsInEnderChest != null)) {
            if (keepSearching) {
                isScanning = true;
            } else {
                try {
                    getClass().getMethod("_break").invoke(this);
                } catch (ReflectiveOperationException e) {
                    LOGGER.error("Failed to break", e);
                }
            }
            return;
        }

        if (currentlySearchingTimeout > 0) {
            currentlySearchingTimeout--;
        } else {
            // timeout
            try {
                getClass().getMethod("_break").invoke(this);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to break", e);
            }
        }
    }

    private void scanChunk(ChunkPos chunkToScan, Entity cameraEntity) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        assert networkHandler != null;

        // check if we can possibly find a closer ender chest
        if (enderChestPosition != null && numItemsInEnderChest != null && !hasPrintedEnderChest) {
            Vec3d cameraPos = cameraEntity.getCameraPosVec(0);
            double enderChestDistanceSq = enderChestPosition.getSquaredDistance(cameraPos);
            int cameraChunkX = MathHelper.floor(cameraPos.x) >> 4;
            int cameraChunkZ = MathHelper.floor(cameraPos.z) >> 4;
            int currentChunkRadius = Math.max(Math.abs(cameraChunkX - chunkToScan.x), Math.abs(cameraChunkZ - chunkToScan.z));
            double closestPossibleDistance = ((currentChunkRadius - 1) << 4) + Math.min(
                Math.min(cameraPos.x - (cameraChunkX << 4), cameraPos.z - (cameraChunkZ << 4)),
                Math.min(((cameraChunkX + 1) << 4) - cameraPos.x, ((cameraChunkZ + 1) << 4) - cameraPos.z));
            if (enderChestDistanceSq < closestPossibleDistance * closestPossibleDistance) {
                hasPrintedEnderChest = true;
                if (numItemsInEnderChest > 0) {
                    printLocation(enderChestPosition, numItemsInEnderChest);
                }
            }
        }

        WorldChunk chunk = world.getChunk(chunkToScan.x, chunkToScan.z);

        for (BlockPos pos : BlockPos.iterate(chunkToScan.getStartX(), world.getBottomY(), chunkToScan.getStartZ(), chunkToScan.getEndX(), world.getTopY(), chunkToScan.getEndZ())) {
            if (searchedBlocks.contains(pos)) {
                continue;
            }
            BlockState state = chunk.getBlockState(pos);

            if (state.isOf(Blocks.ENDER_CHEST)) {
                BlockPos currentPos = pos.toImmutable();
                searchedBlocks.add(currentPos);
                if (enderChestPosition == null) {
                    enderChestPosition = currentPos;
                    currentlySearchingTimeout = NO_RESPONSE_TIMEOUT;
                    CLDDataQueryHandler.get(networkHandler).queryEntityNbt(player.getId(), playerNbt -> {
                        int numItemsInEnderChest = 0;
                        if (playerNbt != null && playerNbt.contains("EnderItems", NbtElement.LIST_TYPE)) {
                            numItemsInEnderChest = countItems(playerNbt.getList("EnderItems", NbtElement.COMPOUND_TYPE));
                        }
                        this.numItemsInEnderChest = numItemsInEnderChest;
                        totalFound += numItemsInEnderChest;
                        currentlySearchingTimeout = NO_RESPONSE_TIMEOUT;
                    });
                } else if (!hasPrintedEnderChest) {
                    Vec3d cameraPos = cameraEntity.getCameraPosVec(0);
                    double currentDistanceSq = enderChestPosition.getSquaredDistance(cameraPos);
                    double newDistanceSq = currentPos.getSquaredDistance(cameraPos);
                    if (newDistanceSq < currentDistanceSq) {
                        enderChestPosition = currentPos;
                    }
                }
            } else if (chunk.getBlockEntity(pos) instanceof Inventory) {
                BlockPos currentPos = pos.toImmutable();
                searchedBlocks.add(currentPos);
                waitingOnBlocks.add(currentPos);
                currentlySearchingTimeout = NO_RESPONSE_TIMEOUT;
                CLDDataQueryHandler.get(networkHandler).queryBlockNbt(currentPos, blockNbt -> {
                    waitingOnBlocks.remove(currentPos);
                    if (blockNbt != null && blockNbt.contains("Items", NbtElement.LIST_TYPE)) {
                        int count = countItems(blockNbt.getList("Items", NbtElement.COMPOUND_TYPE));
                        if (count > 0) {
                            totalFound += count;
                            printLocation(currentPos, count);
                        }
                    }
                    currentlySearchingTimeout = NO_RESPONSE_TIMEOUT;
                });
            }
        }
    }

    private int countItems(NbtList inventory) {
        int result = 0;
        for (int i = 0; i < inventory.size(); i++) {
            NbtCompound compound = inventory.getCompound(i);
            ItemStack stack = ItemStack.fromNbt(compound);
            if (searchingFor.test(stack)) {
                result += stack.getCount();
            }
            if (searchShulkerBoxes && stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock) {
                NbtCompound blockEntityNbt = BlockItem.getBlockEntityNbt(stack);
                if (blockEntityNbt != null && blockEntityNbt.contains("Items", NbtElement.LIST_TYPE)) {
                    result += countItems(blockEntityNbt.getList("Items", NbtElement.COMPOUND_TYPE));
                }
            }
        }
        return result;
    }

    private void printLocation(BlockPos pos, int count) {
        try {
            Class<?> clientCommandHelper = Class.forName("net.earthcomputer.clientcommands.command.ClientCommandHelper");
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("commands.cfinditem.match.left", count, searchingForName)
                .append((Text) clientCommandHelper.getMethod("getLookCoordsTextComponent", BlockPos.class).invoke(null, pos))
                .append(" ")
                .append((Text) clientCommandHelper.getMethod("getGlowCoordsTextComponent", MutableText.class, BlockPos.class).invoke(null, Text.translatable("commands.cfindblock.success.glow"), pos))
                .append(Text.translatable("commands.cfinditem.match.right", count, searchingForName)));
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to print location", e);
        }
    }

    // @Override
    public void onCompleted() {
        if (enderChestPosition != null && numItemsInEnderChest != null && numItemsInEnderChest > 0 && !hasPrintedEnderChest) {
            printLocation(enderChestPosition, numItemsInEnderChest);
        }
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("commands.cfinditem.total", totalFound, searchingForName).formatted(Formatting.BOLD));
    }
}
