package me.stuntguy3000.java.ledhub.handler;

import lombok.Getter;
import me.stuntguy3000.java.ledhub.LEDHub;
import me.stuntguy3000.java.ledhub.object.LEDService;
import me.stuntguy3000.java.ledhub.object.LEDServiceAction;
import me.stuntguy3000.java.ledhub.object.LEDServiceActionType;
import me.stuntguy3000.java.ledhub.object.LEDServiceActionWrapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author stuntguy3000
 */
public class ServiceHandler {
    @Getter
    private Queue<LEDServiceAction> serviceQueue = new LinkedList<>();
    @Getter
    private boolean isBackgroundRunning = false;
    @Getter
    private LEDServiceAction serviceBackground = null;

    public void addToServiceQueue(LEDService ledService) {
        ledService.getServiceActions().values().forEach(this::addToServiceQueue);
    }

    public void addToServiceQueue(LEDServiceAction ledServiceAction) {
        if (ledServiceAction != null) {
            if (ledServiceAction.getType() == LEDServiceActionType.BACKGROUND) {
                serviceBackground = ledServiceAction;
                System.out.println("Setting background");
            } else {
                switch (ledServiceAction.getLedServiceQueueCondition()) {
                    case ALWAYS_QUEUE: {
                        serviceQueue.add(ledServiceAction);

                        if (serviceQueue.size() == 1 && isBackgroundRunning) {
                            processQueue();
                        }
                        break;
                    }
                    case JUMP_QUEUE: {
                        isBackgroundRunning = false;
                        LEDHub.getInstance().getThreadHandler().getNewTask(ledServiceAction).start();
                        break;
                    }
                    case RUN_IF_FREE: {
                        if (isBackgroundRunning) {
                            LEDHub.getInstance().getThreadHandler().getNewTask(ledServiceAction).start();
                        }
                        break;
                    }
                }
            }
        }
    }

    public void processQueue() {
        if (serviceQueue.size() > 0) {
            if (isBackgroundRunning) {
                isBackgroundRunning = false;
            }

            LEDHub.getInstance().getThreadHandler().getNewTask(serviceQueue.remove()).start();
        } else {
            isBackgroundRunning = true;

            if (serviceBackground == null) {
                LEDHub.getInstance().getThreadHandler().getNewTask(
                        LEDHub.getInstance().getConfigHandler().getMainConfiguration().getBackgroundServiceActions())
                        .start();
            } else {
                LEDHub.getInstance().getThreadHandler().getNewTask(
                        serviceBackground).start();
            }
        }
    }

    /**
     * Returns an LEDService by name
     * <p>Returns null if non existent</p>
     *
     * @param name String name of the LEDService
     * @return LEDService the requested LEDService, null if invalid
     */
    public LEDService getService(String name) {
        return LEDHub.getInstance().getConfigHandler().getMainConfiguration().getLedServices().get(name.toLowerCase());
    }

    /**
     * Create a new LEDService
     *
     * @param name String name of the LEDService
     * @return LEDService the new LEDService
     */
    public LEDService createService(String name) {
        LEDService ledService = new LEDService(name, null);

        LEDHub.getInstance().getConfigHandler().getMainConfiguration().addService(ledService);

        return ledService;
    }

    /**
     * Add a new LEDAction to a LEDService
     *
     * @param serviceName String the name of the service
     * @param ledServiceActionWrapper LEDServiceActionWrapper the information about the Action
     */
    public void addAction(String serviceName, LEDServiceActionWrapper ledServiceActionWrapper) {
        LEDHub.getInstance().getConfigHandler().getMainConfiguration()
                .addAction(getService(serviceName), ledServiceActionWrapper);
    }

    /**
     * Creates a ActionWrapper
     *
     * @param actionName String the name of the action
     * @param ledServiceAction LEDServiceAction the intended action
     * @return LEDServiceActionWrapper the created action wrapper
     */
    public LEDServiceActionWrapper createActionWrapper(String actionName, LEDServiceAction ledServiceAction) {
        LEDServiceActionWrapper wrapper = new LEDServiceActionWrapper();
        wrapper.setActionName(actionName);
        wrapper.setLedServiceActionType(ledServiceAction);

        return wrapper;
    }

    /**
     * Returns all known LEDService's
     * @return HashMap all known LEDService's
     */
    public HashMap<String, LEDService> getAllServices() {
        return LEDHub.getInstance().getConfigHandler().getMainConfiguration().getLedServices();
    }

    public void setServiceBackground(LEDServiceAction ledServiceAction) {
        serviceBackground = ledServiceAction;
    }

    public void reset() {
        serviceBackground = null;
        getServiceQueue().clear();
        isBackgroundRunning = false;
    }
}
