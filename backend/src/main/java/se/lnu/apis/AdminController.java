package se.lnu.apis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lnu.data.DataStore;

/**
 * Admin controller for reloading the dataset.
 * Used before a test starts.
 *
 * POST /api/admin/reload?D=3&F=3&K=4&seed=42
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DataStore dataStore;

    /**
     * Reloads the dataset with new values for depth, fan-out, field count, and
     * seed.
     *
     * @param D    depth
     * @param F    fan-out
     * @param K    number of fields
     * @param seed random seed
     * @return confirmation message
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reload(
            @RequestParam int D,
            @RequestParam int F,
            @RequestParam int K,
            @RequestParam(defaultValue = "42") int seed) {
        dataStore.reload(D, F, K, seed);
        return ResponseEntity.ok("Reloaded: D=" + D + " F=" + F + " K=" + K + " seed=" + seed);
    }
}
