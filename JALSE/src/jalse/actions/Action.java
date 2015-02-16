package jalse.actions;

import jalse.Cluster;
import jalse.JALSE;
import jalse.TickInfo;
import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;
import jalse.misc.Identifiable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Action is the JALSE equivalent of {@link Runnable}. Actions are performed
 * using a given actor and can be scheduled to be run once now, in the future or
 * periodically at an interval. {@link TickInfo} will be supplied on every
 * execution of an action, this will be current and contain the delta between
 * the last tick. Actions are generally scheduled by {@link Scheduler} for the
 * actor type suitable for the desired result.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied.
 *
 * @see Scheduler#schedule(Action, long, long, java.util.concurrent.TimeUnit)
 * @see TickInfo#getDelta()
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Gets any agent from a cluster.
     *
     * @param cluster
     *            Cluster to get agent from.
     * @return Gets an Optional of the resulting agent or an empty optional if
     *         none were found.
     *
     */
    default Optional<Agent> anyAgent(final Cluster cluster) {

	return cluster.streamAgents().findAny();
    }

    /**
     * Gets any agent marked of the specified type from the cluster.
     *
     * @param cluster
     *            Cluster to get agent from.
     * @param type
     *            Type agent has been marked by.
     * @return Gets an Optional of the resulting agent or an empty optional if
     *         none matching were found.
     *
     * @see Agent#isMarkedAsType(Class)
     * @see Agents#asType(Agent, Class)
     */
    default <S extends Agent> Optional<S> anyAgentOfType(final Cluster cluster, final Class<S> type) {

	return cluster.streamAgentsOfType(type).findAny();
    }

    /**
     * Gets any cluster from JALSE.
     *
     * @param jalse
     *            JALSE to get cluster from.
     * @return Gets an Optional of the resulting cluster or an empty optional if
     *         none were found.
     *
     */
    default Optional<Cluster> anyCluster(final JALSE jalse) {

	return jalse.streamClusters().findAny();
    }

    /**
     * Predicate to check attribute is not present.
     *
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is not present and
     *         {@code false} if it is.
     */
    default <S extends Attributable, U extends Attribute> Predicate<S> notPresent(final Class<U> attr) {

	return this.<S, U> isPresent(attr).negate();
    }

    /**
     * Predicate to check attribute is present.
     *
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is present and
     *         {@code false} if it is not.
     */
    default <S extends Attributable, U extends Attribute> Predicate<S> isPresent(final Class<U> attr) {

	return c -> c.getOfType(attr).isPresent();
    }

    /**
     * Predicate to check if the ID is equal to that supplied.
     *
     * @param id
     *            ID to check for.
     * @return Predicate of {@code true} if the ID is equal or {@code false} if
     *         it is not.
     *
     * @see Identifiable#getID()
     */
    default <S extends Identifiable> Predicate<S> isID(final UUID id) {

	return c -> c.getID().equals(id);
    }

    /**
     * Checks to see if the agent has been tagged with the type.
     *
     * @param type
     *            Agent type to check for.
     * @return Predicate of {@code true} if the agent is of the type or
     *         {@code false} if it is not.
     */
    default <S extends Agent> Predicate<S> isMarkedAsType(final Class<? extends Agent> type) {

	return a -> a.isMarkedAsType(type);
    }

    /**
     * Checks to see if the agent has not been tagged with the type.
     *
     * @param type
     *            Agent type to check for.
     * @return Predicate of {@code true} if the agent is not of the type or
     *         {@code false} if it is.
     */
    default <S extends Agent> Predicate<S> notMarkedAsType(final Class<? extends Agent> type) {

	return this.<S> isMarkedAsType(type).negate();
    }

    /**
     * Predicate to check if the ID is not equal to that supplied.
     *
     * @param id
     *            ID to check for.
     * @return Predicate of {@code false} if the ID is equal or {@code true} if
     *         it is.
     *
     * @see Identifiable#getID()
     */
    default <S extends Identifiable> Predicate<S> notID(final UUID id) {

	return this.<S> isID(id).negate();
    }

    /**
     * Performs the actions using the supplied actor and given tick information.
     *
     * @param actor
     *            Actor to use.
     * @param tick
     *            Current tick information
     *
     * @see Runnable#run()
     */
    void perform(T actor, TickInfo tick);
}