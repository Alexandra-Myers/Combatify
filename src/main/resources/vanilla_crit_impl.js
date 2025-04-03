function overrideCrit() {
	return false;
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
	if (isCrit) combinedDamage.set(combinedDamage.get() * 1.5);
	return isCrit;
}
