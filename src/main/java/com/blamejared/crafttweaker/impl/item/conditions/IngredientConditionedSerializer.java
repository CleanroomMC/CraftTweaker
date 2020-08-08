package com.blamejared.crafttweaker.impl.item.conditions;

import com.blamejared.crafttweaker.CraftTweakerRegistries;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.conditions.IIngredientCondition;
import com.blamejared.crafttweaker.api.item.conditions.IIngredientConditionSerializer;
import com.google.gson.JsonObject;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class IngredientConditionedSerializer implements IIngredientSerializer<IngredientConditioned<?, ?>> {
    
    public JsonObject toJson(IngredientConditioned<?, ?> ingredientVanillaPlus) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("base", ingredientVanillaPlus.getCrTIngredient().getBaseIngredient().asVanillaIngredient().serialize());
        final IIngredientCondition<?> condition = ingredientVanillaPlus.getCondition();
        final JsonObject value = condition.toJson();
        if(!value.has("type")) {
            value.addProperty("type", condition.getType().toString());
        }
        jsonObject.add("condition", value);
        
        return jsonObject;
    }
    
    @Override
    public IngredientConditioned<?, ?> parse(PacketBuffer buffer) {
        final IIngredient base = IIngredient.fromIngredient(Ingredient.read(buffer));
        final ResourceLocation type = buffer.readResourceLocation();
        final Optional<IIngredientConditionSerializer<?>> value = CraftTweakerRegistries.REGISTRY_CONDITIONER_SERIALIZER.getValue(type);
        if(!value.isPresent()) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        
        // noinspection rawtypes,unchecked
        return new IngredientConditioned(new MCIngredientConditioned(base, value.get().parse(buffer)));
    }
    
    @Override
    public IngredientConditioned<?, ?> parse(JsonObject json) {
        final JsonObject base = json.getAsJsonObject("base");
        final IIngredient baseIngredient = IIngredient.fromIngredient(CraftingHelper.getIngredient(base));
        
        final JsonObject condition = json.getAsJsonObject("condition");
        final ResourceLocation type = new ResourceLocation(condition.get("type").getAsString());
        final Optional<IIngredientConditionSerializer<?>> value = CraftTweakerRegistries.REGISTRY_CONDITIONER_SERIALIZER.getValue(type);
        if(!value.isPresent()) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        
        // noinspection rawtypes,unchecked
        return new IngredientConditioned(new MCIngredientConditioned(baseIngredient, value.get().parse(condition)));
    }
    
    @Override
    public void write(PacketBuffer buffer, IngredientConditioned<?, ?> ingredient) {
        final Ingredient baseIngredient = ingredient.getCrTIngredient().getBaseIngredient().asVanillaIngredient();
        baseIngredient.write(buffer);
        
        final IIngredientCondition<?> condition = ingredient.getCondition();
        final IIngredientConditionSerializer<? extends IIngredientCondition<?>> serializer = condition.getSerializer();
        buffer.writeResourceLocation(serializer.getType());
        condition.write(buffer);
    }
}
