package com.blamejared.crafttweaker.impl.item.conditions;

import com.blamejared.crafttweaker.CraftTweakerRegistries;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IngredientVanillaPlus;
import com.blamejared.crafttweaker.api.item.conditions.IIngredientCondition;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import com.google.gson.JsonElement;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

@MethodsReturnNonnullByDefault
public class IngredientConditioned<I extends IIngredient, T extends MCIngredientConditioned<I>> extends IngredientVanillaPlus {
    
    private final T crtIngredient;
    
    public IngredientConditioned(T crtIngredient) {
        super(crtIngredient);
        this.crtIngredient = crtIngredient;
    }
    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && crtIngredient.matches(new MCItemStackMutable(stack), true);
    }
    
    @Override
    public T getCrTIngredient() {
        return crtIngredient;
    }
    
    @Override
    public JsonElement serialize() {
        return getSerializer().toJson(this);
    }
    
    @Override
    public IngredientConditionedSerializer getSerializer() {
        return CraftTweakerRegistries.INGREDIENT_CONDITIONED_SERIALIZER;
    }
    
    
    public IIngredientCondition<I> getCondition() {
        return crtIngredient.getCondition();
    }
}
