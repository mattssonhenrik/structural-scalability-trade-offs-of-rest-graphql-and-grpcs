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
 * REST API controller — baseline implementation.
 *
 * Endpoints:
 * GET /api/rest/root -> returns the root node
 * GET /api/rest/node/{id}/children -> returns the direct children of a node
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/rest")
public class RestController {

    @Autowired
    private DataStore dataStore;

    /**
     * Returns the root node.
     *
     * @return root node, or status 503 if no dataset is loaded
     */
    @GetMapping("/root")
    public ResponseEntity<Map<String, Object>> getRoot() {
        Node root = dataStore.getRoot();
        if (root == null)
            return ResponseEntity.status(503).build();
        return ResponseEntity.ok(toResponse(root));
    }

    /**
     * Returns the direct children of a node.
     *
     * @param id node id, for example 000001
     * @return list of child nodes, or an empty list if there are none
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
     * Converts a Node to a simple response format with id and fields.
     */
    private Map<String, Object> toResponse(Node node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", node.getId());
        map.putAll(node.getFields());
        return map;
    }
}
