package net.atlas.combatify.config.wrapper;

import net.minecraft.util.RandomSource;

public record RandomSourceWrapper(RandomSource randomSource) implements GenericAPIWrapper<RandomSource> {
	public void setSeed(long l) {
		randomSource.setSeed(l);
	}

	public int nextInt() {
		return randomSource.nextInt();
	}

	public int nextInt(int upper) {
		return randomSource.nextInt(upper);
	}

	public int nextIntBetweenInclusive(int i, int j) {
		return this.nextInt(j - i + 1) + i;
	}

	public long nextLong() {
		return randomSource.nextLong();
	}

	public boolean nextBoolean() {
		return randomSource.nextBoolean();
	}

	public float nextFloat() {
		return randomSource.nextFloat();
	}

	public double nextDouble() {
		return randomSource.nextDouble();
	}

	public double nextGaussian() {
		return randomSource.nextGaussian();
	}

	public double triangle(double d, double e) {
		return d + e * (this.nextDouble() - this.nextDouble());
	}

	public float triangle(float f, float g) {
		return f + g * (this.nextFloat() - this.nextFloat());
	}

	public void consumeCount(int i) {
		for(int j = 0; j < i; ++j) {
			this.nextInt();
		}

	}

	public int nextInt(int i, int j) {
		if (i >= j) {
			throw new IllegalArgumentException("bound - origin is non positive");
		} else {
			return i + this.nextInt(j - i);
		}
	}

	@Override
	public RandomSource unwrap() {
		return randomSource;
	}
}
