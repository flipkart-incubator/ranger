package com.flipkart.ranger.core.finderhub;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.model.Criteria;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.flipkart.ranger.core.signals.ExternalTriggeredSignal;
import com.flipkart.ranger.core.signals.ScheduledSignal;
import com.flipkart.ranger.core.signals.Signal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class ServiceFinderHub<T, R extends ServiceRegistry<T>, U extends Criteria<T, R>> {
    private final AtomicReference<Map<Service, ServiceFinder<T, R, U>>> finders = new AtomicReference<>(new HashMap<>());
    private final Lock updateLock = new ReentrantLock();
    private final Condition updateCond = updateLock.newCondition();
    private boolean updateAvailable = false;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Getter
    private final ExternalTriggeredSignal<Void> startSignal
            = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());
    @Getter
    private final ExternalTriggeredSignal<Void> stopSignal
            = new ExternalTriggeredSignal<>(() -> null, Collections.emptyList());

    private final List<Signal<Void>> refreshSignals = new ArrayList<>();

    private final ServiceDataSource serviceDataSource;
    private final ServiceFinderFactory<T, R, U> finderFactory;

    private AtomicBoolean alreadyUpdating = new AtomicBoolean(false);
    private Future<?> monitorFuture = null;

    public ServiceFinderHub(
            ServiceDataSource serviceDataSource,
            ServiceFinderFactory<T, R, U> finderFactory) {
        this.serviceDataSource = serviceDataSource;
        this.finderFactory = finderFactory;
        this.refreshSignals.add(new ScheduledSignal<>("service-hub-updater",
                                                      () -> null,
                                                      Collections.emptyList(),
                                                      10_000));
    }

    public Optional<ServiceFinder<T, R, U>> finder(final Service service) {
        return Optional.ofNullable(finders.get().get(service));
    }

    public void start() {
        monitorFuture = executorService.submit(this::monitor);
        refreshSignals.forEach(signal -> signal.registerConsumer(x -> updateAvailable()));
        startSignal.trigger();
        updateAvailable();
        log.info("Service finder hub started");
    }

    public void stop() {
        stopSignal.trigger();
        if (null != monitorFuture) {
            try {
                monitorFuture.cancel(true);
            }
            catch (Exception e) {
                log.warn("Error stopping service finder hub monitor: {}", e.getMessage());
            }
        }
        log.info("Service finder hub stopped");
    }

    public void registerUpdateSignal(final Signal<Void> refreshSignal) {
        refreshSignals.add(refreshSignal);
    }

    public void updateAvailable() {
        try {
            updateLock.lock();
            updateAvailable = true;
            updateCond.signalAll();
        }
        finally {
            updateLock.unlock();
        }
    }

    private void monitor() {
        while (true) {
            try {
                updateLock.lock();
                while (!updateAvailable) {
                    updateCond.await();
                }
                updateRegistry();
            }
            catch (InterruptedException e) {
                log.info("Updater thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
            finally {
                updateAvailable = false;
                updateLock.unlock();
            }
        }
    }

    private void updateRegistry() {
        if (alreadyUpdating.get()) {
            log.warn("Service hub is already updating");
            return;
        }
        alreadyUpdating.set(true);
        final Map<Service, ServiceFinder<T, R, U>> updatedFinders = new HashMap<>();
        try {
            final Collection<Service> services = serviceDataSource.services();
            final Map<Service, ServiceFinder<T, R, U>> knownServiceFinders = finders.get();
            val newFinders = services.stream()
                    .filter(service -> !knownServiceFinders.containsKey(service))
                    .collect(Collectors.toMap(Function.identity(), finderFactory::buildFinder));
            val matchingServices = knownServiceFinders.entrySet()
                    .stream()
                    .filter(entry -> services.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (newFinders.isEmpty() && matchingServices.size() == knownServiceFinders.size()) {
                log.debug("No update to known list of services. Skipping update on the registry.");
                return;
            }
            updatedFinders.putAll(newFinders);
            updatedFinders.putAll(matchingServices);
        }
        catch (Exception e) {
            log.error("Error updating service list. Will maintain older list", e);
        }
        finally {
            alreadyUpdating.set(false);
        }
        finders.set(updatedFinders);
    }
}
