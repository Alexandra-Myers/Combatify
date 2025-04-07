function processHungerTick(foodData, player) {
    return false;
}
function estimateNewFoodLevel(foodData, player, foodProperties) {
    return Math.min(foodData.getFoodLevel() + foodProperties.nutrition(), 20);
}
function estimateNewSaturationLevel(foodData, player, foodProperties) {
    return Math.min(foodData.getSaturationLevel() + foodProperties.saturation(), 20);
}
function estimateGainedFoodLevel(foodData, player, foodProperties) {
    return estimateNewFoodLevel(foodData, player, foodProperties) - foodData.getFoodLevel();
}
function estimateGainedSaturationLevel(foodData, player, foodProperties) {
    return estimateNewSaturationLevel(foodData, player, foodProperties) - foodData.getSaturationLevel();
}
function shouldOverrideAppleSkin() {
    return true;
}
function editAppleSkinHealthGained(foodLevel, saturationLevel, exhaustionLevel) {
    var health = 0;
    if (isFinite(exhaustionLevel) && isFinite(saturationLevel)) {
        while(foodLevel >= getMinimumHealingLevel()) {
            while(exhaustionLevel > 4.0) {
                exhaustionLevel -= 4.0;
                if (saturationLevel > 0) {
                    saturationLevel = Math.max(saturationLevel - 1, 0);
                } else {
                    --foodLevel;
                }
            }

            if (canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel) && foodLevel >= getMinimumFastHealingLevel()) {
                var limitedSaturationLevel = Math.min(saturationLevel, 6.0);
                var exhaustionUntilAboveMax = 4.0 - exhaustionLevel + 0.00000001;
                var numIterationsUntilAboveMax = Math.max(1, Math.ceil(exhaustionUntilAboveMax / limitedSaturationLevel));
                health += limitedSaturationLevel / 6.0 * numIterationsUntilAboveMax;
                exhaustionLevel += limitedSaturationLevel * numIterationsUntilAboveMax;
            } else if (foodLevel >= getMinimumHealingLevel()) {
                ++health;
                if (saturationLevel > 0.0) exhaustionLevel += 6.0;
                else foodLevel--;
            }
        }

        return health;
    } else {
        return 0;
    }
}
function addFood(foodData, food, saturation) {
    foodData.setFoodLevel(Math.max(Math.min(food + foodData.getFoodLevel(), 20), 0));
    foodData.setSaturation(Math.max(Math.min(saturation + foodData.getSaturationLevel(), 20), 0));
    return true;
}
function canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel) {
    return false;
}
function canFastHeal(foodData, player) {
    return false;
}
function getStarvationSeconds() {
    return 2;
}
function getHealSeconds() {
    return 2;
}
function getFastHealSeconds() {
    return 0.5;
}
function getMinimumFastHealingLevel() {
    return 20;
}
function getMinimumHealingLevel() {
    return 9;
}
function getMinimumSprintLevel(player) {
    return 4;
}
function fastHeal(foodData, player) {
    return false;
}
function heal(foodData, player) {
    player.heal(1.0);
    if (foodData.getSaturationLevel() >= 0.0) foodData.addExhaustion(6.0);
    else foodData.setFoodLevel(foodData.getFoodLevel() - 1);
    return true;
}
