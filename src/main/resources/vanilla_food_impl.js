function processHungerTick(foodData, player) {
    return false;
}
function estimateNewFoodLevel(foodData, player, foodProperties) {
    return Math.min(foodData.getFoodLevel() + foodProperties.nutrition(), 20);
}
function estimateNewSaturationLevel(foodData, player, foodProperties) {
    return Math.min(foodData.getSaturationLevel() + foodProperties.saturation(), estimateNewFoodLevel(foodData, player, foodProperties));
}
function estimateGainedFoodLevel(foodData, player, foodProperties) {
    return estimateNewFoodLevel(foodData, player, foodProperties) - foodData.getFoodLevel();
}
function estimateGainedSaturationLevel(foodData, player, foodProperties) {
    return estimateNewSaturationLevel(foodData, player, foodProperties) - foodData.getSaturationLevel();
}
function shouldOverrideAppleSkin() {
    return false;
}
function editAppleSkinHealthGained(foodLevel, saturationLevel, exhaustionLevel) {
    return 0;
}
function addFood(foodData, food, saturation) {
    return false;
}
function canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel) {
    return true;
}
function canFastHeal(foodData, player) {
    return true;
}
function getStarvationSeconds() {
    return 4;
}
function getHealSeconds() {
    return 4;
}
function getFastHealSeconds() {
    return 0.5;
}
function getMinimumFastHealingLevel() {
    return 20;
}
function getMinimumHealingLevel() {
    return 18;
}
function fastHeal(foodData, player) {
    return false;
}
function heal(foodData, player) {
    return false;
}
