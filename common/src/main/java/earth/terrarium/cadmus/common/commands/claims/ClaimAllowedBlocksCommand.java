package earth.terrarium.cadmus.common.commands.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ClaimAllowedBlocksCommand {

    private static final SimpleCommandExceptionType BLOCK_NOT_ADDED = new SimpleCommandExceptionType(ConstantComponents.BLOCK_NOT_ADDED);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("claim")
            .then(Commands.literal("settings")
                .then(Commands.literal("allowedBlocks")
                    .then(Commands.literal("add")
                        .then(Commands.argument("value", BlockStateArgument.block(buildContext))
                            .executes(context -> {
                                BlockState block = BlockStateArgument.getBlock(context, "value").getState();
                                addBlock(context.getSource(), block);
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("value", BlockStateArgument.block(buildContext))
                            .executes(context -> {
                                BlockState block = BlockStateArgument.getBlock(context, "value").getState();
                                removeBlock(context.getSource(), block);
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(context -> {
                            listBlocks(context.getSource());
                            return 1;
                        })
                    )
                    .executes(context -> {
                        listBlocks(context.getSource());
                        return 1;
                    })
                )
            )
        );
    }

    private static void addBlock(CommandSourceStack source, BlockState block) throws CommandSyntaxException {
        CadmusSaveData.addAllowedBlock(source.getServer(), TeamApi.API.getId(source.getPlayerOrException()), block.getBlock());
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.add_allowed_block", block.getBlock().getName()), false);
    }

    private static void removeBlock(CommandSourceStack source, BlockState block) throws CommandSyntaxException {
        UUID id = TeamApi.API.getId(source.getPlayerOrException());
        if (!CadmusSaveData.isBlockAllowed(source.getServer(), id, block.getBlock())) throw BLOCK_NOT_ADDED.create();
        CadmusSaveData.removeAllowedBlock(source.getServer(), id, block.getBlock());
        source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.remove_allowed_block", block.getBlock().getName()), false);
    }

    private static void listBlocks(CommandSourceStack source) throws CommandSyntaxException {
        CadmusSaveData.getAllowedBlocks(source.getServer(), TeamApi.API.getId(source.getPlayerOrException())).forEach(key -> {
            Block block = BuiltInRegistries.BLOCK.get(key);
            source.sendSuccess(() -> ModUtils.translatableWithStyle("command.cadmus.setting.list_allowed_blocks", block == Blocks.AIR ? key : block.getName()), false);
        });
    }
}
