import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.common.blocks.behavior.HexComposting
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.command.PatternResLocArgument
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.lib.*
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.fabric.FabricHexConfig
import at.petrak.hexcasting.fabric.event.VillagerConversionCallback
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.commands.synchronization.ArgumentTypes
import net.minecraft.commands.synchronization.EmptyArgumentSerializer
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import java.util.function.BiConsumer

object FabricHexInitializer : ModInitializer {
    override fun onInitialize() {
        FabricHexConfig.setup()
        FabricPacketHandler.init()

        initListeners()

        initRegistries()

        ArgumentTypes.register(
            "hexcasting:pattern",
            PatternResLocArgument::class.java,
            EmptyArgumentSerializer { PatternResLocArgument.id() }
        )
        RegisterPatterns.registerPatterns()
        HexAdvancementTriggers.registerTriggers()
        HexComposting.setup()
        HexStrippables.init()
    }

    fun initListeners() {
        UseEntityCallback.EVENT.register(Brainsweeping::tradeWithVillager)
        VillagerConversionCallback.EVENT.register(Brainsweeping::copyBrainsweepFromVillager)

        CommandRegistrationCallback.EVENT.register { dp, _ -> HexCommands.register(dp) }
    }

    fun initRegistries() {
        HexSounds.registerSounds(bind(Registry.SOUND_EVENT))
        HexBlocks.registerBlocks(bind(Registry.BLOCK))
        HexBlocks.registerBlockItems(bind(Registry.ITEM))
        HexBlockEntities.registerTiles(bind(Registry.BLOCK_ENTITY_TYPE))
        HexItems.registerItems(bind(Registry.ITEM))

        HexEntities.registerEntities(bind(Registry.ENTITY_TYPE))

        HexRecipeSerializers.registerSerializers(bind(Registry.RECIPE_SERIALIZER))

        HexParticles.registerParticles(bind(Registry.PARTICLE_TYPE))

        // Done with soft implements in forge
        val flameOn = FlammableBlockRegistry.getDefaultInstance()
        for (log in listOf(
            HexBlocks.AKASHIC_LOG,
            HexBlocks.AKASHIC_LOG_STRIPPED,
            HexBlocks.AKASHIC_WOOD,
            HexBlocks.AKASHIC_LOG_STRIPPED,
        )) {
            flameOn.add(log, 5, 5)
        }
        for (wood in listOf(
            HexBlocks.AKASHIC_PLANKS,
            HexBlocks.AKASHIC_PANEL,
            HexBlocks.AKASHIC_TILE,
            HexBlocks.AKASHIC_DOOR,
            HexBlocks.AKASHIC_TRAPDOOR,
            HexBlocks.AKASHIC_STAIRS,
            HexBlocks.AKASHIC_SLAB,
            HexBlocks.AKASHIC_STAIRS,
            HexBlocks.AKASHIC_SLAB,
            HexBlocks.AKASHIC_BUTTON,
            HexBlocks.AKASHIC_PRESSURE_PLATE,
        )) {
            flameOn.add(wood, 20, 5)
        }
        for (papery in listOf(
            HexBlocks.SCROLL_PAPER,
            HexBlocks.SCROLL_PAPER_LANTERN,
            HexBlocks.ANCIENT_SCROLL_PAPER,
            HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,

            )) {
            flameOn.add(papery, 100, 60)
        }
        for (leaves in listOf(
            HexBlocks.AKASHIC_LEAVES1,
            HexBlocks.AKASHIC_LEAVES2,
            HexBlocks.AKASHIC_LEAVES3,
        )) {
            flameOn.add(leaves, 60, 30)
        }

        HexRecipeSerializers.registerTypes()
        HexStatistics.register()
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}