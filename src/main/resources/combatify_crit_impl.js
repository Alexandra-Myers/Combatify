function overrideCrit() {
	return true;
}

function runCrit(player, target, combinedDamage) {
	var isCrit = player.getFallDistance() > 0
	    && !player.onGround()
		&& !player.onClimbable()
		&& !player.isInWater()
		&& !player.hasEffect("blindness")
		&& !player.isPassenger()
		&& target.isLivingEntity()
		&& !player.isSprinting()
		&& player.getAttackStrengthScale(0.5) > 0.9;
	var isChargedCrit = player.getAttackStrengthScale(0.5) > 1.95;
	if (isCrit) combinedDamage.set(combinedDamage.get() * isChargedCrit ? 1.5 : 1.25);
	return isCrit;
}
