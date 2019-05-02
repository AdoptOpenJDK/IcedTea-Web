// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.adoptopenjdk.icedteaweb;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic wrapper for thread safe lazy loading.
 * For details see: https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
 *
 * @param <T> the type of the object to lazy load.
 */
public class LazyLoaded<T> implements Supplier<T> {

    private Supplier<T> supplier;

    private volatile T instance;

    /**
     * @param supplier
     *      supplier to load the value lazy upon first request.
     *      Must not be {@code null} and should not return {@code null}.
     */
    public LazyLoaded(final Supplier<T> supplier) {
        this.supplier =  Objects.requireNonNull(supplier, "Null \"supplier\" argument passed to LazyLoaded constructor");
    }

    /**
     * @return the value which is lazy loaded if necessary.
     */
    public T get() {
        // The 'result' variable is a performance optimisation because access to the
        // volatile field 'instance' can force syncing of shared memory between threads.
        T result = instance;
        if (result == null) {
            synchronized (this) {
                result = instance;
                if (result == null) {
                    result = supplier.get();
                    instance = result;

                    if (result != null) {
                        // free supplier as it may hold on to a considerable amount of data
                        supplier = null;
                    }
                }
            }
        }

        return result;
    }

    /**
     * @return {@code true} iff the data has been loaded
     */
    public boolean isLoaded() {
        T result = instance;
        if (result == null) {
            synchronized (this) {
                result = instance;
            }
        }
        return result != null;
    }
}
