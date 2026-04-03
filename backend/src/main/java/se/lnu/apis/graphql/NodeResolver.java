package se.lnu.apis.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import se.lnu.data.DataStore;
import se.lnu.data.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles GraphQL requests for the tree data.
 * Returns the root node and its children when requested.
 */
@Controller
public class NodeResolver {

    @Autowired
    private DataStore dataStore;

    /**
     * Returns the root node of the current dataset.
     *
     * @return root node, or null if no dataset is loaded
     */
    @QueryMapping
    public NodeView root() {
        Node root = dataStore.getRoot();
        if (root == null)
            return null;
        return new NodeView(root);
    }

    /**
     * Returns the direct children of a node.
     *
     * @param parent the parent node
     * @return list of child nodes, or an empty list if there are none
     */
    @SchemaMapping(typeName = "Node", field = "children")
    public List<NodeView> children(NodeView parent) {
        List<NodeView> result = new ArrayList<>();
        for (Node child : dataStore.getChildren(parent.getId())) {
            result.add(new NodeView(child));
        }
        return result;
    }
}
