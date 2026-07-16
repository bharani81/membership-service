package com.membership.factory;

import com.membership.entity.enums.MembershipAction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that routes membership actions to the appropriate {@link MembershipActionHandler}.
 *
 * <p>All handler implementations are auto-discovered via Spring injection.
 * Adding a new action (e.g., RENEW, PAUSE) requires only:
 * <ol>
 *   <li>Adding the value to {@link MembershipAction}.</li>
 *   <li>Creating a new {@code @Component} implementing {@link MembershipActionHandler}.</li>
 * </ol>
 * No changes to this class are required.
 */
@Slf4j
@Component
public class MembershipActionFactory {

    private final Map<MembershipAction, MembershipActionHandler> handlerMap;

    public MembershipActionFactory(List<MembershipActionHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        MembershipActionHandler::supportedAction,
                        Function.identity()
                ));
    }

    @PostConstruct
    void logRegisteredHandlers() {
        log.info("MembershipActionFactory registered handlers: {}",
                handlerMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    /**
     * Returns the handler for the requested action.
     *
     * @throws IllegalArgumentException if no handler is registered for the action
     */
    public MembershipActionHandler getHandler(MembershipAction action) {
        MembershipActionHandler handler = handlerMap.get(action);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for action: " + action);
        }
        return handler;
    }
}
