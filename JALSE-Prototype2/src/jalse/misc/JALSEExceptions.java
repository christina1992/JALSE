package jalse.misc;

import jalse.Cluster;
import jalse.Core;
import jalse.Engine;
import jalse.JALSE;
import jalse.actions.Scheduler;
import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.attributes.Attribute;

import java.util.function.Supplier;

/**
 * These are the suppliers for all the specific exceptions JALSE throws. JALSE
 * currently does not provide any custom exception types but instead uses the
 * suitable {@code java.lang} exceptions.
 *
 * @author Elliot Ford
 *
 */
public class JALSEExceptions {

    /**
     * Runtime exception supplier for when an agent is associated twice.
     *
     * @see Cluster
     */
    public static final Supplier<RuntimeException> AGENT_ALREADY_ASSOCIATED = () -> new IllegalArgumentException(
	    "Agent is already associated");

    /**
     * Runtime exception supplier for when an agent is created past the total
     * agent limit set.
     *
     * @see Cluster
     */
    public static final Supplier<RuntimeException> AGENT_LIMIT_REARCHED = () -> new IllegalStateException(
	    "Agent limit has been reached");

    /**
     * Runtime exception supplier for when a two clusters are created with the
     * same ID.
     *
     * @see JALSE
     */
    public static final Supplier<RuntimeException> CLUSTER_ALREADY_ASSOCIATED = () -> new IllegalArgumentException(
	    "Cluster is already associated");

    /**
     * Runtime exception supplier for when a cluster is created past the total
     * cluster limit set.
     *
     * @see JALSE
     */
    public static final Supplier<RuntimeException> CLUSTER_LIMIT_REARCHED = () -> new IllegalStateException(
	    "Cluster limit has been reached");

    /**
     * Runtime exception supplier for attempting to change the running state of
     * a stopped engine.
     *
     * @see Engine
     */
    public static final Supplier<RuntimeException> ENGINE_SHUTDOWN = () -> new IllegalStateException(
	    "Engine has already been stopped");

    /**
     * Runtime exception supplier for when an {@code interface} without
     * {@link Agent} as a parent is being used as an agent type.
     *
     * @see Agents
     */
    public static final Supplier<RuntimeException> INVALID_AGENT = () -> new IllegalArgumentException(
	    "Agent is invalid");

    /**
     * Runtime exception supplier for when a type is used that does not
     * implement {@link Attribute}.
     *
     * @see Core
     */
    public static final Supplier<RuntimeException> INVALID_ATTRIBUTE_CLASS = () -> new IllegalArgumentException(
	    "Invalid attribute class");

    /**
     * Runtime exception supplier for when {@link Scheduler} methods are used
     * without being attached to an engine.
     *
     * @see Core
     */
    public static final Supplier<RuntimeException> NOT_ATTACHED = () -> new IllegalStateException(
	    "Not attached to an engine");

    private JALSEExceptions() {

	throw new UnsupportedOperationException();
    }

    /**
     * Throws the runtime exception generated by the supplier.
     *
     * @param supplier
     *            Runtime exception supplier.
     * @throws RuntimeException
     *             Will throw the supplied exception or will null pointer if
     *             supplied with {@code null}.
     */
    public static void throwRE(final Supplier<? extends RuntimeException> supplier) {

	throw supplier.get();
    }
}