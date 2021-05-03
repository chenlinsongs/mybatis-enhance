package cus.mybatis.enhance.core.dynamic;

import org.springframework.core.io.Resource;
import org.w3c.dom.NodeList;

import java.io.Serializable;

public class ResourceWrap implements Serializable {

    private NodeList nodeList;
    private Resource resource;
    private int resourceIndex;

    public ResourceWrap(NodeList nodeList, Resource resource, int resourceIndex) {
        this.nodeList = nodeList;
        this.resource = resource;
        this.resourceIndex = resourceIndex;
    }

    public NodeList getNodeList() {
        return nodeList;
    }

    public void setNodeList(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int getResourceIndex() {
        return resourceIndex;
    }

    public void setResourceIndex(int resourceIndex) {
        this.resourceIndex = resourceIndex;
    }
}
