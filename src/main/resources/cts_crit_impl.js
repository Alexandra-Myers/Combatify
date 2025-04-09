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
		&& target.isLivingEntity();
	if (isCrit) combinedDamage.set(combinedDamage.get() * 1.5);
	return isCrit;
}
