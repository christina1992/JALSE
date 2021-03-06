package jalse.entities;

import static jalse.attributes.Attributes.EMPTY_ATTRIBUTECONTAINER;
import static jalse.entities.Entities.asType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import jalse.attributes.AttributeContainer;
import jalse.misc.ListenerSet;

/**
 * An DefaultEntityContainer is a thread-safe implementation of {@link EntityContainer}. <br>
 * <br>
 *
 * DefaultEntityContainer can take a delegate container to supply to {@link EntityEvent}. Entity
 * updates will trigger these events using {@link EntityListener}.<br>
 * <br>
 *
 * By default DefaultEntityContainer will use {@link DefaultEntityFactory} with no delegate
 * container.
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityContainer implements EntityContainer {

    /**
     * A {@link DefaultEntityContainer} instance builder that uses the provided
     * {@link EntityFactory} and delegate {@link EntityContainer}.<br>
     *
     * @author Dennis Ting
     *
     */
    public static final class Builder {

	private class EntityStub {

	    private final UUID id;
	    private final Class<? extends Entity> type;
	    private final AttributeContainer sourceContainer;

	    private EntityStub(final UUID id, final Class<? extends Entity> type,
		    final AttributeContainer sourceContainer) {
		this.id = Objects.requireNonNull(id);
		this.type = type;
		this.sourceContainer = sourceContainer != null ? sourceContainer : EMPTY_ATTRIBUTECONTAINER;
	    }
	}

	private final Set<EntityListener> builderListeners;
	private final List<EntityStub> builderEntities;
	private EntityFactory builderFactory;
	private EntityContainer builderDelegateContainer;

	/**
	 * Creates a new Builder instance.
	 */
	public Builder() {
	    builderListeners = new HashSet<>();
	    builderEntities = new ArrayList<>();
	    builderFactory = null;
	    builderDelegateContainer = null;
	}

	/**
	 * Adds new EntityListener.
	 *
	 * @param listener
	 *            EntityListener.
	 * @return This builder.
	 */
	public Builder addListener(final EntityListener listener) {
	    builderListeners.add(Objects.requireNonNull(listener));
	    return this;
	}

	/**
	 * Builds an instance of DefaultEntityContainer with the supplied parameters.
	 *
	 * @return Newly created DefaultEntityContainer instance.
	 */
	public DefaultEntityContainer build() {
	    final EntityFactory factory = builderFactory != null ? builderFactory : new DefaultEntityFactory();
	    final DefaultEntityContainer container = new DefaultEntityContainer(factory, builderDelegateContainer,
		    builderListeners);
	    builderEntities.forEach(e -> container.newEntity0(e.id, e.type, e.sourceContainer));
	    return container;
	}

	/**
	 * Adds an Entity to be created when building.
	 *
	 * @param id
	 *            ID for Entity.
	 * @return This builder.
	 */
	public Builder newEntity(final UUID id) {
	    builderEntities.add(new EntityStub(id, null, null));
	    return this;
	}

	/**
	 * Adds an Entity to be created when building.
	 *
	 * @param id
	 *            ID for Entity.
	 * @param sourceContainer
	 *            AttributeContainer for entity.
	 * @return This builder.
	 */
	public Builder newEntity(final UUID id, final AttributeContainer sourceContainer) {
	    builderEntities.add(new EntityStub(id, null, Objects.requireNonNull(sourceContainer)));
	    return this;
	}

	/**
	 * Adds an Entity to be created when building.
	 *
	 * @param id
	 *            ID for Entity.
	 * @param type
	 *            Class for Entity.
	 * @return This builder.
	 */
	public <T extends Entity> Builder newEntity(final UUID id, final Class<T> type) {
	    builderEntities.add(new EntityStub(id, Objects.requireNonNull(type), null));
	    return this;
	}

	/**
	 * Adds an Entity to be created when building.
	 *
	 * @param id
	 *            ID for Entity.
	 * @param type
	 *            Class for Entity.
	 * @param sourceContainer
	 *            AttributeContainer for entity.
	 * @return This builder.
	 */
	public <T extends Entity> Builder newEntity(final UUID id, final Class<T> type,
		final AttributeContainer sourceContainer) {
	    builderEntities
		    .add(new EntityStub(id, Objects.requireNonNull(type), Objects.requireNonNull(sourceContainer)));
	    return this;
	}

	/**
	 * Sets DelegateContainer.
	 *
	 * @param delegateContainer
	 *            Delegate container for events and entity creation.
	 * @return This builder.
	 */
	public Builder setDelegateContainer(final EntityContainer delegateContainer) {
	    builderDelegateContainer = Objects.requireNonNull(delegateContainer);
	    return this;
	}

	/**
	 * Sets EntityFactory.
	 *
	 * @param factory
	 *            Entity creation/death factory.
	 * @return This builder.
	 */
	public Builder setFactory(final EntityFactory factory) {
	    builderFactory = Objects.requireNonNull(factory);
	    return this;
	}
    }

    private final Map<UUID, Entity> entities;
    private final ListenerSet<EntityListener> listeners;
    private final EntityFactory factory;
    private final EntityContainer delegateContainer;
    private final Lock read;
    private final Lock write;

    /**
     * Creates an entity container with the default entity factory and no delegate container.
     *
     */
    public DefaultEntityContainer() {
	this(new DefaultEntityFactory());
    }

    /**
     * Creates an entity container with the supplied factory and no delegate container.
     *
     * @param factory
     *            Entity creation/death factory.
     */
    public DefaultEntityContainer(final EntityFactory factory) {
	this(factory, null, null);
    }

    /**
     * Creates an entity container with the supplied factory and delegate container.
     *
     * @param factory
     *            Entity creation/death factory.
     * @param delegateContainer
     *            Delegate container for events and entity creation.
     */
    public DefaultEntityContainer(final EntityFactory factory, final EntityContainer delegateContainer) {
	this(factory, Objects.requireNonNull(delegateContainer), null);
    }

    private DefaultEntityContainer(final EntityFactory factory, final EntityContainer delegateContainer,
	    final Set<EntityListener> listeners) {
	this.factory = Objects.requireNonNull(factory);
	this.delegateContainer = delegateContainer != null ? delegateContainer : this;
	entities = new HashMap<>();
	this.listeners = new ListenerSet<>(EntityListener.class);
	if (listeners != null) {
	    this.listeners.addAll(listeners);
	}
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    return listeners.add(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}

	if (!(obj instanceof DefaultEntityContainer)) {
	    return false;
	}

	final DefaultEntityContainer other = (DefaultEntityContainer) obj;
	return entities.equals(other.entities) && listeners.equals(other.listeners);
    }

    /**
     * Gets the delegate container for events and entity creation.
     *
     * @return Delegate container.
     */
    public EntityContainer getDelegateContainer() {
	return delegateContainer;
    }

    @Override
    public Entity getEntity(final UUID id) {
	Objects.requireNonNull(id);

	read.lock();
	try {
	    return entities.get(id);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public int getEntityCount() {
	read.lock();
	try {
	    return entities.size();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<UUID> getEntityIDs() {
	read.lock();
	try {
	    return new HashSet<>(entities.keySet());
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	read.lock();
	try {
	    return new HashSet<>(listeners);
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets entity factory for this set.
     *
     * @return Entity creation / death factory.
     */
    public EntityFactory getFactory() {
	return factory;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + entities.hashCode();
	result = prime * result + listeners.hashCode();
	return result;
    }

    @Override
    public void killEntities() {
	write.lock();
	try {
	    new ArrayList<>(entities.keySet()).forEach(this::killEntity);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean killEntity(final UUID id) {
	Objects.requireNonNull(id);

	write.lock();
	try {
	    final Entity e = entities.get(id);
	    if (e == null || !factory.tryKillEntity(e)) {
		return false;
	    }

	    entities.remove(id);
	    listeners.getProxy().entityKilled(new EntityEvent(delegateContainer, e));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer sourceContainer) {
	return newEntity0(id, null, sourceContainer);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type,
	    final AttributeContainer sourceContainer) {
	Objects.requireNonNull(type);
	return asType(newEntity0(id, type, sourceContainer), type);
    }

    private Entity newEntity0(final UUID id, final Class<? extends Entity> type,
	    final AttributeContainer sourceContainer) {
	Objects.requireNonNull(id);
	Objects.requireNonNull(sourceContainer);

	write.lock();
	try {
	    Entity e = entities.get(id);
	    if (e != null) {
		throw new IllegalArgumentException(String.format("Entity %s is already associated", id));
	    }

	    e = factory.newEntity(id, delegateContainer);
	    entities.put(id, e);

	    if (type != null) {
		e.markAsType(type);
	    }

	    e.addAll(sourceContainer);

	    listeners.getProxy().entityCreated(new EntityEvent(delegateContainer, e));

	    return e;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	if (Objects.equals(delegateContainer, Objects.requireNonNull(e))) {
	    throw new IllegalArgumentException(String.format("Cannot transfer %s to itself", e.getID()));
	}

	write.lock();
	try {

	    final UUID id = e.getID();
	    if (entities.containsKey(id)) {
		return false;
	    }

	    boolean imported = false;
	    if (!factory.tryTakeFromTree(e, delegateContainer)) {
		if (!factory.tryImportEntity(e, delegateContainer)) {
		    return false;
		}
		imported = true;
	    }

	    entities.put(id, e);
	    if (imported) { // Otherwise transfer is triggered.
		listeners.getProxy().entityReceived(new EntityEvent(delegateContainer, e));
	    }

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	write.lock();
	try {
	    return listeners.remove(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeEntityListeners() {
	write.lock();
	try {
	    listeners.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public Stream<Entity> streamEntities() {
	read.lock();
	try {
	    return new ArrayList<>(entities.values()).stream();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public String toString() {
	return "DefaultEntityContainer [" + getEntityIDs() + "]";
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	Objects.requireNonNull(id);

	if (Objects.equals(delegateContainer, Objects.requireNonNull(destination))) {
	    throw new IllegalArgumentException(String.format("Cannot transfer %s to the same container", id));
	}

	write.lock();
	try {
	    final Entity e = entities.get(id);
	    if (e == null) {
		return false;
	    }

	    if (Objects.equals(e, destination)) {
		throw new IllegalArgumentException(String.format("Cannot transfer %s to itself", id));
	    }

	    boolean exported = false;
	    if (!factory.withinSameTree(delegateContainer, destination)) {
		factory.exportEntity(e);
		exported = true;
	    }

	    if (!destination.receiveEntity(e)) {
		if (exported) {
		    throw new IllegalStateException(String.format("Entity %s exported but not transferred", id));
		}
		return false;
	    }

	    entities.remove(id);
	    listeners.getProxy().entityTransferred(new EntityEvent(delegateContainer, e, destination));

	    return true;
	} finally {
	    write.unlock();
	}
    }
}
