package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

class UnmodifiableDelegateAttributeContainer implements AttributeContainer {

    private final AttributeContainer delegate;

    UnmodifiableDelegateAttributeContainer(final AttributeContainer delegate) {

	this.delegate = delegate;
    }

    @Override
    public boolean addAttributeListener(final AttributeListener<? extends Attribute> listener) {

	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Attribute> Optional<T> addAttributeOfType(final T attr) {

	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Attribute> boolean fireAttributeChanged(final Class<T> attr) {

	throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeCount() {

	return delegate != null ? delegate.getAttributeCount() : 0;
    }

    @Override
    public Set<? extends AttributeListener<? extends Attribute>> getAttributeListeners() {

	return delegate != null ? delegate.getAttributeListeners() : Collections.emptySet();
    }

    @Override
    public <T extends Attribute> Set<? extends AttributeListener<T>> getAttributeListeners(final Class<T> attr) {

	return delegate != null ? delegate.getAttributeListeners(attr) : Collections.emptySet();
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeListenerTypes() {

	return delegate != null ? delegate.getAttributeListenerTypes() : Collections.emptySet();
    }

    @Override
    public <T extends Attribute> Optional<T> getAttributeOfType(final Class<T> attr) {

	return delegate != null ? delegate.getAttributeOfType(attr) : Optional.empty();
    }

    @Override
    public Set<? extends Attribute> getAttributes() {

	return delegate != null ? delegate.getAttributes() : Collections.emptySet();
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeTypes() {

	return delegate != null ? delegate.getAttributeTypes() : Collections.emptySet();
    }

    @Override
    public boolean removeAttributeListener(final AttributeListener<? extends Attribute> listener) {

	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Attribute> Optional<T> removeAttributeOfType(final Class<T> attr) {

	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributes() {

	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<? extends Attribute> streamAttributes() {

	return delegate != null ? delegate.streamAttributes() : Stream.empty();
    }
}