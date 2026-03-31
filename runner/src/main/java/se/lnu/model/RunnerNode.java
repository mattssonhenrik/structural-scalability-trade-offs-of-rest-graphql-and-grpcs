package se.lnu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Runner-side representation of a tree node parsed from an API response.
 * Mirrors the backend Node structure but lives in the runner module.
 * Unknown fields (k00..k09 etc.) are ignored — runner only needs the id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunnerNode {

    private String id;
    private List<RunnerNode> children;

    public RunnerNode() {
    }

    public RunnerNode(String id, List<RunnerNode> children) {
        this.id = id;
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public List<RunnerNode> getChildren() {
        return children;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setChildren(List<RunnerNode> children) {
        this.children = children;
    }
}
