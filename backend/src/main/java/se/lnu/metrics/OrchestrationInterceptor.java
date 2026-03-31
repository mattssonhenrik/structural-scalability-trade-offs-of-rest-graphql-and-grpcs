package se.lnu.metrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts HTTP requests to count DP2 (server-side orchestration)
 * 
 */
@Component 
public class OrchestrationInterceptor implements HandlerInterceptor {

    /**
     * Resets the counter before each run.
     * 
     * @return Boolean
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        OrchestrationCounter.reset();
        return true;
    }

    /**
     * Sets value of DP2 counter as a Header to be returned to testrunner.
     * Must be postHandle — afterCompletion fires after response is committed.
     */
    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res,
            Object handler, org.springframework.web.servlet.ModelAndView mv) {
        res.setHeader("X-Orchestration-Count", String.valueOf(OrchestrationCounter.get()));
    }
}
