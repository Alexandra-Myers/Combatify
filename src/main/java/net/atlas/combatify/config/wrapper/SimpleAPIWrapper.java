package net.atlas.combatify.config.wrapper;

public record SimpleAPIWrapper<O>(O value) implements GenericAPIWrapper<O> {
	@Override
	public O unwrap() {
		return value;
	}
}
