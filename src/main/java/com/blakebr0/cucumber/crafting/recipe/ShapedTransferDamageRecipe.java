package com.blakebr0.cucumber.crafting.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ShapedTransferDamageRecipe extends ShapedRecipe {
    public ShapedTransferDamageRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> inputs, ItemStack output) {
        super(id, group, width, height, inputs, output);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        var damageable = ItemStack.EMPTY;

        for (var i = 0; i < inv.getContainerSize(); i++) {
            var slotStack = inv.getItem(i);

            if (slotStack.isDamageableItem()) {
                damageable = slotStack;
                break;
            }
        }

        if (damageable.isEmpty())
            return super.assemble(inv);

        var result = this.getResultItem().copy();

        result.setDamageValue(damageable.getDamageValue());

        return result;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ShapedTransferDamageRecipe> {
        @Override
        public ShapedTransferDamageRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            var group = GsonHelper.getAsString(json, "group", "");
            var key = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            var width = pattern[0].length();
            var height = pattern.length;
            var ingredients = ShapedRecipe.dissolvePattern(pattern, key, width, height);
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            return new ShapedTransferDamageRecipe(recipeId, group, width, height, ingredients, output);
        }

        @Override
        public ShapedTransferDamageRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            var group = buffer.readUtf(32767);
            var width = buffer.readVarInt();
            var height = buffer.readVarInt();
            var ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (var k = 0; k < ingredients.size(); ++k) {
                ingredients.set(k, Ingredient.fromNetwork(buffer));
            }

            var output = buffer.readItem();

            return new ShapedTransferDamageRecipe(recipeId, group, width, height, ingredients, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedTransferDamageRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeVarInt(recipe.getRecipeWidth());
            buffer.writeVarInt(recipe.getRecipeHeight());

            for (var ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.getResultItem());
        }
    }
}
