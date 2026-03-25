package se.lnu.apis.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lnu.data.DataStore;
import se.lnu.data.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API — baseline implementation.
 *
 * Endpoints:
 * GET /api/rest/root → root node fields only (no children)
 * GET /api/rest/node/{id}/children → direct children of node {id} (fields only)
 *
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/rest")
public class RestController {

    @Autowired
    private DataStore dataStore;

    /**
     * Returns the root node with its K fields. No children included.
     *
     * @return 200 with root node, 503 if dataset not loaded
     */
    @GetMapping("/root")
    public ResponseEntity<Map<String, Object>> getRoot() {
        Node root = dataStore.getRoot();
        if (root == null)
            return ResponseEntity.status(503).build();
        return ResponseEntity.ok(toResponse(root));
    }

    /**
     * Returns the direct children of a node with their K fields. No grandchildren
     * included.
     *
     * @param id node identifier eg. 000001
     * @return 200 with list of child nodes, empty list if leaf or id not found
     */
    // {id:.+} allows dots in the path variable (node IDs use dot-notation)
    @GetMapping("/node/{id:.+}/children")
    public ResponseEntity<List<Map<String, Object>>> getChildren(@PathVariable String id) {
        List<Map<String, Object>> response = new ArrayList<>();
         for (Node child : dataStore.getChildren(id)) {
            response.add(toResponse(child));
         }
        return ResponseEntity.ok(response); 
}

    /**
     * Serializes a Node to a flat map: id + all K fields.
     * Children are intentionally excluded, REST fetches one level at a time.
     */
    private Map<String, Object> toResponse(Node node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", node.getId());
        map.putAll(node.getFields());
        return map;
    }
}
