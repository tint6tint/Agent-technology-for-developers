package fi.jyu.ties454.cleaningAgents.infra;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import fi.jyu.ties454.cleaningAgents.infra.Device.AvailableDevice;

public class PartsShop {

	private final ImmutableMap<String, Part> parts;

	public PartsShop() {
		this(DefaultDevices.class);
	}

	public PartsShop(Class<?> partsContainer) {
		Builder<String, Part> b = new Builder<>();

		Class<?>[] innerClasses = partsContainer.getDeclaredClasses();
		for (Class<?> class1 : innerClasses) {
			AvailableDevice anot = class1.getAnnotation(Device.AvailableDevice.class);

			if (anot != null) {
				int price = anot.cost();
				if (Device.class.isAssignableFrom(class1)) {
					@SuppressWarnings("unchecked")
					Class<? extends Device> deviceClass = (Class<? extends Device>) class1;
					Constructor<? extends Device> cons;
					try {
						cons = deviceClass.getDeclaredConstructor(Floor.class, AgentState.class, List.class);
					} catch (NoSuchMethodException | SecurityException e) {
						throw new Error(
								"A class annotated with AvailableDevice must have a Floor,AgentState constructor", e);
					}
					Part part = new Part(cons, price);
					b.put(deviceClass.getName(), part);
				} else {
					throw new Error("AvailableDevice annotation on someting which is not a " + Device.class.getName());
				}
			}
		}
		this.parts = b.build();
	}

	public boolean partExists(String part) {
		return this.parts.containsKey(part);
	}

	public int getPrice(String part) {
		Preconditions.checkArgument(this.partExists(part));
		return this.parts.get(part).price;
	}

	public void attachPart(String device, Floor map, AgentState state, List<AgentState> others) {
		Part part = this.parts.get(device);
		try {
			Device d = part.constructor.newInstance(map, state, others);
			// double dispatch
			d.attach(state.agent);

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new Error(e);
		}
	}

	public static class Part {
		public final Constructor<? extends Device> constructor;
		public final int price;

		public Part(Constructor<? extends Device> cons, int price) {
			super();
			this.constructor = cons;
			this.price = price;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("class", this.constructor.getDeclaringClass().getName())
					.add("price", this.price).toString();
		}
	}

	@Override
	public String toString() {
		return this.parts.toString();
	}

}
